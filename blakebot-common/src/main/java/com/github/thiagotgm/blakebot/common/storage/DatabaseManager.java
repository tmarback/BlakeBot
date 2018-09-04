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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.blakebot.common.storage.Database.DatabaseException;
import com.github.thiagotgm.blakebot.common.storage.Database.Parameter;
import com.github.thiagotgm.blakebot.common.storage.impl.XMLDatabase;
import com.github.thiagotgm.blakebot.common.storage.impl.DynamoDBDatabase;
import com.github.thiagotgm.blakebot.common.utils.Utils;

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
	
	private static final String DB_TYPE_SETTING = "Database Service";
	private static final String DB_ARGS_SETTING = "Database Args";
	
	public enum DatabaseType {
		
		/** Stores data in local XML files. */
		XML( "Local XML Files", () -> { return new XMLDatabase(); } ),
		
		DYNAMO_DB( "DynamoDB (AWS)", () -> { return new DynamoDBDatabase(); } );
		
		private final String name;
		private final List<Parameter> loadParams;
		private final Supplier<Database> instanceProducer;
		
		/**
		 * Constructs an enum that represents a database provider.
		 * 
		 * @param name The name of the provider.
		 * @param instanceProducer A function that produces an instance of
		 *                         the database.
		 */
		DatabaseType( String name, Supplier<Database> instanceProducer ) {
			
			this.name = name;
			this.loadParams = instanceProducer.get().getLoadParams();
			this.instanceProducer = instanceProducer;
			
		}
		
		/**
		 * Retrieves the name of the database provider.
		 * 
		 * @return The database provider name.
		 */
		public String getName() {
			
			return name;
			
		}
		
		/**
		 * Retrieves the list of parameters required to load this database
		 * type, where each element is the name of the corresponding parameter.
		 * 
		 * @return The required load parameters.
		 */
		public List<Parameter> getLoadParams() {
			
			return new ArrayList<>( loadParams );
			
		}
		
		/**
		 * Retrieves an instance of this database type.
		 * 
		 * @return A new instance.
		 */
		protected Database getInstance() {
			
			return instanceProducer.get();
			
		}
		
	}
	
	private static Database db = null;
	private static DatabaseType dbChangeType = null;
	private static List<String> dbChangeArgs = null;
	
	/**
	 * Requests a change to the database service. The request does not take effect
	 * immediately, instead it is applied when the program is about to exit (so
	 * a restart is necessary). Once it occurs, all the data in the current database
	 * will be copied into a database of the specified provider type using the given
	 * load arguments, and the next time the program runs it will use the new database.
	 * <p>
	 * However, while the data transfer is delayed, an attempt to load the new database
	 * is made immediately to check that the arguments are valid. If the load fails,
	 * the request is not placed and this method returns <tt>false</tt>. If it connects
	 * successfully, the request is successful (this method returns <tt>true</tt>) and is
	 * recorded to be applied later. The new database loaded here is closed, and when the
	 * data transfer process starts it will be loaded again using a new instance and the
	 * same arguments.
	 * <p>
	 * If, later on, the process of copying data fails due to errors in either database,
	 * the change is aborted (the cause will be logged).
	 * <p>
	 * If this method is called multiple times, the latest <b>successful</b> request is
	 * the one that will be applied when the program closes. Any preceding (successful)
	 * requests are aborted.
	 * 
	 * @param newType The type of the new database.
	 * @param args The arguments to load the new database with.
	 * @return <tt>true</tt> if the request was placed successfully.<br>
	 *         <tt>false</tt> if a database of the given type could not be loaded with the
	 *         given arguments. The previous successful request, if any, is maintained.
	 */
	public static synchronized boolean requestDatabaseChange( DatabaseType newType, List<String> args ) {
		
		LOG.debug( "Received database change request to type {} with arguments {}.", newType, args );
		Database newDB = newType.getInstance();
		if ( newDB.load( args ) ) { // Opened the new database successfully.
			LOG.info( "Placed database change request to type {}.", newType );
			newDB.close();
			dbChangeType = newType; // Record change arguments.
			dbChangeArgs = new ArrayList<>( args );
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Retrieves the type of database that is going to be migrated to.
	 * 
	 * @return The database request type, or <tt>null</tt> if there is
	 *         no change request currently.
	 */
	public static synchronized DatabaseType getDatabaseChangeRequestType() {
		
		return dbChangeType;
		
	}
	
	/**
	 * Cancels/aborts the current database change request, if any.
	 * 
	 * @return Whether a database change was pending and was cancelled.
	 */
	public static synchronized boolean cancelDatabaseChange() {
		
		LOG.info( "Database change aborted." );
		if ( dbChangeType != null ) { 
			dbChangeType = null; // Abort change request.
			dbChangeArgs = null;
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Converts a list of arguments to a single String.
	 * 
	 * @param args The arguments.
	 * @return A string representing the args.
	 */
	private static String toArgString( List<String> args ) {
		
		return Utils.encodeList( args );
		
	}
	
	/**
	 * Obtains a list of arguments from an argument String.
	 * <p>
	 * The inverse of {@link #toArgString(List)}.
	 * 
	 * @param argString The argument string.
	 * @return The arguments in the string.
	 */
	private static List<String> fromArgString( String argString ) {
		
		return Utils.decodeList( argString );
		
	}
	
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
		
		DatabaseType type = DatabaseType.valueOf( Settings.getStringSetting( DB_TYPE_SETTING ) );
		List<String> params = fromArgString( Settings.getStringSetting( DB_ARGS_SETTING ) );
		
		LOG.debug( "Loading database of type {} with args {}.", type, params );
		
		db = type.getInstance();
		boolean result = db.load( params );
		
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
	public static synchronized void shutdown() throws IllegalStateException {
		
		if ( db == null ) {
			throw new IllegalStateException( "Database not currently running." );
		}
		
		if ( dbChangeType != null ) { // Pending change request.
			LOG.info( "Executing database change request." );
			LOG.debug( "Request: type {} with arguments {}.", dbChangeType, dbChangeArgs );
			Database newDB = dbChangeType.getInstance();
			if ( newDB.load( dbChangeArgs ) ) {
				LOG.debug( "New database loaded." );
				try {
					newDB.copyData( db ); // Try to copy data.
					
					/* Copy successful, save new database settings */
					newDB.close();
					Settings.setSetting( DB_TYPE_SETTING, dbChangeType.toString() );
					Settings.setSetting( DB_ARGS_SETTING, toArgString( dbChangeArgs ) );
					Settings.saveSettings(); // Flush to file.
					LOG.info( "Database change performed successfully." );
				} catch ( DatabaseException e ) {
					LOG.error( "Could not copy database data. Change aborted.", e );
				}
			} else {
				LOG.error( "Failed to load new database. Change aborted." );
			}
			
		}
		
		LOG.info( "Terminating database." );
		
		db.close();
		
		LOG.info( "Database terminated." );
		
	}

}
