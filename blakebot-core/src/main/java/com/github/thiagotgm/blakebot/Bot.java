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

package com.github.thiagotgm.blakebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.modular_commands.ModularCommandsModule;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Main bot runner that manages log in/out and bot state.
 * Uses a Singleton pattern (only a single instance can exist).
 * The instance can only be started after the properties were set using the
 * {@link #setProperties(Properties) setProperties} method.
 * 
 * @author ThiagoTGM
 * @version 2.4.0
 * @since 2016-12-27
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger( Bot.class );
    private static final String[] IMAGE_TYPES = { "png", "jpeg", "jpg", "bmp", "gif" };
    private static final String[] TERMINATE_MODULES = { "Admin Module" };
    
    private static Properties properties = null;
    private static Bot instance = null;
    private static ArrayList<ConnectionStatusListener> listeners = new ArrayList<>();
    
    private volatile IDiscordClient client;
    private long startTime;
    private Time lastUptime;
    
    /**
     * Creates a new instance of the bot.
     */
    private Bot() {
        
        String token = properties.getProperty( PropertyNames.LOGIN_TOKEN );
        ModularCommandsModule modularCommands = new ModularCommandsModule();
        try {
            this.client = new ClientBuilder().withToken( token ).build();
        } catch ( DiscordException e ) {
            log.error( "Failed to create bot.", e );
            System.exit( 5 );
        }
        this.client.getModuleLoader().loadModule( modularCommands );
        this.startTime = 0;
        this.lastUptime = new Time( 0 );
        
    }

    /**
     * Gets the running instance of the bot. If one is not currently running,
     * creates a new one.
     * Can only be used after setting bot properties with
     * {@link #setProperties(Properties) setProperties} once,
     * as the bot depends on them to function correctly.
     * 
     * @return The running instance of the bot.
     * @throws IllegalStateException if used before bot properties are set.
     */
    public static Bot getInstance() throws IllegalStateException {
        
        if ( properties == null ) {
            throw new IllegalStateException( "Attempted to use bot before setting properties." );
        }
        if ( instance == null ) {
            instance = new Bot();   
        }
        return instance;
        
    }
    
    /**
     * Sets the bot properties.
     * 
     * @param properties Properties used by the bot.
     */
    public static void setProperties( Properties properties ) {

        Bot.properties = properties;

    }
    
    /**
     * Retrieves the properties of the bot.
     * 
     * @return The object that contains the properties of the bot.
     */
    public static Properties getProperties() {
        
        return Bot.properties;
        
    }
    
    /**
     * Registers a new listener (for connection status updates).
     * 
     * @param listener Listener to be registered.
     */
    public static void registerListener( ConnectionStatusListener listener ) {
        
        listeners.add( listener );
        log.trace( "Registered status listener." );
        
    }
    
    /**
     * Unregisters a current listener.
     * 
     * @param listener Listener to be unregistered.
     */
    public static void unregisterListener( ConnectionStatusListener listener ) {
        
        listeners.remove( listener );
        log.trace( "Unregistered status listener." );
        
    }
    
    /**
     * Notifies all listeners of a change in the bot connection status.
     * 
     * @param connectionStatus New connection status of the bot.
     */
    private void notifyListeners( boolean connectionStatus ) {
        
        log.trace( "Notifying all listeners." );
        for ( ConnectionStatusListener listener : listeners ) {
            
            listener.connectionChange( connectionStatus );
            
        }
        
    }
    
    /**
     * Retrieves the modules that have to be terminated before disconnecting
     * the bot.
     * 
     * @return The list of modules that need to be terminated.
     */
    private List<IModule> getTerminateModules() {
        
        List<String> targets = Arrays.asList( TERMINATE_MODULES );
        List<IModule> found = new LinkedList<>();
        for ( IModule module : client.getModuleLoader().getLoadedModules() ) {
            
            if ( targets.contains( module.getName() ) ) {
                found.add( module );
            }
            
        }
        return found;
        
    }
    
    /**
     * Reboots the modules that need to be terminated on disconnect, so no tasks will be
     * pending.
     */
    private void rebootModules() {
        
        for ( IModule module : getTerminateModules() ) {
            
            module.disable();
            module.enable( client );
            
        }
        
    }

    /**
     * Logs in to Discord.
     * 
     * @throws DiscordException if the login failed.
     */
    public void login() throws DiscordException, RateLimitException {

        try {
            client.login();
        } catch ( DiscordException | RateLimitException e ) {
            log.error( "Failed to connect!", e );
            throw e;
        }
        client.getDispatcher().registerListener( this );

    }

    /**
     * Method triggered when the bot is connected to Discord and ready to use.
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onReady( ReadyEvent event ) {

        connected();
        log.info( "=== Bot READY! ===" );

    }
    
    /**
     * Method triggered when the bot is resumed (after disconnect).
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onResume( ResumedEvent event ) {

        connected();
        log.info( "=== Bot RECONNECTED! ===" );

    }
    
    /**
     * Records time that the bot connected and notifies listeners.
     */
    private void connected() {
        
        startTime = System.currentTimeMillis();
        notifyListeners( true );
        
    }
    
    /**
     * Resets the bot to a disconnected state and notifies the listeners
     * for a disconnection.
     */
    private void disconnected() {
      
        if ( startTime != 0 ) {
            lastUptime = new Time( System.currentTimeMillis() - startTime );
            log.info( "Disconnected after " + lastUptime.toString( false ) );
            startTime = 0;
        } else {
            log.debug( "Bot disconnected - but was not connected." );
        }
        notifyListeners( false );
        
    }

    /**
     * Method triggered when the bot is diconnected from Discord.
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onDisconnect( DisconnectedEvent event ) {
     
        log.debug( "Bot disconnected." );
        disconnected();

    }

    /**
     * Stops the bot, disconnecting it from Discord.
     * 
     * @throws DiscordException if the logout failed.
     */
    public void terminate() throws DiscordException {

        log.debug( "Disconnecting bot." );
        try {
            rebootModules();
            client.logout();
            log.info( "=== Bot terminated ===" );
        } catch ( DiscordException e ) {
            log.error( "Logout failed", e );
            throw e;
        }

    }
    
    /**
     * Saves the properties of the bot to the properties file.
     */
    public void saveProperties() {
        
        log.info( "Saving properties." );
        FileOutputStream file;
        try {
            file = new FileOutputStream( PropertyNames.PROPERTIES_FILE );
            properties.storeToXML( file, PropertyNames.PROPERTIES_COMMENT );
            file.close();
        } catch ( FileNotFoundException e ) {
            log.error( "Could not open properties file.", e );
        } catch ( IOException e ) {
            log.error( "Could not write to properties file.", e );
        }
        
        
    }
    
    /**
     * Retrieves whether the bot is currently connected to Discord.
     * 
     * @return true if the bot is currently connected.
     *         false otherwise.
     */
    public boolean isConnected() {
        
        return ( client != null ) && ( client.isLoggedIn() );
        
    }
    
    /**
     * Retrieves the current username of the bot.
     * 
     * @return The username of the bot.
     */
    public String getUsername() {
        
        return client.getOurUser().getName();
        
    }
    
    /**
     * Retrieves the current status of the bot.
     * 
     * @return The status of the bot.
     */
    public String getStatus() {
        
        return client.getOurUser().getPresence().getPlayingText().orElse( "" );
        
    }
    
    /**
     * Gets the time that the bot has been connected to Discord.
     * 
     * @return The time elapsed, in milliseconds.
     * @throws IllegalStateException if called when the bot is currently disconnected.
     */
    public Time getUptime() throws IllegalStateException {
        
        if ( startTime == 0 ) {
            throw new IllegalStateException( "Tried to get uptime when the bot is disconnected." );
        }
        
        Time uptime = new Time( System.currentTimeMillis() - startTime );
        log.debug( "Uptime: " + uptime.getTotalTime() + "ms = " + uptime.toString( false ) );
        return uptime;
        
    }
    
    /**
     * Gets the time that the bot has been connected to Discord before the last disconnect.
     * 
     * @return The time elapsed, in milliseconds.
     */
    public Time getLastUptime() {
        
        return new Time( lastUptime );
        
    }
    
    /**
     * Gets the username of the owner of the bot account.
     * 
     * @return The Discord username of the owner.
     * @throws DiscordException if the name could not be retrieved.
     */
    public String getOwner() throws DiscordException {
        
        try {
            return client.getApplicationOwner().getName();
        } catch ( DiscordException e ) {
            log.warn( "Could not get owner name.", e );
            throw e;
        }
        
    }
    
    /**
     * Gets the nickname of the owner of the bot in a certain server.
     * 
     * @param server Target server.
     * @return The nickname of the bot owner in the server, or null if it doesn't
     *         exist.
     */
    public String getOwner( IGuild server ) {
        
        return client.getApplicationOwner().getNicknameForGuild( server );
        
    }
    
    /**
     * Retrieves the url of the avatar image of the owner of the bot.
     *
     * @return The avatar of the owner of the bot.
     * @throws DiscordException If the image could not be retrieved.
     */
    public String getOwnerImage() throws DiscordException {
        
        try {
            return client.getApplicationOwner().getAvatarURL();
        } catch ( DiscordException e ) {
            log.warn( "Could not get owner image url.", e );
            throw e;
        }
        
    }
    
    /**
     * Sets the username of the bot.
     * 
     * @param newName New username to be set.
     */
    public void setUsername( String newName ) {
        
        try {
            client.changeUsername( newName );
            log.info( "Changed bot name to " + newName );
        } catch ( DiscordException | RateLimitException e ) {
            log.warn( "Failed to change username.", e );
        }
        
    }
    
    /**
     * Sets the status of the bot.
     * 
     * @param newStatus New status to be set.
     */
    public void setStatus( String newStatus ) {
        
        client.online( newStatus );
        log.info( "Changed bot status to " + newStatus );
        
    }
    
    /**
     * Sets the presence of the bot.
     * 
     * @param isIdle If true, the bot becomes idle.
     *               If false, becomes online.
     */
    public void setIdle( boolean isIdle ) {
        
        if ( isIdle ) {
            client.idle();
        } else {
            client.online();
        }
        log.info( "Changed bot presence to " + ( isIdle ? "idle" : "online" )
                + "." );
        
    }
    
    /**
     * Changes the profile image of the bot to that of a given URL.
     * 
     * @param url URL of the image.
     * @throws IllegalArgumentException if cannot identify the image type from the URL.
     */
    public void setImage( String url ) throws IllegalArgumentException {
        
        log.debug( "Changing bot image to " + url );
        // Determines type of the image from the URL.
        String type = null;
        for ( String candidate : IMAGE_TYPES ) {
            
            if ( url.contains( "." + candidate ) ) {
                type = candidate;
                break;
            }
            
        }
        if ( type == null ) {
            log.warn( "Image type not recognized." );
            throw new IllegalArgumentException( "Could not identify image type from URL " + url );
        }
        log.debug( "Detected type " + type + "." );
        try {
            client.changeAvatar( Image.forUrl( type, url ) );
            log.info( "Changed bot image to " + url + " of type " + type + "." );
        } catch ( DiscordException | RateLimitException e ) {
            log.warn( "Failed to change bot image.", e );
        }
        
    }
    
    /**
     * Changes the profile image of the bot to that of a given file.
     * 
     * @param file File of the image.
     */
    public void setImage( File file ) {
        
        log.debug( "Changing bot image to " + file.getAbsolutePath() + "." );
        try {
            client.changeAvatar( Image.forFile( file ) );
            log.info( "Changed bot image to " + file.getAbsolutePath() + "." );
        } catch ( RateLimitException | DiscordException e ) {
            log.warn( "Failed to change bot image.", e );
        }
        
    }
    
    /**
     * Gets all the channels visible to the bot.
     *
     * @return The list of channels visible to the bot.
     */
    public List<IChannel> getChannels() {
        
        return client.getChannels( true );
        
    }
    
    /**
     * Gets all the public channels visible to the bot.
     *
     * @return The list of public channels visible to the bot.
     */
    public List<IChannel> getPublicChannels() {
        
        return client.getChannels();
        
    }
    
    /**
     * Gets all the private channels visible to the bot.
     *
     * @return The list of private channels visible to the bot.
     */
    public List<IChannel> getPrivateChannels() {
        
        List<IChannel> channels = client.getChannels( true );
        channels.removeAll( client.getChannels() );
        return channels;
        
    }
    
    /**
     * Gets all the guilds (servers) where the bot is present.
     *
     * @return The list of guilds that the bot is connected to.
     */
    public List<IGuild> getGuilds() {
        
        return client.getGuilds();
        
    }
    
    /**
     * Retrieves the current version of the core module.
     *
     * @return The version of the core module.
     */
    public String getVersion() {
        
        return getClass().getPackage().getImplementationVersion();
        
    }

}
