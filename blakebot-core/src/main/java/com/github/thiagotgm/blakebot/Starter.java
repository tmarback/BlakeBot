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

import java.io.File;
import java.util.Arrays;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.console.ConsoleGUI;
import com.github.thiagotgm.blakebot.settings.Settings;

/**
 * Starts up the bot and the control console.
 * 
 * @author ThiagoTGM
 * @version 2.0.1
 * @since 2016-12-28
 */
public class Starter {

    private static final String LOG_EXT = ".log";
    private static final File WORKING_DIR = new File( "." );
    private static final File LOG_DIR = new File( "logs" );
    private static final String LOG_SUBDIR_NAME = "BlakeBot-%d";
    private static final int LOG_FILE_ERROR = 5;
    
    /**
     * On program startup, creates and starts a new instance of the bot, and a
     * console to manage it. The bot isn't immediately connected to discord, it
     * must be ordered to do so through the console.
     * 
     * @param args Command line arguments.
     */
    public static void main( String[] args ) {

        /* Get log files */
        File[] logFiles = WORKING_DIR.listFiles( ( dir, filename ) -> {
            
            return filename.endsWith( LOG_EXT );
            
        });
        /* If there are existing log files, (attempts to) archive them */
        if ( !Arrays.asList( logFiles ).isEmpty() ) {
            if ( !LOG_DIR.exists() ) {
                if ( !LOG_DIR.mkdir() ) {
                    System.err.println( "Could not create log archive directory." );
                    System.exit( LOG_FILE_ERROR );
                }
            } else if ( LOG_DIR.isFile() ) {
                System.err.println( "Log archive directory name taken by file." );
                System.exit( LOG_FILE_ERROR );
            }
            
            /* Get subdir to place files in */
            int num = -1;
            File nextSubDir;
            do { // Find free number.
                
                num++;
                nextSubDir = new File( LOG_DIR, String.format( LOG_SUBDIR_NAME, num ) );
                
            } while ( nextSubDir.exists() );
            if ( !nextSubDir.mkdir() ) { // Create subdirectory.
                System.err.println( "Could not create log archive subdirectory." );
                System.exit( LOG_FILE_ERROR );
            }
            
            /* Move log files to subdirectory */
            boolean error = false;
            for ( File logFile : logFiles ) {
                
                System.out.println( "Moving log file " + logFile.getName() + "." );
                if ( !logFile.renameTo( new File( nextSubDir, logFile.getName() ) ) ) {
                    System.err.println( "Could not move log file." );
                    error = true;
                }
                
            }
            if ( error ) { // An error was encountered while moving some files.
                System.exit( LOG_FILE_ERROR );
            }
        }
        
        final Logger log = LoggerFactory.getLogger( Starter.class );

        /* Requests login token if none registered */
        if ( !Settings.hasSetting( Bot.LOGIN_TOKEN_SETTING ) ) {
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
            Settings.setSetting( Bot.LOGIN_TOKEN_SETTING, key );
        }

        ConsoleGUI.getInstance().setVisible( true ); // Start and show console.
        log.info( "Console started." );

    }

}
