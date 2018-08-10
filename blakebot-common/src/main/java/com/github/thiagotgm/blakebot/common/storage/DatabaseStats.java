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

package com.github.thiagotgm.blakebot.common.storage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores stats related to dabase accessing.
 * <p>
 * Note: A "successful" database fetch is when a value mapped to the desired key
 * is found, while a "failed" fetch is when a value is not found (but the fetch was
 * still executed successfully). Does not include cache operations.
 * <p>
 * Does not count operations other than a "get" (so operations like containsKey
 * would not be counted).
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-09
 */
public class DatabaseStats {
	
	private static final AtomicLong cacheHits = new AtomicLong();
	private static final AtomicLong cacheMisses = new AtomicLong();
	private static final AtomicLong dbFetchSuccessTimeTotal = new AtomicLong();
	private static final AtomicLong dbFetchSuccesses = new AtomicLong();
	private static final AtomicLong dbFetchFailTimeTotal = new AtomicLong();
	private static final AtomicLong dbFetchFailures = new AtomicLong();
	
	/**
	 * Records a hit to a database cache.
	 */
	public static void addCacheHit() {
		
		cacheHits.incrementAndGet();
		
	}
	
	/**
	 * Records a miss to a database cache.
	 */
	public static void addCacheMiss() {
		
		cacheMisses.incrementAndGet();
		
	}
	
	/**
	 * Records a successful fetch to the database.
	 * 
	 * @param time The time elapsed to perform the fetch operation,
	 *             in milliseconds.
	 */
	public static synchronized void addDbFetchSuccess( long time ) {
		
		dbFetchSuccessTimeTotal.addAndGet( time );
		dbFetchSuccesses.incrementAndGet();
		
	}
	
	/**
	 * Records a failed fetch to the database.
	 * 
	 * @param time The time elapsed to perform the fetch operation,
	 *             in milliseconds.
	 */
	public static synchronized void addDbFetchFailure( long time ) {
		
		dbFetchFailTimeTotal.addAndGet( time );
		dbFetchFailures.incrementAndGet();
		
	}
	
	/**
	 * Retrieves the amount of cache hits so far.
	 * 
	 * @return The amount of cache hits since the program started.
	 */
	public static long getCacheHits() {
		
		return cacheHits.get();
		
	}
	
	/**
	 * Retrieves the amount of cache misses so far.
	 * 
	 * @return The amount of cache misses since the program started.
	 */
	public static long getCacheMisses() {
		
		return cacheMisses.get();
		
	}
	
	/**
	 * Retrieves the average time of a successful database fetch so far.
	 * 
	 * @return The average time of a successful database fetch, in milliseconds,
	 *         or -1 if there have not been any successful fetches yet.
	 */
	public static synchronized long getAverageFetchSuccessTime() {
		
		if ( dbFetchSuccesses.get() > 0 ) { // At least one success.
			return dbFetchSuccessTimeTotal.get() / dbFetchSuccesses.get();
		} else {
			return -1;
		}
		
	}
	
	/**
	 * Retrieves the average time of a failed database fetch so far.
	 * 
	 * @return The average time of a failed database fetch, in milliseconds,
	 *         or -1 if there have not been any failed fetches yet.
	 */
	public static synchronized long getAverageFetchFailTime() {
		
		if ( dbFetchFailures.get() > 0 ) { // At least one failure.
			return dbFetchFailTimeTotal.get() / dbFetchFailures.get();
		} else {
			return -1;
		}
		
	}

}
