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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Executor that functions similarly to a {@link ThreadPoolExecutor} in that
 * submitted tasks are executed in one of multiple threads, but instead
 * of executing each task in an arbitrary thread of the pool, a key is
 * provided when submitting a task that defines which thread will execute
 * that task (based on the {@link Object#hashCode() hash code} of the key).
 * Multiple tasks submitted using the same key are guaranteed to be executed
 * by the same thread (and thus one at at time), which allows the use of
 * parallelization for executing a number of tasks while ensuring that certain
 * groups of these tasks are synchronized as necessary.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-04
 */
public class KeyedThreadPoolExecutor extends AbstractExecutorService implements KeyedExecutorService {
	
	private final ExecutorService[] executors;
	
	/**
	 * Initializes an executor with the given amount of threads obtained from
	 * the given factory.
	 * 
	 * @param nThreads The number of threads that the executor should use.
	 * @param threadFactory The factory to obtain threads from.
	 * @throws IllegalArgumentException if <tt>nThreads <= 0</tt>.
	 */
	public KeyedThreadPoolExecutor( int nThreads, ThreadFactory threadFactory )
			throws IllegalArgumentException {
		
		if ( nThreads <= 0 ) {
			throw new IllegalArgumentException( "Invalid number of threads." );
		}
		
		executors = new ExecutorService[nThreads];
		for ( int i = 0; i < nThreads; i++ ) {
			
			executors[i] = Executors.newSingleThreadExecutor( threadFactory );
			executors[i].execute( () -> { return; } ); // Create numbered thread;
			
		}
		
	}

	@Override
	public void shutdown() {
		
		for ( ExecutorService executor : executors ) {
			
			executor.shutdown(); // Shutdown all executors.
			
		}

	}

	@Override
	public List<Runnable> shutdownNow() {

		List<Runnable> pending = new LinkedList<>();
		for ( ExecutorService executor : executors ) {
			
			pending.addAll( executor.shutdownNow() ); // Shutdown all executors.
			
		}
		return pending;
		
	}

	@Override
	public boolean isShutdown() {

		return executors[0].isShutdown(); // If one is, they all are.
		
	}

	@Override
	public boolean isTerminated() {

		for ( ExecutorService executor : executors ) { // Check all executors.
			
			if ( !executor.isTerminated() ) {
				return false; // Found an executor that is not terminated.
			}
			
		}
		return true;
		
	}

	@Override
	public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException {

		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.submit( () -> {
			
			for ( ExecutorService executor : executors ) { // Wait for each executor.
				
				executor.awaitTermination( timeout, unit );
				
			}
			return true;
			
		});
		exec.shutdown();
		return exec.awaitTermination( timeout, unit );
		
	}
	
	/* Keyed submission methods */
	
	/**
	 * Retrieves the executor to use for the given key.
	 * 
	 * @param key The request key.
	 * @return The executor to use.
	 */
	private ExecutorService getExecutor( Object key ) {
		
		int idx = key == null ? 0 : ( key.hashCode() % executors.length );
		return executors[idx];
		
	}
	
	@Override
	public void execute( Object key, Runnable command )
			throws RejectedExecutionException, NullPointerException {
		
		getExecutor( key ).execute( command );

	}
	
	@Override
	public <T> Future<T> submit( Object key, Callable<T> task )
			throws RejectedExecutionException, NullPointerException {
		
		return getExecutor( key ).submit( task );
		
	}

	@Override
	public Future<?> submit( Object key, Runnable task )
			throws RejectedExecutionException, NullPointerException {
		
		return getExecutor( key ).submit( task );
		
	}
	
	@Override
	public <T> Future<T> submit( Object key, Runnable task, T result )
			throws RejectedExecutionException, NullPointerException {
		
		return getExecutor( key ).submit( task, result );
		
	}

}
