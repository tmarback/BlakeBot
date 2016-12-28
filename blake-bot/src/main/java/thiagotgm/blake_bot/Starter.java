package thiagotgm.blake_bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.DiscordException;

/**
 * Starts up the bot.
 * 
 * @author ThiagoTGM
 * @version 0.5
 * @since 2016-12-28
 */
public class Starter {

    private static final Logger log = LoggerFactory.getLogger( Starter.class );

    public static void main( String[] args ) {
        
        Bot bot;
        if ( args.length == 1 ) {
            bot = new Bot( args[0] );
        } else {
            throw new IllegalArgumentException( "Please enter token as argument" );
        }
        try {
            bot.login();
        } catch ( DiscordException e ) {
            log.warn( "Bot could not start", e );
        }
        
    }

}
