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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.impl.XMLDatabase;

/**
 * Class that manages the bot's database system.
 * <p>
 * The database must be initialized using {@link #startup()} when the bot is
 * initialized, and must be terminated using {@link #shutdown()} before ending
 * the program.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-08
 */
public class DatabaseManager {
	
	private static final Logger LOG = LoggerFactory.getLogger( DatabaseManager.class );
	
	private static Database db = null;
	
	/**
	 * Starts up and loads the database.
	 * 
	 * @throws IllegalStateException if the database is currently running.
	 */
	public static boolean startup() throws IllegalStateException {
		
		if ( db != null ) {
			throw new IllegalStateException( "Database currently running." );
		}
		
		LOG.info( "Starting database." );
		
		// TODO: implement this properly
		
		db = new XMLDatabase();
		boolean result = db.load( Arrays.asList( new String[] { "data" } ) );
		
		if ( result ) {
			LOG.info( "Database started." );
		} else {
			LOG.error( "Could not start database." );
		}
		return result;
		
	}
	
	/**
	 * Obtains the database.
	 * 
	 * @return The database to use for storage.
	 * @throws IllegalStateException if the database is not currently running.
	 */
	public static Database getDatabase() throws IllegalStateException {
		
		if ( db == null ) {
			throw new IllegalStateException( "Database not currently running." );
		}
		return db;
		
	}
	
	/**
	 * Terminates the database, preventing further changes and ensuring all data is properly
	 * stored persistently.
	 * <p>
	 * If a database change was requested, it will be performed before the database closes.
	 * 
	 * @return The database to use for storage.
	 * @throws IllegalStateException if the database is not currently running.
	 */
	public static void shutdown() throws IllegalStateException {
		
		if ( db == null ) {
			throw new IllegalStateException( "Database not currently running." );
		}
		
		LOG.info( "Terminating database." );
		
		// TODO: database changing
		
		db.close();
		
		LOG.info( "Database terminated." );
		
	}

}
