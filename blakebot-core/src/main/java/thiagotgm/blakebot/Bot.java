package thiagotgm.blakebot;

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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main bot runner that manages log in/out and bot state.
 * 
 * @author ThiagoTGM
 * @version 0.3
 * @since 2016-12-27
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger( Bot.class );

    private volatile IDiscordClient client;
    private Properties properties;
    private final AtomicBoolean reconnect;

    /**
     * Creates a new bot that uses a given login token.
     * 
     * @param token Login token to be used by the bot.
     */
    public Bot( Properties properties ) {

        this.properties = properties;
        this.reconnect = new AtomicBoolean( true );

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

        try {

            this.client
                    .changeAvatar( Image.forFile( new File( "Blake.png" ) ) ); // Changes
                                                                               // the
                                                                               // bot's
                                                                               // profile
                                                                               // picture
        } catch ( RateLimitException | DiscordException e ) { // An error
                                                              // occurred
            e.printStackTrace();

        }
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
     * Retrieves the properties of the bot.
     * 
     * @return The object that contains the properties of the bot.
     */
    public Properties getProperties() {
        
        return this.properties;
        
    }
    
    /**
     * Sets the username of the bot.
     * 
     * @param newName New username to be set.
     */
    public void setUsername( String newName ) {
        
        try {
            client.changeUsername( newName );
            log.debug( "Changed bot name to " + newName );
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
        log.debug( "Changed bot status to " + newStatus );
        
    }
    
    /**
     * Sets the presence of the bot.
     * 
     * @param isIdle If true, the bot becomes idle.
     *               If false, becomes online.
     */
    public void setIdle( boolean isIdle ) {
        
        client.changePresence( isIdle );
        log.debug( "Changed bot presence to " + ( isIdle ? "idle" : "online" )
                + "." );
        
    }

}
