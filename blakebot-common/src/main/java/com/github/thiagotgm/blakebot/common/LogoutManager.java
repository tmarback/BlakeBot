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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.event.LogoutEvent;
import com.github.thiagotgm.blakebot.common.event.LogoutFailureEvent;
import com.github.thiagotgm.blakebot.common.event.LogoutRequestedEvent;
import com.github.thiagotgm.blakebot.common.event.LogoutSuccessEvent;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.DiscordException;

/**
 * Logout manager that logs out a client after receiving a {@link LogoutEvent} from it, but only
 * after all the registered listeners have finished processing the logout event.
 * <p>
 * Thus, provides a way to ensure that some objects that need to do cleanup BEFORE the connection
 * is cut are able to do so.
 * <p>
 * Objects that need to do cleanup but don't need the connection should be registered directly to
 * the client's dispatcher.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-29
 */
public class LogoutManager implements IListener<LogoutRequestedEvent> {
    
    private static final Map<IDiscordClient, LogoutManager> managers = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger( LogoutManager.class );
    private static final ThreadGroup threads = new ThreadGroup( "LogoutManager Queue Handler" );
    private static final ExecutorService executor = AsyncTools.createFixedThreadPool( threads, ( t, e ) -> {
                
                LOG.error( "Uncaught exception thrown while processing logout queue.", e );
                
            });
    
    private final IDiscordClient client;
    private final List<IListener<LogoutRequestedEvent>> listeners;
    
    /**
     * Constructs a new manager for the given client.
     * <p>
     * The manager is already registered to the client's dispatcher.
     *
     * @param client The client to manage logout for.
     */
    private LogoutManager( IDiscordClient client ) {
        
        this.client = client;
        listeners = new LinkedList<>();
        
        client.getDispatcher().registerListener( this );
        
    }
    
    /**
     * Retrieves the manager for the given client.
     *
     * @param client The client to get a manager for.
     * @return The manager.
     */
    public synchronized static LogoutManager getManager( IDiscordClient client ) {
        
        if ( !managers.containsKey( client ) ) {
            managers.put( client, new LogoutManager( client ) );
        }
        return managers.get( client );
        
    }
    
    /**
     * Registers a listener to be called before logging out.
     * <p>
     * The {@link IListener#handle(sx.blah.discord.api.events.Event) handle} method of the
     * listener is guaranteed to be able to complete execution before the bot is logged out.
     *
     * @param listener The listener to be registered.
     */
    public synchronized void registerListener( IListener<LogoutRequestedEvent> listener ) {
        
        listeners.add( listener );
        
    }
    
    /**
     * Unregister a listener.
     *
     * @param listener The listener to be unregistered.
     */
    public synchronized void unregisterListener( IListener<LogoutRequestedEvent> listener ) {
        
        listeners.remove( listener );
        
    }

    /**
     * On logout request, executes listener tasks then attempts to log out.
     *
     * @param event The event fired.
     */
    @Override
    public synchronized void handle( LogoutRequestedEvent event ) {

        LOG.info( "Logout request received." );
        
        /* Executes logout queue */
        List<Callable<Object>> tasks = new LinkedList<>();
        for ( IListener<LogoutRequestedEvent> listener : listeners ) { // Build queue.
            
            tasks.add( Executors.callable( () -> {
            
                listener.handle( event ); // Execute the listener.
                
            }) );
            
        }
        EventDispatcher dispatcher = client.getDispatcher(); // Get dispatcher for the result event.
        try {
            executor.invokeAll( tasks ); // Execute and wait for queue.
        } catch ( InterruptedException e ) {
            LOG.error( "Logout queue interrupted.", e );
            dispatcher.dispatch( // Dispatch failure event.
                    new LogoutFailureEvent( client, LogoutFailureEvent.Reason.QUEUE_INTERRUPTED ) );
            return;
        }
        LOG.debug( "Logout queue finished." );
        
        /* Attempt disconnect */
        try {
            client.logout();
            LOG.info( "===[ Bot LOGGED OUT! ]===" );
            dispatcher.dispatch( new LogoutSuccessEvent( client ) ); // Dispatch success event.
        } catch ( DiscordException e ) {
            LOG.error( "Logout failed", e );
            dispatcher.dispatch( // Dispatch failure event.
                    new LogoutFailureEvent( client, LogoutFailureEvent.Reason.LOGOUT_FAILED ) );
        }
        
    }
    
    

}
