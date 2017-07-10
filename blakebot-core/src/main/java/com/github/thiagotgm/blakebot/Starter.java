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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.console.ConsoleGUI;

/**
 * Starts up the bot and the control console.
 * 
 * @author ThiagoTGM
 * @version 2.0.1
 * @since 2016-12-28
 */
public class Starter {

    private static final String LOG_FILE = "BlakeBot";
    private static final String LOG_EXT = ".log";
    private static final String LOG_FOLDER = "logs";
    
    /**
     * On program startup, creates and starts a new instance of the bot with a
     * login key given as argument, and a console to manage it. The bot isn't
     * immediately connected to discord, it must be ordered to do so through the
     * console.
     * 
     * @param args Command line arguments. Must be only one, the bot login key.
     */
    public static void main( String[] args ) {

        // If there is an existing log file, (attempts to) archives it.
        if ( Files.exists( Paths.get( LOG_FILE + LOG_EXT ) ) ) {
            if ( !Files.isDirectory( Paths.get( LOG_FOLDER ) ) ) {
                try {
                    Files.createDirectory( Paths.get( LOG_FOLDER ) );
                } catch ( IOException e ) {
                    logFileError( "Could not create log archive directory.", e );
                }
            }
            int num = 0;
            while ( Files.exists( Paths.get( LOG_FOLDER, LOG_FILE + "-" + num + LOG_EXT ) ) ) {
                
                num++;
                
            }
            try {
                Files.move( Paths.get( LOG_FILE + LOG_EXT ), Paths.get( LOG_FOLDER,
                        LOG_FILE + "-" + num + LOG_EXT ) );
            } catch ( IOException e ) {
                logFileError( "Could not move log file.", e );
            }
        }
        
        final Logger log = LoggerFactory.getLogger( Starter.class );
        
        // Reads default properties.
        Properties defaults = new Properties();
        try {
            ClassLoader loader = Starter.class.getClassLoader();
            InputStream input = loader.getResourceAsStream( PropertyNames.DEFAULTS_FILE );
            defaults.loadFromXML( input );
            input.close();
            log.info( "Loaded default properties." );
        } catch ( IOException e ) {
            log.error( "Error reading default properties file.", e );
            System.exit( 2 );
        }

        // Reads properties
        Properties properties = new Properties( defaults );
        try {
            FileInputStream input = new FileInputStream( PropertyNames.PROPERTIES_FILE );
            properties.loadFromXML( input );
            input.close();
            log.info( "Loaded bot properties." );
        } catch ( FileNotFoundException e ) {
            log.error(
                    "Properties file not found. A new one will be created." );
        } catch ( IOException e ) {
            log.error( "Error reading properties file.", e );
            System.exit( 2 );
        }

        // Requests login token if none registered.
        if ( !properties.containsKey( PropertyNames.LOGIN_TOKEN ) ) {
            log.info( "No registered Key. Requesting key." );
            String key;
            do {

                key = JOptionPane
                        .showInputDialog( "Please enter bot login key." );
                if ( key == null ) {
                    log.debug( "Setup cancelled." );
                    System.exit( 0 );
                }
                key = key.trim();

            } while ( key.length() == 0 );
            log.debug( "Received key." );
            properties.setProperty( PropertyNames.LOGIN_TOKEN, key );
        }

        Bot.setProperties( properties );
        Bot.registerListener( ConsoleGUI.getInstance() );

    }
    
    /**
     * Logs an error that occurred while moving the log file (ie. before the
     * logger object is created).
     * 
     * @param message Message to be logged.
     * @param e Exception that was thrown.
     */
    private static void logFileError( String message, Exception e ) {
        
        Logger log = LoggerFactory.getLogger( Starter.class );
        log.error( message, e );
        log.error( "================================================" );
        
    }

}
