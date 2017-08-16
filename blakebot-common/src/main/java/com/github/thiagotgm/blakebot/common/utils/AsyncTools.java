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

package com.github.thiagotgm.blakebot.common.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Tools for making asynchronous execution algorithms.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-14
 */
public class AsyncTools {
    
    /**
     * Default amount of threads for created fixed-thread executors.
     * <p>
     * Twice the amount of available processing cores.
     */
    protected static final int DEFAULT_THREAD_AMOUNT = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * Creates a thread factory that creates threads in the given thread group, with
     * the given exception handler.
     * <p>
     * The names of the created threads are the name of the group followed by the
     * number of the thread.
     *
     * @param group The thread group that built threads should be a part of.
     * @param handler The handler for uncaught exceptions.
     * @param daemon Whether the created threads should be daemon threads.
     * @return A thread factory that creates threads with the given settings.
     */
    public static ThreadFactory createThreadFactory( ThreadGroup group,
            UncaughtExceptionHandler handler, boolean daemon ) {
        
        return new NumberedThreadFactory( group, handler, daemon );
        
    }
    
    /**
     * Implementation of ThreadFactory that creates threads that belong to a thread
     * group specified on construction, whose name are the name of the thread group
     * appended by the number of the built thread.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-14
     */
    private static class NumberedThreadFactory implements ThreadFactory {
        
        private final ThreadGroup group;
        private final UncaughtExceptionHandler handler; 
        private final boolean daemon;
        
        private volatile int threadNum; 
        
        /**
         * Constructs a factory that creates threads in the given thread group, with
         * the given exception handler.
         *
         * @param group The thread group that built threads should be a part of.
         * @param handler The handler for uncaught exceptions.
         * @param daemon Whether the created threads should be daemon threads.
         */
        public NumberedThreadFactory( ThreadGroup group, UncaughtExceptionHandler handler,
                boolean daemon ) {
            
            this.group = group;
            this.handler = handler;
            this.daemon = daemon;
            
            this.threadNum = 0;
            
        }
        
        @Override
        public Thread newThread( Runnable r ) {

            String name = String.format( "%s-%d", group.getName(), threadNum++ );
            Thread thread = new Thread( group, r, name );
            thread.setUncaughtExceptionHandler( handler );
            thread.setDaemon( daemon );
            return thread;
            
        }
        
    }
    
    /**
     * Creates an executor that uses a fixed amount of threads, where the threads
     * used have the given settings.
     *
     * @param nThreads The amount of threads to use.
     * @param group The group to place the threads in.
     * @param handler The handler to run if an uncaught exception happens.
     * @param daemon Whether the threads to be used should be daemon threads.
     * @return The executor with the given parameters.
     * @see Executors#newFixedThreadPool(int)
     */
    public static ExecutorService createFixedThreadPool( int nThreads, ThreadGroup group,
            UncaughtExceptionHandler handler, boolean daemon ) {
        
        ThreadFactory factory = createThreadFactory( group, handler, daemon );
        return Executors.newFixedThreadPool( nThreads, factory );
        
    }
    
    /**
     * Creates an executor that uses the {@link #DEFAULT_THREAD_AMOUNT default amount of threads},
     * where the threads used have the given settings.
     *
     * @param group The group to place the threads in.
     * @param handler The handler to run if an uncaught exception happens.
     * @param daemon Whether the threads to be used should be daemon threads.
     * @return The executor with the given parameters.
     * @see Executors#newFixedThreadPool(int)
     */
    public static ExecutorService createFixedThreadPool( ThreadGroup group,
            UncaughtExceptionHandler handler, boolean daemon ) {
        
        return createFixedThreadPool( DEFAULT_THREAD_AMOUNT, group, handler, daemon );
        
    }
    
    /**
     * Creates an executor that uses a fixed amount of threads, where the threads
     * used have the given settings and are daemon threads.
     *
     * @param nThreads The amount of threads to use.
     * @param group The group to place the threads in.
     * @param handler The handler to run if an uncaught exception happens.
     * @return The executor with the given parameters.
     * @see Executors#newFixedThreadPool(int)
     */
    public static ExecutorService createFixedThreadPool( int nThreads, ThreadGroup group,
            UncaughtExceptionHandler handler ) {
        
        return createFixedThreadPool( nThreads, group, handler, true );
        
    }
    
    /**
     * Creates an executor that uses the {@link #DEFAULT_THREAD_AMOUNT default amount of threads},
     * where the threads used have the given settings
     * and are daemon threads.
     *
     * @param group The group to place the threads in.
     * @param handler The handler to run if an uncaught exception happens.
     * @return The executor with the given parameters.
     * @see Executors#newFixedThreadPool(int)
     */
    public static ExecutorService createFixedThreadPool( ThreadGroup group,
            UncaughtExceptionHandler handler ) {
        
        return createFixedThreadPool( DEFAULT_THREAD_AMOUNT, group, handler, true );
        
    }

}
