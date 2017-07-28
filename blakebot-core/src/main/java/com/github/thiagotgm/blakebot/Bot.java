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
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private static final Logger LOG = LoggerFactory.getLogger( Bot.class );
    private static final String[] IMAGE_TYPES = { "png", "jpeg", "jpg", "bmp", "gif" };
    
    private static Properties properties = null;
    private static Bot instance = null;
    private static ArrayList<ConnectionStatusListener> listeners = new ArrayList<>();
    
    private volatile IDiscordClient client;
    
    /**
     * Creates a new instance of the bot.
     */
    private Bot() {
        
        String token = properties.getProperty( PropertyNames.LOGIN_TOKEN );
        ModularCommandsModule modularCommands = new ModularCommandsModule();
        try {
            this.client = new ClientBuilder().withToken( token ).build();
        } catch ( DiscordException e ) {
            LOG.error( "Failed to create bot.", e );
            System.exit( 5 );
        }
        this.client.getModuleLoader().loadModule( modularCommands );
        
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
        LOG.trace( "Registered status listener." );
        
    }
    
    /**
     * Unregisters a current listener.
     * 
     * @param listener Listener to be unregistered.
     */
    public static void unregisterListener( ConnectionStatusListener listener ) {
        
        listeners.remove( listener );
        LOG.trace( "Unregistered status listener." );
        
    }
    
    /**
     * Notifies all listeners of a change in the bot connection status.
     * 
     * @param connectionStatus New connection status of the bot.
     */
    private void notifyListeners( boolean connectionStatus ) {
        
        LOG.trace( "Notifying all listeners." );
        for ( ConnectionStatusListener listener : listeners ) {
            
            listener.connectionChange( connectionStatus );
            
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
            LOG.error( "Failed to connect!", e );
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

        LOG.info( "===[ Bot READY! ]===" );
        notifyListeners( true );

    }
    
    /**
     * Method triggered when the bot is resumed (after disconnect).
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onResume( ResumedEvent event ) {

        LOG.info( "===[ Bot RECONNECTED! ]===" );
        notifyListeners( true );

    }

    /**
     * Method triggered when the bot is diconnected from Discord.
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onDisconnect( DisconnectedEvent event ) {
     
        LOG.debug( "===[ Bot DISCONNECTED! ]===" );
        notifyListeners( false );

    }

    /**
     * Stops the bot, disconnecting it from Discord.
     * 
     * @throws DiscordException if the logout failed.
     */
    public void terminate() throws DiscordException {

        LOG.debug( "Disconnecting bot." );
        
        /* Disable modules */
        for ( IModule module : client.getModuleLoader().getLoadedModules() ) {
            
            module.disable();
            
        }
        
        /* Attempt disconnect */
        try {
            client.logout();
            LOG.info( "===[ Bot TERMINATED! ]===" );
        } catch ( DiscordException e ) {
            LOG.error( "Logout failed", e );
            throw e;
        }
        
        /* Re-enable modules */
        for ( IModule module : client.getModuleLoader().getLoadedModules() ) {
            
            module.enable( client );
            
        }

    }
    
    /**
     * Saves the properties of the bot to the properties file.
     */
    public void saveProperties() {
        
        LOG.info( "Saving properties." );
        FileOutputStream file;
        try {
            file = new FileOutputStream( PropertyNames.PROPERTIES_FILE );
            properties.storeToXML( file, PropertyNames.PROPERTIES_COMMENT );
            file.close();
        } catch ( FileNotFoundException e ) {
            LOG.error( "Could not open properties file.", e );
        } catch ( IOException e ) {
            LOG.error( "Could not write to properties file.", e );
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
     * Sets the username of the bot.
     * 
     * @param newName New username to be set.
     */
    public void setUsername( String newName ) {
        
        RequestBuffer.request( () -> {
            
            client.changeUsername( newName );
            LOG.info( "Changed bot name to {}.", newName );
            
        });
        
    }
    
    /**
     * Sets the "playing" text of the bot.
     * 
     * @param newText New playing text to be set.
     */
    public void setPlayingText( String newText ) {
        
        client.changePlayingText( newText );
        LOG.info( "Changed bot playing text to " + newText );
        
    }
    
    /**
     * Sets the bot to be idle.
     */
    public void setIdle() {
        
        client.idle();
        LOG.info( "Changed bot presence to idle." );
        
    }
    
    /**
     * Sets the bot to be online.
     */
    public void setOnline() {
        
        client.online();
        LOG.info( "Changed bot presence to online." );
        
    }
    
    /**
     * Sets the bot to be streaming with the given streaming text and the given
     * url.
     *
     * @param playingText The "streaming" text.
     * @param url The stream url.
     */
    public void setStreaming( String playingText, String url ) {
        
        client.streaming( playingText, url );
        LOG.info( "Changed bot presence to streaming {} @ {}.", playingText, url );
        
    }
    
    /**
     * Changes the profile image of the bot to that of a given URL.
     * 
     * @param url URL of the image.
     * @throws IllegalArgumentException if cannot identify the image type from the URL.
     */
    public void setImage( String url ) throws IllegalArgumentException {
        
        LOG.debug( "Changing bot image to " + url );
        // Determines type of the image from the URL.
        String type = null;
        for ( String candidate : IMAGE_TYPES ) {
            
            if ( url.contains( "." + candidate ) ) {
                type = candidate;
                break;
            }
            
        }
        if ( type == null ) {
            LOG.warn( "Image type not recognized." );
            throw new IllegalArgumentException( "Could not identify image type from URL " + url );
        }
        LOG.debug( "Detected type " + type + "." );
        try {
            client.changeAvatar( Image.forUrl( type, url ) );
            LOG.info( "Changed bot image to " + url + " of type " + type + "." );
        } catch ( DiscordException | RateLimitException e ) {
            LOG.warn( "Failed to change bot image.", e );
        }
        
    }
    
    /**
     * Changes the profile image of the bot to that of a given file.
     * 
     * @param file File of the image.
     */
    public void setImage( File file ) {
        
        LOG.debug( "Changing bot image to " + file.getAbsolutePath() + "." );
        try {
            client.changeAvatar( Image.forFile( file ) );
            LOG.info( "Changed bot image to " + file.getAbsolutePath() + "." );
        } catch ( RateLimitException | DiscordException e ) {
            LOG.warn( "Failed to change bot image.", e );
        }
        
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
