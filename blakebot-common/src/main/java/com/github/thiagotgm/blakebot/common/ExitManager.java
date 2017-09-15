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

package com.github.thiagotgm.blakebot.common;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.utils.AsyncTools;

/**
 * Utility class that terminates the program, but only after all the registered listeners have
 * finished their cleanup tasks.
 * <p>
 * Thus, provides a way to ensure that some objects that need to do cleanup BEFORE the program
 * terminates are able to do so.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-29
 */
public class ExitManager {
    
    private static final Logger LOG = LoggerFactory.getLogger( ExitManager.class );
    private static final ThreadGroup THREADS = new ThreadGroup( "ExitManager Queue Handler" );
    private static final ExecutorService EXECUTOR = AsyncTools.createFixedThreadPool( THREADS, ( t, e ) -> {
                
                LOG.error( "Uncaught exception thrown while processing exit queue.", e );
                
            });
    
    private static final List<ExitListener> LISTENERS = new LinkedList<>();
    
    /**
     * Registers a listener to be called before exiting.
     * <p>
     * The {@link ExitListener#handle()} method of the listener is guaranteed to be able
     * to complete execution before the program is terminated.
     *
     * @param listener The listener to be registered.
     */
    public synchronized static void registerListener( ExitListener listener ) {
        
        LISTENERS.add( listener );
        
    }
    
    /**
     * Unregister a listener.
     *
     * @param listener The listener to be unregistered.
     */
    public synchronized static void unregisterListener( ExitListener listener ) {
        
        LISTENERS.remove( listener );
        
    }
    
    /**
     * Executes listener tasks then terminates the program.
     */
    public synchronized static void exit() {
        
        LOG.info( "Exit request received." );
        
        /* Executes exit queue */
        List<Callable<Object>> tasks = new LinkedList<>();
        for ( ExitListener listener : LISTENERS ) { // Build queue.
            
            tasks.add( Executors.callable( () -> {
                
                listener.handle(); // Execute the listener.
                
            }) );
            
        }
        
        try {
            EXECUTOR.invokeAll( tasks ); // Execute and wait for queue.
        } catch ( InterruptedException e ) {
            LOG.error( "Exit queue interrupted. Aborting.", e );
            return;
        }
        LOG.debug( "Exit queue finished." );
        
        System.exit( 0 ); // Close program.
        
    }
    
    /**
     * Used to represent a class that must perform cleanup tasks before the program is terminated.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-11
     */
    public static interface ExitListener {
        
        /**
         * Performs any necessary tasks before the bot program is shut down.
         */
        void handle();
        
    }

}
