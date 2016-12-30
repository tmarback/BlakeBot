package thiagotgm.blakebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.DiscordException;
import thiagotgm.blakebot.console.ConsoleGUI;

/**
 * Starts up the bot and the control console.
 * 
 * @author ThiagoTGM
 * @version 0.5
 * @since 2016-12-28
 */
public class Starter {

    private static final Logger log = LoggerFactory.getLogger( Starter.class );

    /**
     * On program startup, creates and starts a new instance of the bot
     * with a login key given as argument, and a console to manage it.
     * The bot isn't immediately connected to discord, it must be ordered
     * to do so through the console.
     * 
     * @param args Command line arguments. Must be only one, the bot login
     *             key.
     */
    public static void main( String[] args ) {
        
        Bot bot;
        if ( args.length == 1 ) {
            bot = new Bot( args[0] );
        } else {
            throw new IllegalArgumentException( "Please enter token as argument" );
        }
        new ConsoleGUI( bot );
        
    }

}
