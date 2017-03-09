package com.github.thiagotgm.blakebot.module.admin;

import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
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
    private static final String ID_SEPARATOR = "|";
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
     */
    public void timeout( IUser user, IChannel channel, long timeout ) {
        
        final String id = user.getID() + ID_SEPARATOR + channel.getID() +
                ID_SEPARATOR + channel.getGuild().getID();
        if ( !tasks.contains( id ) ) {
            setPermission( user, channel, false );
            addTask( new TimerTask() {
                
                @Override
                public void run() {
                    
                    // TODO: Check if guild timeout pending.
                    setPermission( user, channel, true );
                    tasks.remove( id );
                    
                }
                
            }, timeout, id );
        } else {
            // TODO: Error message
        }
        
    }
    
    /**
     * Prevents a user from writing to a given guild.
     *
     * @param user User to be restricted.
     * @param guild Guild to be restricted.
     * @param timeout How long the timeout should last, in milliseconds.
     */
    public void timeout( IUser user, IGuild guild, long timeout ) {
        
        for ( IChannel channel : guild.getChannels() ) {
            // Disables writing permissions for each channel the user is in on this server.
            if ( channel.getUsersHere().contains( user ) ) {
                setPermission( user, channel, false );
            }
            
        }
        
    }
    
    /**
     * Restores writing privileges of a user to a cetain channel.
     *
     * @param user User to be unrestricted.
     * @param guild Guild to be unrestricted.
     */
    public void untimeout( IUser user, IChannel channel ) {

        setPermission( user, channel, true );
        
    }
    
    /**
     * Restores writing privileges of a user to a cetain guild.
     *
     * @param user User to be unrestricted.
     * @param guild Guild to be unrestricted.
     */
    public void untimeout( IUser user, IGuild guild ) {
        
        for ( IChannel channel : guild.getChannels() ) {
            // Re-enables writing permissions for each channel the user is in on this server.
            if ( channel.getUsersHere().contains( user ) ) {
                setPermission( user, channel, true );
            }
            
        }
        
    }

}
