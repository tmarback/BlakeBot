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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Executor service where a key is provided along with the task to be submitted,
 * and all tasks that are submitted with the same key (as per {@link Object#equals(Object)})
 * are guaranteed to be executed synchronously in relation to each other (e.g. two tasks
 * submitted with the same key will never be executed at the same time).
 * Tasks with different keys may be ran sequentially in the same thread or
 * parallelized across multiple threads, at the implementation's discretion.
 * <p>
 * <tt>null</tt> is a valid request key. Using the submission methods that
 * do not take a key (the ones specified in {@link ExecutorService}) is
 * the same as using <tt>null</tt> as a key.
 * <p>
 * If all requests are submitted using the same key, any implementation of
 * this type functions the same as a single-threaded executor. However,
 * there is no guarantee that the load will be equally spread among the
 * pool when the keys are different.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-04
 */
public interface KeyedExecutorService extends ExecutorService {
	
	/**
	 * Executes the given command at some time in the future, in a thread specified
	 * by the given key.
	 * 
	 * @param key The key to determine what thread to run with.
	 * @param command The command to execute.
	 * @throws RejectedExecutionException if this task cannot be accepted for execution.
	 * @throws NullPointerException if command is null.
	 */
	void execute( Object key, Runnable command )
			throws RejectedExecutionException, NullPointerException;
	
	/**
	 * Submits a value-returning task for execution and returns a Future representing the
	 * pending results of the task.<br>
	 * The task is executed in a thread specified by the given key.
	 * 
	 * @param <T> The type of the task's result.
	 * @param key The key to determine what thread to run with.
	 * @param task The task to submit.
	 * @return A Future representing pending completion of the task.
	 * @throws RejectedExecutionException if this task cannot be scheduled for execution.
	 * @throws NullPointerException if command is null.
	 * @see ExecutorService#submit(Callable)
	 */
	<T> Future<T> submit( Object key, Callable<T> task )
			throws RejectedExecutionException, NullPointerException;

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task.
	 * The Future's get method will return <tt>null</tt> upon successful completion.<br>
	 * The task is executed in a thread specified by the given key.
	 * 
	 * @param key The key to determine what thread to run with.
	 * @param task The task to submit.
	 * @return A Future representing pending completion of the task.
	 * @throws RejectedExecutionException if this task cannot be scheduled for execution.
	 * @throws NullPointerException if command is null.
	 */
	Future<?> submit( Object key, Runnable task )
			throws RejectedExecutionException, NullPointerException;
	
	/**
	 * Submits a Runnable task for execution and returns a Future representing that task.
	 * The Future's <tt>get</tt> method will return the given result upon successful completion.<br>
	 * The task is executed in a thread specified by the given key.
	 * 
	 * @param <T> The type of the result.
	 * @param key The key to determine what thread to run with.
	 * @param task The task to submit.
	 * @param result The result to return
	 * @return A Future representing pending completion of the task.
	 * @throws RejectedExecutionException if this task cannot be scheduled for execution.
	 * @throws NullPointerException if command is null.
	 */
	<T> Future<T> submit( Object key, Runnable task, T result )
			throws RejectedExecutionException, NullPointerException;
	
	/* Overrides for keyless ExecutorService methods */
	
	@Override
	default void execute( Runnable command )
			throws RejectedExecutionException, NullPointerException {
		
		execute( null, command );

	}
	
	@Override
	default <T> Future<T> submit( Callable<T> task )
			throws RejectedExecutionException, NullPointerException {
		
		return submit( (Object) null, task );
		
	}
	
	@Override
	default Future<?> submit( Runnable task )
			throws RejectedExecutionException, NullPointerException {
		
		return submit( (Object) null, task );
		
	}
	
	@Override
	default <T> Future<T> submit( Runnable task, T result )
			throws RejectedExecutionException, NullPointerException {
		
		return submit( null, task, result );
		
	}

}
