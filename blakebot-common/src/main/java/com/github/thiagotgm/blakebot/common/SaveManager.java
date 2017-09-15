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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.utils.AsyncTools;

/**
 * Class that manages objects that save their state to disk. All registered listeners are =
 * auto-saved every time a certain time delay passes, and right before the program is terminated.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class SaveManager implements ExitManager.ExitListener {

    private static final Logger LOG = LoggerFactory.getLogger( SaveManager.class );
    private static final String DELAY_SETTING = "Auto-save delay";
    
    /**
     * Minimum time, in minutes, that can be set for the delay between auto-saves.
     */
    public static final long MIN_DELAY = 10;
    
    private static final List<Saveable> LISTENERS = new LinkedList<>();
    private static final ThreadGroup THREADS = new ThreadGroup( "Auto-save Scheduler" );
    private static final ScheduledExecutorService EXECUTOR =
            AsyncTools.createScheduledThreadPool( 1, THREADS, ( t, e ) -> {
                
                LOG.error( "Uncaught exception thrown while autosaving.", e );
                
            });
    private static final Runnable AUTO_SAVE = () -> {
        
        LOG.info( "Auto-saving." );
        save();
        
    };
    private static ScheduledFuture<?> currentTask;
    
    static {
        // Schedule initial autosave.
        long delay = Settings.getLongSetting( DELAY_SETTING );
        setAutoSave( delay );
        
    }
    
    /**
     * Sets up the auto-save task.
     * <p>
     * If the given delay is smaller than {@value #MIN_DELAY}, the auto-save will be
     * disabled.
     *
     * @param delay The time between auto-saves, in minutes.
     */
    protected static void setAutoSave( long delay ) {
        
        if ( currentTask != null ) { // Stops current task if any.
            currentTask.cancel( false );
        }
        
        if ( delay >= MIN_DELAY ) { // Set new task.
            currentTask = EXECUTOR.scheduleAtFixedRate( AUTO_SAVE, delay, delay, TimeUnit.MINUTES );
            LOG.info( "Auto-save delay set to {} minutes.", delay );
        } else { // Disable autosave.
            currentTask = null;
            LOG.info( "Auto-save disabled." );
        }
        
    }
    
    /**
     * Sets the time delay between auto-saves.
     * <p>
     * If the given delay is smaller than {@value #MIN_DELAY}, the auto-save will be
     * disabled.
     *
     * @param delay The time between auto-saves, in minutes.
     */
    public static void setAutoSaveDelay( long delay ) {
        
        setAutoSave( delay );
        Settings.setSetting( DELAY_SETTING, delay );
        
    }
    
    /**
     * Registers a listener to be called when a save event happens.
     *
     * @param listener The listener to be registered.
     */
    public synchronized static void registerListener( Saveable listener ) {
        
        LISTENERS.add( listener );
        
    }
    
    /**
     * Unregister a listener.
     *
     * @param listener The listener to be unregistered.
     */
    public synchronized static void unregisterListener( Saveable listener ) {
        
        LISTENERS.remove( listener );
        
    }
    
    /**
     * Saves all registered listeners.
     */
    public synchronized static void save() {
        
        LOG.debug( "Saving all listeners..." );
        for ( Saveable listener : LISTENERS ) { // Save each listener.
            
            listener.save();
            
        }
        LOG.debug( "Save completed." );
        
    }
    
    static {
        // Register an instance with the ExitManager for pre-exit save.
        ExitManager.registerListener( new SaveManager() );
        
    }
    
    /**
     * Instantiates a SaveManager.
     */
    private SaveManager() {}
    
    /**
     * Saves all registered listeners before the program shuts down.
     */
    @Override
    public void handle() {

        LOG.info( "Received program end signal. Stopping auto-save." );
        if ( currentTask != null ) { // Stop auto-save task.
            currentTask.cancel( false );
            currentTask = null;
        }
        LOG.info( "Performing final save." );
        save();
        
    }
    
    /**
     * An object that can have its state saved.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-11
     */
    public static interface Saveable {
        
        /**
         * Saves the object state.
         */
        void save();
        
    }

}
