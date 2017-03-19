package com.github.thiagotgm.blakebot.module.admin;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class TimeoutController {
    
    private static final int START_SIZE = 100;
    private static final String ID_SEPARATOR = "@";
    private static final Logger log = LoggerFactory.getLogger( TimeoutController.class );
    
    private static TimeoutController instance;
    
    private final Timer timer;
    private final Hashtable<String, TimerTask> tasks;
    
    /**
     * Creates a new Controller instance.
     */
    private TimeoutController() {
        
        timer = new Timer( "Timeout Timer" );
        tasks = new Hashtable<>( START_SIZE );
        
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
     * Adds a new task to the timer with a certain delay and ID.
     *
     * @param task Task to be executed.
     * @param delay Delay before the task is executed.
     * @param id The ID of the task.
     */
    private void addTask( TimerTask task, long delay, String id ) {
        
        tasks.put( id, task );
        timer.schedule( task, delay );  
        
    }
    
    /**
     * Stops the Controller instance, executing all pending tasks and stopping the timer.
     * Removes the currently running instance.
     */
    public void terminate() {
        
        timer.cancel();
        for ( TimerTask task : tasks.values() ) {
            task.run();
        }    
        instance = null;
        
    }
    
    /**
     * Sets the "Deny Send Message" permission for a user in a channel.
     *
     * @param user Target user.
     * @param channel Target channel.
     * @param allow If false, sets the channel override SEND_TEXT permission of that user
     *              to deny the permission. If true, removes that override.
     */
    private void setPermission( IUser user, IChannel channel, boolean allow ) {
        
        log.debug( "Permission set " + allow + " for " + user.getName() + "@" +
                channel.getName() + "@" + channel.getGuild().getName() );
        IChannel.PermissionOverride overrides = channel.getUserOverrides().get( user.getID() );
        EnumSet<Permissions> allowed = 
                ( overrides != null ) ? overrides.allow() : EnumSet.noneOf( Permissions.class );
        EnumSet<Permissions> denied = 
                ( overrides != null ) ? overrides.deny() : EnumSet.noneOf( Permissions.class );
        if ( allow ) { // Removes deny-permission override.
            denied.remove( Permissions.SEND_MESSAGES );
        } else { // Adds deny-permission override.
            denied.add( Permissions.SEND_MESSAGES );
        }
        
        RequestBuffer.request( () -> {
            
            try { // Sets permissions for this channel.
                if ( allowed.isEmpty() && denied.isEmpty() ) { // If no overrides, remove user  
                    channel.removePermissionsOverride( user ); // from override list.
                } else {
                    channel.overrideUserPermissions( user, allowed, denied );
                }
            } catch ( DiscordException e ) {
                log.warn( "Error encountered.", e ); 
            } catch ( MissingPermissionsException e ) {
                log.info( "Missing permission to set permissions." );
            }
            
        });
        
    }
    
    /**
     * Prevents a user from writing to a given channel.
     *
     * @param user User to be restricted.
     * @param channel Channel to be restricted.
     * @param timeout How long the timeout should last, in milliseconds.
     * @return true if the user was timed out successfully.
     *         false if the user was already timed out.
     */
    public boolean timeout( IUser user, IChannel channel, long timeout ) {
        
        log.debug( "Timing out " + user.getName() + "@" + channel.getName() + "@" +
                channel.getGuild().getName()  );
        final String id = getTaskID( user, channel );
        if ( !tasks.containsKey( id ) ) {
            setPermission( user, channel, false );
            addTask( new TimerTask() {
                
                @Override
                public void run() {
                    
                    if ( !tasks.containsKey( getTaskID( user, channel.getGuild() ) ) ) {
                        // Check if guild timeout pending.
                        setPermission( user, channel, true );
                    }
                    tasks.remove( id );
                    
                }
                
            }, timeout, id );
            return true;
        } else {
            return false;
        }
        
    }
    
    /**
     * Prevents a user from writing to a given guild.
     *
     * @param user User to be restricted.
     * @param guild Guild to be restricted.
     * @param timeout How long the timeout should last, in milliseconds.
     * @return true if the user was timed out successfully.
     *         false if the user was already timed out.
     */
    public boolean timeout( IUser user, IGuild guild, long timeout ) {
        
        log.debug( "Timing out " + user.getName() + "@" + guild.getName() );
        final String id = getTaskID( user, guild );
        if ( !tasks.containsKey( id ) ) {
            for ( IChannel channel : guild.getChannels() ) {
                // Disables writing permissions for each channel the user is in on this server.
                if ( channel.getUsersHere().contains( user ) ) {
                    setPermission( user, channel, false );
                }
                
            }
            addTask( new TimerTask() {
                
                @Override
                public void run() {
                    
                    for ( IChannel channel : guild.getChannels() ) {
                        // Re-enables writing permissions for each channel the user is in
                        if ( channel.getUsersHere().contains( user ) && // on this server.
                                !tasks.containsKey( getTaskID( user, channel ) ) ) { 
                            // Check if channel timeout pending.
                            setPermission( user, channel, true );
                        }
                        
                    }
                    tasks.remove( id );
                    
                }
                
            }, timeout, id );
            return true;
        } else {
            return false;
        }
        
    }
    
    /**
     * Restores writing privileges of a user to a cetain channel.
     *
     * @param user User to be unrestricted.
     * @param guild Guild to be unrestricted.
     */
    public void untimeout( IUser user, IChannel channel ) {

        log.debug( "Untiming out " + user.getName() + "@" + channel.getName() + "@" +
                channel.getGuild().getName()  );
        // Removes and stops timeout task if any.
        TimerTask task = tasks.remove( getTaskID( user, channel ) );
        if ( task != null ) {
            task.cancel();
        }
        if ( !tasks.containsKey( getTaskID( user, channel.getGuild() ) ) ) {
            setPermission( user, channel, true );
        }
        
    }
    
    /**
     * Restores writing privileges of a user to a cetain guild.
     *
     * @param user User to be unrestricted.
     * @param guild Guild to be unrestricted.
     */
    public void untimeout( IUser user, IGuild guild ) {
        
        log.debug( "Untiming out " + user.getName() + "@" + guild.getName() );
        // Removes and stops timeout task if any.
        TimerTask task = tasks.remove( getTaskID( user, guild ) );
        if ( task != null ) {
            task.cancel();
        }
        
        for ( IChannel channel : guild.getChannels() ) {
            // Re-enables writing permissions for each channel the user is in on this server.
            if ( channel.getUsersHere().contains( user ) &&
                    !tasks.containsKey( getTaskID( user, channel ) ) ) {
                setPermission( user, channel, true );
            }
            
        }
        
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
        
        log.trace( "Checking to for " + user.getName() + "@" + channel.getName() + "@" +
                channel.getGuild().getName()  );
        return tasks.containsKey( getTaskID( user, channel ) );
        
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
        
        log.trace( "Checking to for " + user.getName() + "@" + guild.getName() );
        return tasks.containsKey( getTaskID( user, guild ) );
        
    }
    
    /**
     * Calculates the ID of a timeoutask executed over a given user in a given channel.
     *
     * @param user User the task is executed over.
     * @param channel Channel the task was executed in.
     * @return The task ID.
     */
    private static String getTaskID( IUser user, IChannel channel ) {
        
        return user.getID() + ID_SEPARATOR + channel.getID() + ID_SEPARATOR +
                channel.getGuild().getID();
        
    }
    
    /**
     * Calculates the ID of a timeoutask executed over a given user in a given guild.
     *
     * @param user User the task is executed over.
     * @param guild Guild the task was executed in.
     * @return The task ID.
     */
    private static String getTaskID( IUser user, IGuild guild ) {
        
        return user.getID() + ID_SEPARATOR + guild.getID();
        
    }

}
