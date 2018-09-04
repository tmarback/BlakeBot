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

import com.github.thiagotgm.blakebot.common.LogoutManager;
import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.modular_commands.ModularCommandsModule;
import com.github.thiagotgm.modular_commands.api.CommandRegistry;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.util.ArrayList;

/**
 * Main bot runner that manages log in/out and bot state.
 * Uses a Singleton pattern (only a single instance can exist).
 * 
 * @author ThiagoTGM
 * @version 2.4.0
 * @since 2016-12-27
 */
public class Bot {

    private static final Logger LOG = LoggerFactory.getLogger( Bot.class );
    private static final String[] IMAGE_TYPES = { "png", "jpeg", "jpg", "bmp", "gif" };
    public static final String LOGIN_TOKEN_SETTING = "token";
    
    private static Bot instance = null;
    
    private volatile IDiscordClient client;
    private final ArrayList<ConnectionStatusListener> listeners;
    
    /**
     * Creates a new instance of the bot.
     */
    private Bot() {
        
        String token = Settings.getStringSetting( LOGIN_TOKEN_SETTING );
        try {
            client = new ClientBuilder().withToken( token ).build();
        } catch ( DiscordException e ) {
            LOG.error( "Failed to create bot.", e );
            System.exit( 5 );
        }
        listeners = new ArrayList<>();
        
        /* Set up commands */
        client.getModuleLoader().loadModule( new ModularCommandsModule() );
        String prefix = Settings.getStringSetting( "Prefix" );
        LOG.info( "Using prefix {}.", prefix );
        CommandRegistry.getRegistry( client ).setPrefix( prefix );
        
    }

    /**
     * Gets the running instance of the bot. If one is not currently running,
     * creates a new one.
     * 
     * @return The running instance of the bot.
     */
    public static Bot getInstance() {
        
        if ( instance == null ) {
            instance = new Bot();   
        }
        return instance;
        
    }
    
    /**
     * Registers a new listener (for connection status updates).
     * 
     * @param listener Listener to be registered.
     */
    public void registerListener( ConnectionStatusListener listener ) {
        
        listeners.add( listener );
        LOG.trace( "Registered status listener." );
        
    }
    
    /**
     * Unregisters a current listener.
     * 
     * @param listener Listener to be unregistered.
     */
    public void unregisterListener( ConnectionStatusListener listener ) {
        
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
     * Retrieves the client being used by the bot.
     *
     * @return The bot client.
     */
    public IDiscordClient getClient() {
        
        return client;
        
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
     
        LOG.info( "===[ Bot DISCONNECTED! ]===" );
        notifyListeners( false );

    }

    /**
     * Logs out the bot, disconnecting it from Discord.
     */
    public void logout() {

        LOG.debug( "Disconnecting bot." );
        
        /* Send logout request */
        LogoutManager.getManager( client ).logout();

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
        
        return client.getOurUser().getPresence().getText().orElse( "" );
        
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
        
        client.changePresence( StatusType.ONLINE, ActivityType.PLAYING, newText );
        LOG.info( "Changed bot playing text to " + newText );
        
    }
    
    /**
     * Sets the bot to be idle.
     */
    public void setIdle() {
        
        client.changePresence( StatusType.IDLE );
        LOG.info( "Changed bot presence to idle." );
        
    }
    
    /**
     * Sets the bot to be online.
     */
    public void setOnline() {
        
    	client.changePresence( StatusType.ONLINE );
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
        
        client.changeStreamingPresence( StatusType.ONLINE, playingText, url );
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
