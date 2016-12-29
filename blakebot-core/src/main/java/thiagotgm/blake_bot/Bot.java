package thiagotgm.blake_bot;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main bot runner that manages log in/out and bot state.
 * 
 * @author ThiagoTGM
 * @version 0.2
 * @since 2016-12-27
 */
public class Bot {

    private static final Logger log = LoggerFactory.getLogger( Bot.class );

    private volatile IDiscordClient client;
    private String token;
    private final AtomicBoolean reconnect;  
    private boolean connected;

    /**
     * Creates a new bot that uses a given login token.
     * 
     * @param token Login token to be used by the bot.
     */
    public Bot( String token ) {

        this.token = token;
        this.reconnect = new AtomicBoolean( true );
        this.connected = false;

    }

    /**
     * Logs in to Discord.
     */
    public void login() throws DiscordException {

        client = new ClientBuilder().withToken( token ).login();
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

            this.client.changeUsername( "BlakeBot" ); // Changes the bot's
                                                      // username
            this.client
                    .changeAvatar( Image.forFile( new File( "Blake.png" ) ) ); // Changes
                                                                               // the
                                                                               // bot's
                                                                               // profile
                                                                               // picture
            this.client.changePresence( true ); // Changes the bot's presence to
                                                // idle
            this.client.changeStatus( Status.game( "Bot Dev" ) ); // Changes the
                                                                  // bot's
                                                                  // status
        } catch ( RateLimitException | DiscordException e ) { // An error
                                                              // occurred
            e.printStackTrace();

        }
        connected = true;
        log.info( "=== Bot READY! ===" );

    }

    /**
     * Method triggered when the bot is diconnected from Discord.
     * 
     * @param event Event fired.
     */
    @EventSubscriber
    public void onDisconnect( DisconnectedEvent event ) {

        connected = false;
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
            terminate();
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
    public void terminate() {

        reconnect.set( false );
        try {
            client.logout();
            log.info( "=== Bot terminated ===" );
        } catch ( DiscordException e ) {
            log.warn( "Logout failed", e );
        }

    }
    
    /**
     * Retrieves whether the bot is currently connected to Discord.
     * 
     * @return true if the bot is currently connected.
     *         false otherwise.
     */
    public boolean isConnected() {
        
        return this.connected;
        
    }

}
