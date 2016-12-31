package com.github.thiagotgm.blakebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main bot runner that manages log in/out and bot state.
 * Uses a Singleton pattern (only a single instance can exist).
 * The instance can only be started after the properties were set using the
 * setProperties() method.
 * 
 * @author ThiagoTGM
 * @version 2.0
 * @since 2016-12-27
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger( Bot.class );
    private static final String[] IMAGE_TYPES = { "png", "jpeg", "jpg", "bmp", "gif" };
    
    private static Properties properties = null;
    private static Bot instance = null;
    
    private volatile IDiscordClient client;
    private final AtomicBoolean reconnect;
    private ArrayList<ConnectionStatusListener> listeners;
    
    /**
     * Creates a new instance of the bot.
     */
    private Bot() {
        
        this.client = null;
        this.reconnect = new AtomicBoolean( true );
        this.listeners = new ArrayList<>();
        
    }

    /**
     * Gets the running instance of the bot. If one is not currently running,
     * creates a new one.
     * Can only be used after setting bot properties with setProperties() once,
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
    public void registerListener( ConnectionStatusListener listener ) {
        
        listeners.add( listener );
        log.trace( "Registered status listener." );
        
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
     * Logs in to Discord.
     */
    public void login() throws DiscordException {

        String token = properties.getProperty( PropertyNames.LOGIN_TOKEN );
        try {
            client = new ClientBuilder().withToken( token ).login();
        } catch ( DiscordException e ) {
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

        notifyListeners( true );
        log.info( "=== Bot READY! ===" );

    }

    /**
     * Method triggered when the bot is diconnected from Discord.
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onDisconnect( DisconnectedEvent event ) {

        log.debug( "Bot disconnected." );
        CompletableFuture.runAsync( new Runnable() {

            @Override
            public void run() {

                // If not requested disconnect, attempts to reconnect.
                if ( reconnect.get() ) {
                    log.info( "Reconnecting bot" );
                    try {
                        login();
                    } catch ( DiscordException e ) {
                        log.warn( "Failed to reconnect bot", e );
                        client = null;
                        notifyListeners( false );
                    }
                }

            }

        } );

    }

    /**
     * Method triggered when a message is received in one of the channels
     * the bot is reading from.
     * 
     * @param event Event triggered.
     */
    @EventSubscriber
    public void onMessage( MessageReceivedEvent event ) {

        log.debug( "Got message" );

        IMessage message = event.getMessage(); // Gets the message from the event
                                           // object NOTE: This is not the
                                           // content of the message, but the
                                           // object itself
        IChannel channel = message.getChannel(); // Gets the channel in which
                                                 // this message was sent.

        String newMessage;
        if ( message.getContent().equals( "!exit" ) ) {
            try {
                terminate();
            } catch ( DiscordException e ) {
                newMessage = "Sorry, failed to exit.";
            }
            return;
        } else if ( message.getContent().equalsIgnoreCase( "hi" ) ) {
            newMessage = "Hi, I am a bot!";
        } else {
            newMessage = message.getContent();
        }
        try {
            // Builds (sends) and new message in the channel that the original
            // message was sent with the content of the original message.
            new MessageBuilder( this.client ).withChannel( channel )
                    .withContent( newMessage ).build();
        } catch ( RateLimitException e ) { // RateLimitException thrown. The bot
                                           // is sending messages too quickly!
            System.err.print( "Sending messages too quickly!" );
            e.printStackTrace();
        } catch ( DiscordException e ) { // DiscordException thrown. Many
                                         // possibilities. Use getErrorMessage()
                                         // to see what went wrong.
            System.err.print( e.getErrorMessage() ); // Print the error message
                                                     // sent by Discord
            e.printStackTrace();
        } catch ( MissingPermissionsException e ) { // MissingPermissionsException
                                                    // thrown. The bot doesn't
                                                    // have permission to send
                                                    // the message!
            System.err.println( "Missing permissions for channel!" );
            e.printStackTrace();
        }

    }

    /**
     * Stops the bot, disconnecting it from Discord.
     */
    public void terminate() throws DiscordException {

        reconnect.set( false );
        log.debug( "Disconnecting bot." );
        try {
            client.logout();
            client = null;
            notifyListeners( false );
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
        
        return client.getOurUser().getStatus().getStatusMessage();
        
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
        
        client.changeStatus( Status.game( newStatus ) );
        log.info( "Changed bot status to " + newStatus );
        
    }
    
    /**
     * Sets the presence of the bot.
     * 
     * @param isIdle If true, the bot becomes idle.
     *               If false, becomes online.
     */
    public void setIdle( boolean isIdle ) {
        
        client.changePresence( isIdle );
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
     * @param url File of the image.
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

}
