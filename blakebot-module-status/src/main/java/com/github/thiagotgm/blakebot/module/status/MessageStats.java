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

package com.github.thiagotgm.blakebot.module.status;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Keeps track of message-related statistics. In order to ensure count consistency without
 * creating bottlenecks due to synchronization locks, stat increments are only
 * <b>requested</b> upon receiving a message, and are applied one at a time by an independent
 * internal thread.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-13
 */
public abstract class MessageStats {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor( ( r ) -> {
        
        Thread thread = new Thread( r, "MessageStats Updater" );
        thread.setDaemon( true );
        return thread;
        
    });
    private static final Runnable PUBLIC_INCREMENT_TASK = () -> { incrementPublic(); };
    private static final Runnable PRIVATE_INCREMENT_TASK = () -> { incrementPrivate(); };
    
    private static volatile long publicCount = 0;
    private static volatile long privateCount = 0;
    
    /**
     * Increments the counter of public messages.
     */
    private static void incrementPublic() {
        
        synchronized ( PUBLIC_INCREMENT_TASK ) {
            
            publicCount++;
            
        }
        
    }
    
    /**
     * Increments the counter of private messages.
     */
    private static void incrementPrivate() {
        
        synchronized ( PRIVATE_INCREMENT_TASK ) {
            
            privateCount++;
            
        }
        
    }
    
    /**
     * Retrieves the amount of public messages received by the bot since the program started.
     *
     * @return The public message count.
     */
    public static long getPublicMessageCount() {
        
        return publicCount;
        
    }
    
    /**
     * Retrieves the amount of private messages received by the bot since the program started.
     *
     * @return The private message count.
     */
    public static long getPrivateMessageCount() {
        
        return privateCount;
        
    }

    /**
     * Upon receiving a message, determines if it is a private or public message and
     * updates the appropriate counter.
     *
     * @param event The event fired by the received message.
     */
    @EventSubscriber
    public static void countMessage( MessageReceivedEvent event ) {

        EXECUTOR.execute( event.getChannel().isPrivate() ? PRIVATE_INCREMENT_TASK :
                                                           PUBLIC_INCREMENT_TASK );
        
    }

}
