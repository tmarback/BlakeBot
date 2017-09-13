/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlakeBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.module.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.LogoutManager;
import com.github.thiagotgm.blakebot.common.event.LogoutRequestedEvent;
import com.github.thiagotgm.blakebot.common.utils.AsyncTools;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuilder;
import sx.blah.discord.util.RequestBuilder.IRequestAction;

/**
 * Manager that is capable of timing out users from channels or guilds for a period of time.
 * <p>
 * Must be registered with the appropriate {@link LogoutManager} so that all pending timeouts are
 * reverted before the client logs out.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-13
 */
public class TimeoutController implements IListener<LogoutRequestedEvent> {
    
    private static final String CHANNEL_ID_FORMAT = "%d@%d@%d";
    private static final String GUILD_ID_FORMAT = "%d@%d";
    private static final Logger LOG = LoggerFactory.getLogger( TimeoutController.class );
    private static final Consumer<DiscordException> ERROR_HANDLER = e -> {
        
        LOG.error( "Error encountered while setting permissions.", e );
        
    };
    private static final Consumer<MissingPermissionsException> MISSING_PERMS_HANDLER = e -> {
        
        LOG.warn( "Does not have permission to edit permissions.", e );
        
    };
    private static final IRequestAction NO_OP = () -> { return true; };
    
    private static TimeoutController instance;
    
    private final ThreadGroup threads;
    private final ScheduledExecutorService timer;
    private final Map<String, ScheduledUntimeout> pending;
    private final Multiset<String> timeouts;
    
    /**
     * Creates a new Controller instance.
     */
    private TimeoutController() {
        
        threads = new ThreadGroup( "TimeoutController Scheduler" );
        timer = AsyncTools.createScheduledThreadPool( threads, ( t, e ) -> {
                
                LOG.error( "Uncaught exception thrown while managing timeouts.", e );
                
            } );
        pending = new ConcurrentHashMap<>();
        timeouts = ConcurrentHashMultiset.create();
        
    }
    
    /**
     * Gets the currently running instance of the Controller, or creates one if none
     * exist.
     *
     * @return The currently running instance of the Controller.
     */
    public static TimeoutController getInstance() {
        
        if ( instance == null ) {
            instance = new TimeoutController();
        }
        return instance;
        
    }
    
    /**
     * Executes pending tasks before the bot logs out.
     *
     * @param event Event fired.
     */
    public void handle( LogoutRequestedEvent event ) {
        
        terminate();
        
    }
    
    /**
     * Executes all pending tasks, in preparation for stopping the module.
     */
    public synchronized void terminate() {
        
        LOG.info( "Terminating." );
        for ( ScheduledUntimeout task : pending.values() ) {
            // Execute pending timeout reversals.
            task.run();
            
        }
        LOG.debug( "Terminated." );
        
    }
    
    /**
     * Configures a request for setting the timeout state for a given user in a given channel.
     *
     * @param user Target user.
     * @param channel Target channel.
     * @param timeout If <tt>true</tt>, places the user on timeout, that is, sets the channel
     *                override SEND_TEXT permission of that user to deny the permission. If
     *                <tt>false</tt>, removes the timeout (removes that override).
     * @return A request that will set (or unset) the timeout for the given user in the given channel.
     */
    private IRequestAction setTimeout( IUser user, IChannel channel, boolean timeout ) {
        
        LOG.trace( "Requested timeout set {} for {} in channel {} of guild.", timeout, user.getName(),
                channel.getName(), channel.getGuild().getName() );
        IChannel.PermissionOverride overrides = channel.getUserOverridesLong().get( user.getLongID() );
        EnumSet<Permissions> allowed = 
                ( overrides != null ) ? overrides.allow() : EnumSet.noneOf( Permissions.class );
        EnumSet<Permissions> denied = 
                ( overrides != null ) ? overrides.deny() : EnumSet.noneOf( Permissions.class );
        String timeoutID = getTaskID( user, channel );
        synchronized ( this ) {
            
            if ( timeout ) { // Adds deny-permission override.
                timeouts.add( timeoutID );
                if ( timeouts.count( timeoutID ) > 1 ) {
                    LOG.trace( "Timeout already in place." );
                    return NO_OP; // A timeout was alread in place.
                }
                denied.add( Permissions.SEND_MESSAGES );
            } else { // Removes deny-permission override.
                timeouts.remove( timeoutID );
                if ( timeouts.contains( timeoutID ) ) {
                    LOG.trace( "Equal timeout still in place." );
                    return NO_OP; // There is still another timeout in place.
                }
                denied.remove( Permissions.SEND_MESSAGES );
            }
            
        }
        
        return () -> {
            
            LOG.debug( "Setting timeout {} for {} in channel {} of guild.", timeout, user.getName(),
                    channel.getName(), channel.getGuild().getName() );
            if ( allowed.isEmpty() && denied.isEmpty() ) { // If no overrides, remove user  
                channel.removePermissionsOverride( user ); // from override list.
            } else {
                channel.overrideUserPermissions( user, allowed, denied );
            }
            return true;
            
        };
        
    }
    
    /**
     * Sets the timeout state for a user in a list of channels.
     *
     * @param user Target user.
     * @param channels Target channels.
     * @param timeout If <tt>true</tt>, places the user on timeout, that is, sets the channel
     *                override SEND_TEXT permission of that user on each channel to deny the permission.
     *                If <tt>false</tt>, removes the timeout (removes that override).
     */
    private void setTimeout( IUser user, List<IChannel> channels, boolean timeout ) {
        
        RequestBuilder request = new RequestBuilder( user.getClient() ).shouldBufferRequests( true )
                .setAsync( false ).shouldFailOnException( false ).onDiscordError( ERROR_HANDLER )
                .onMissingPermissionsError( MISSING_PERMS_HANDLER ).doAction( NO_OP );
        for ( IChannel channel : channels ) {
            
            request.andThen( setTimeout( user, channel, timeout ) );
            
        }
        request.execute();
        
    }
    
    /**
     * Times out a user from a list of guilds.
     *
     * @param user User to be timed out.
     * @param channels Channels to time out in.
     * @param timeout How long the timeout should last, in milliseconds.
     * @param taskID The ID of the timeout task.
     * @return <tt>true</tt> if the user was timed out successfully.
     *         <tt>false</tt> if the user was already timed out.
     */
    private synchronized boolean timeout( IUser user, List<IChannel> channels, long timeout, String taskID ) {
        
        if ( !pending.containsKey( taskID ) ) {
            setTimeout( user, channels, true );
            ScheduledUntimeout untimeout = new ScheduledUntimeout( user, channels, taskID );
            pending.put( taskID, untimeout );
            timer.schedule( untimeout, timeout, TimeUnit.MILLISECONDS );
            return true;
        } else {
            return false;
        }
        
    }
    
    /**
     * Times out a user in a channel for a given time, preventing him/her from writing to
     * the channel until the time has been elapsed or the timeout is lifted.
     *
     * @param user User to be timed out.
     * @param channel Channel to time out in.
     * @param timeout How long the timeout should last, in milliseconds.
     * @return <tt>true</tt> if the user was timed out successfully.
     *         <tt>false</tt> if the user was already timed out.
     */
    public boolean timeout( IUser user, IChannel channel, long timeout ) {
        
        LOG.debug( "Requested timing out {}@{}@{} for {}ms.", user.getName(), channel.getName(),
                channel.getGuild().getName(), timeout );
        return timeout( user, Arrays.asList( channel ), timeout, getTaskID( user, channel ) );
        
    }
    
    /**
     * Times out a user in a guild for a given time, preventing him/her from writing to
     * channels in the guild until the time has been elapsed or the timeout is lifted.
     *
     * @param user User to be timed out.
     * @param guild Guild to time out in.
     * @param timeout How long the timeout should last, in milliseconds.
     * @return <tt>true</tt> if the user was timed out successfully.
     *         <tt>false</tt> if the user was already timed out.
     */
    public boolean timeout( IUser user, IGuild guild, long timeout ) {
        
        LOG.debug( "Requested timing out {}@{} for {}ms.", user.getName(), guild.getName(), timeout );
        return timeout( user, guild.getChannels(), timeout, getTaskID( user, guild ) );
        
    }
    
    /**
     * Removes a currently placed timeout. 
     *
     * @param taskID The task ID of the timeout that was placed.
     * @return <tt>true</tt> if the timeout was reverted successfully.
     *         <tt>false</tt> if there is no timeout currently in place with the given ID.
     */
    private synchronized boolean untimeout( String taskID ) {
        
        if ( pending.containsKey( taskID ) ) {
            pending.remove( taskID ).run();
            return true;
        } else {
            return false;
        }
        
    }
    
    /**
     * Lifts the timeout on a user in a channel, restoring his/her ability to write to
     * the channel.<br>
     * If a guild-wide timeout is still in effect, the user will still not be able to write
     * until that timeout is also completed/lifted.
     *
     * @param user User to be un-timed out.
     * @param channel Channel to un-time out in.
     * @return <tt>true</tt> if the timeout was lifted successfully.
     *         <tt>false</tt> if the user is not timed out in the given channel.
     */
    public boolean untimeout( IUser user, IChannel channel ) {

        LOG.debug( "Requested un-timing out {}@{}@{}.", user.getName(), channel.getName(),
                channel.getGuild().getName() );
        return untimeout( getTaskID( user, channel ) );
        
    }
    
    /**
     * Lifts the timeout on a user in a guild, restoring his/her ability to write to
     * channels in that guild (except in channels timeout is still in place).<br>
     * In channels where a channel-wide timeout is still in effect, the user will
     * still not be able to write until that timeout is also completed/lifted.
     *
     * @param user User to be un-timed out.
     * @param guild Guild to un-time out in.
     * @return <tt>true</tt> if the timeout was lifted successfully.
     *         <tt>false</tt> if the user is not timed out in the given guild.
     */
    public boolean untimeout( IUser user, IGuild guild ) {
        
        LOG.debug( "Requested un-timing out {}@{}.", user.getName(), guild.getName() );
        return untimeout( getTaskID( user, guild ) );
        
    }
    
    /**
     * Checks if a user has a timeout currently running on a given channel.
     *
     * @param user User to be checked. 
     * @param channel Channel to be checked.
     * @return true if the user has a timeout on that channel.
     *         false otherwise.
     */
    public boolean hasTimeout( IUser user, IChannel channel ) {
        
        LOG.trace( "Checking timeout for {}@{}@{}", user.getName(), channel.getName(),
                channel.getGuild().getName() );
        return pending.containsKey( getTaskID( user, channel ) );
        
    }
    
    /**
     * Checks if a user has a timeout currently running on a given guild.
     *
     * @param user User to be checked. 
     * @param guild Guild to be checked.
     * @return true if the user has a timeout on that guild.
     *         false otherwise.
     */
    public boolean hasTimeout( IUser user, IGuild guild ) {
        
        LOG.trace( "Checking timeout for {}@{}", user.getName(), guild.getName() );
        return pending.containsKey( getTaskID( user, guild ) );
        
    }
    
    /**
     * Generates the ID of a timeout task executed over a given user in a given channel.
     *
     * @param user User the task is executed over.
     * @param channel Channel the task was executed in.
     * @return The task ID.
     */
    private static String getTaskID( IUser user, IChannel channel ) {
        
        return String.format( CHANNEL_ID_FORMAT, user.getLongID(), channel.getLongID(),
                channel.getGuild().getLongID() );
        
    }
    
    /**
     * Generates the ID of a timeout task executed over a given user in a given guild.
     *
     * @param user User the task is executed over.
     * @param guild Guild the task was executed in.
     * @return The task ID.
     */
    private static String getTaskID( IUser user, IGuild guild ) {
        
        return String.format( GUILD_ID_FORMAT, user.getLongID(), guild.getLongID() );
        
    }
    
    /**
     * Un-timeout task to be scheduled when a timeout is set.<br>
     * Can only be performed once, with additional run calls doing nothing.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-12
     */
    private class ScheduledUntimeout implements Runnable {
        
        private volatile boolean pending;
        private final List<IChannel> channels;
        private final IUser user;
        private final String id;
        
        /**
         * Initializes an untimeout task for the given user in the given channels.
         *
         * @param user The user to apply the untimeout for.
         * @param channels The channels where the untimeout should be applied.
         * @param id The ID of the task.
         */
        private ScheduledUntimeout( IUser user, List<IChannel> channels, String id ) {
            
            this.pending = true;
            this.channels = new ArrayList<>( channels );
            this.user = user;
            this.id = id;
            
        }

        /**
         * Performs the configured untimeout. If was already performed before, does nothing.
         */
        @Override
        public synchronized void run() {

            if ( pending ) { // Task still pending.
                pending = false; // Task will now start.
            } else { // Task already started.
                return; // Nothing to do.
            }
            
            TimeoutController.this.pending.remove( id );
            setTimeout( user, channels, false );
            
        }
        
    }

}
