package com.github.thiagotgm.blakebot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.console.ConsoleGUI;

/**
 * Starts up the bot and the control console.
 * 
 * @author ThiagoTGM
 * @version 1.0
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
        
        // Reads default properties.
        Properties defaults = new Properties();
        try {
             FileInputStream input = new FileInputStream( PropertyNames.DEFAULTS_FILE );
             defaults.loadFromXML( input );
             log.info( "Loaded default properties." );
        } catch ( FileNotFoundException e ) {
            log.error( "Default properties file not found.", e );
            System.exit( 1 );
        } catch ( IOException e ) {
            log.error( "Error reading default properties file.", e );
            System.exit( 2 );
        }
        
        // Reads properties
        Properties properties = new Properties( defaults );
        try {
            FileInputStream input = new FileInputStream( PropertyNames.PROPERTIES_FILE );
            properties.loadFromXML( input );
            log.info( "Loaded bot properties." );
        } catch ( FileNotFoundException e ) {
            log.error( "Properties file not found. A new one will be created." );
        } catch ( IOException e ) {
            log.error( "Error reading properties file.", e );
            System.exit( 2 );
        }
        
        // Requests login token if none registered.
        if ( !properties.containsKey( PropertyNames.LOGIN_TOKEN ) ) {
            log.info( "No registered Key. Requesting key." );
            String key;
            do {

                key = JOptionPane.showInputDialog( "Please enter bot login key." );
                if ( key == null ) {
                    log.debug( "Setup cancelled." );
                    System.exit( 0 );
                }
                key = key.trim();
                
            } while ( key.length() == 0 );
            log.debug( "Received key." );
            properties.setProperty( PropertyNames.LOGIN_TOKEN, key );
        }
        
        Bot bot;
        bot = new Bot( properties );
        bot.registerListener( new ConsoleGUI( bot ) );
        
    }

}
