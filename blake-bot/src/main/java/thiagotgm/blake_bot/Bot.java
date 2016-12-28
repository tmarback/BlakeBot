package thiagotgm.blake_bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class Bot {
    
    private static final Logger log = LoggerFactory.getLogger( Bot.class );

    private volatile IDiscordClient client;
    private String token;
    private final AtomicBoolean reconnect = new AtomicBoolean( true );

    public Bot( String token ) {
        
        this.token = token;
        
    }

    public void login() throws DiscordException {
        
        client = new ClientBuilder().withToken( token ).login();
        client.getDispatcher().registerListener( this );
        
    }

    @EventSubscriber
    public void onReady( ReadyEvent event ) {
        
        try {

            this.client.changeUsername( "BlakeBot" ); // Changes the bot's username
            //this.client.changeAvatar(Image.forFile(new File("picture.png"))); // Changes the bot's profile picture
            this.client.changePresence(true); // Changes the bot's presence to idle
            this.client.changeStatus( Status.game( "Bot Dev" ) ); // Changes the bot's status

        } catch (RateLimitException | DiscordException e) { // An error occurred

            e.printStackTrace();

        }
        log.info( "*** Discord bot armed ***" );
        
    }

    @EventSubscriber
    public void onDisconnect( DisconnectedEvent event ) {
        
        CompletableFuture.runAsync( new Runnable() {
            
            @Override
            public void run() {
                
                if (reconnect.get()) {
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

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        
        log.debug("Got message");
        
    }

    public void terminate() {
        
        reconnect.set(false);
        try {
            client.logout();
        } catch ( DiscordException e ) {
            log.warn( "Logout failed", e );
        }
        
    }

}
