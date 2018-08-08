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

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.Graph;
import com.github.thiagotgm.blakebot.common.utils.Tree;

/**
 * Database that provides persistent data storage in the form of tree-graphs and maps.
 * <p>
 * Only one tree or map can exist for a given name. When obtaining a tree or graph,
 * if the name already matches an existing one it will be retrieved, else a new one
 * is created.<br>
 * Names should be unique between trees and maps. That is, if there is a tree with a
 * certain name, there cannot be a map with the same name, and vice versa.<br>
 * Moreover, if a tree or map has already been obtained using a certain set of
 * key/value translators (including no translators), it can only be obtained again
 * using translators of the same class (not necessarily the same instance). This is
 * to ensure data consistency and allow certain optimizations for some types of
 * databases.<br>
 * However, all state is reset between program executions, so it is possible to
 * swap between maps and trees and/or translator types.
 * <p>
 * The database can only be used after it is successfully loaded using the
 * {@link #load(List)} method (must return <tt>true</tt>). Any calls to methods other
 * than {@link #getLoadParams()} or {@link #load(List)} before this will fail with
 * an {@link IllegalStateException}.
 * In addition, all maps and trees that were obtained from a database are closed when
 * the database itself is closed (the {@link #close()} method is called). Any calls
 * made to this database or derived maps or trees (other than {@link #close()} itself)
 * after that will fail with an {@link IllegalStateException}.
 * <p>
 * If a method in the returned trees or maps is called, but some internal error in the
 * database prevents it from being executed properly, a {@link DatabaseException} is
 * thrown.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-16
 */
public interface Database extends Closeable {
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @return The tree.
	 * @throws NullPointerException if the tree name is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the tree.
	 */
	default Tree<String,String> getDataTree( String treeName )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		return getTranslatedDataTree( treeName, new StringTranslator(), new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data tree backed by this database that maps object paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The tree.
	 * @throws NullPointerException if the tree name or the translator is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the tree.
	 * @param <K> The type of the keys that define connections on the tree.
	 */
	default <K> Tree<K,String> getKeyTranslatedDataTree( String treeName, Translator<K> keyTranslator )
					throws NullPointerException, IllegalStateException, IllegalArgumentException,
					DatabaseException {
		
		return getTranslatedDataTree( treeName, keyTranslator, new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to objects.
	 * 
	 * @param treeName The name of the tree.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The tree.
	 * @throws NullPointerException if the tree name or the translator is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the tree.
	 * @param <V> The type of the values stored in the tree.
	 */
	default <V> Tree<String,V> getValueTranslatedDataTree( String treeName, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException, DatabaseException {
		
		return getTranslatedDataTree( treeName, new StringTranslator(), valueTranslator );
		
	}
	
	/**
	 * Obtains a data tree backed by this database that maps object paths to objects.
	 * 
	 * @param treeName The name of the tree.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The tree.
	 * @throws NullPointerException if the tree name or either of the translators is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the tree.
	 * @param <K> The type of the keys that define connections on the tree.
	 * @param <V> The type of the values stored in the tree.
	 */
	<K,V> Tree<K,V> getTranslatedDataTree( String treeName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException;
	
	/**
	 * Obtains a data map backed by this database that maps strings to strings.
	 * 
	 * @param mapName The name of the map.
	 * @return The map.
	 * @throws NullPointerException if the map name is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the map.
	 */
	default Map<String,String> getDataMap( String mapName )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		return getTranslatedDataMap( mapName, new StringTranslator(), new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps objects to strings.
	 * 
	 * @param mapName The name of the map.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The map.
	 * @throws NullPointerException if the map name or the translator is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the map.
	 * @param <K> The type of the keys that define connections on the map.
	 */
	default <K> Map<K,String> getKeyTranslatedDataMap( String mapName, Translator<K> keyTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		return getTranslatedDataMap( mapName, keyTranslator, new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps strings to objects.
	 * 
	 * @param mapName The name of the map.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or the translator is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the map.
	 * @param <V> The type of the values stored in the map.
	 */
	default <V> Map<String,V> getValueTranslatedDataMap( String mapName, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		return getTranslatedDataMap( mapName, new StringTranslator(), valueTranslator );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps objects to objects.
	 * 
	 * @param mapName The name of the map.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or either of the translators is null.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @throws DatabaseException if an error occurred while obtaining the map.
	 * @param <K> The type of the keys that define connections on the map.
	 * @param <V> The type of the values stored in the map.
	 */
	<K,V> Map<K,V> getTranslatedDataMap( String mapName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException;
	
	/**
	 * Retrieves the number of trees and maps contained by this database.<br>
	 * The same as <tt>getDataTrees().size() + getDataMaps().size()</tt>.
	 * <p>
	 * OBS: The value returned by this does not necessarily reflect the number of trees and
	 * maps stored in the storage system that backs this instance, but rather the amount
	 * of trees and maps that were created due to calls to <tt>get*()</tt> methods and so are
	 * currently being managed by this instance. It is possible that the backing storage 
	 * contains more trees or maps that haven't been requested yet.
	 * 
	 * @return The number of trees and maps in use.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 */
	int size() throws IllegalStateException;
	
	/**
	 * Retrieves all the trees currently managed by this database, along with their names and
	 * translators.
	 * <p>
	 * Note that this only returns the data from trees currently managed by the database
	 * (that were checked out using a get*DataTree method), and so any other data that may be
	 * present in the backing storage system but is not represented by an active tree will not
	 * be included.
	 * <p>
	 * The returned set is backed by the database, so changes to the database (a new tree being
	 * created) are reflected in the set. However, the set is not editable, and any attempts to
	 * do so will throw an exception.
	 * 
	 * @return The data trees of this database.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 */
	Collection<TreeEntry<?,?>> getDataTrees() throws IllegalStateException;
	
	/**
	 * Retrieves all the maps currently managed by this database, along with their names and
	 * translators.
	 * <p>
	 * Note that this only returns the data from maps currently managed by the database
	 * (that were checked out using a get*DataMap method), and so any other data that may be
	 * present in the backing storage system but is not represented by an active map will not
	 * be included.
	 * <p>
	 * The returned set is backed by the database, so changes to the database (a new tree being
	 * created) are reflected in the set. However, the set is not editable, and any attempts to
	 * do so will throw an exception.
	 * 
	 * @return The data maps of this database.
	 * @throws IllegalStateException if the database hasn't been successfully loaded yet or
	 * 								 was already closed.
	 */
	Collection<MapEntry<?,?>> getDataMaps() throws IllegalStateException;
	
	/**
	 * Retrieves the names of the parameters required for {@link #load(List)}.
	 * 
	 * @return The load parameters.
	 */
	List<String> getLoadParams();
	
	/**
	 * Loads/connects the database using the given parameters.
	 * 
	 * @param params The parameters to load the database with. Each argument in the list must
	 *               correspond to an argument named in the return of {@link #getLoadParams()}.
	 *               This implies that it is necessary that
	 *               <tt>params.size() == getLoadParams().size()</tt>.
	 * @return <tt>true</tt> if the database was successfully loaded.
	 *         <tt>false</tt> if an error occurred (but all parameters were valid).
	 * @throws IllegalStateException if the database was already loaded.
	 * @throws IllegalArgumentException if the parameter list given has a different size than the
	 *                                  list returned by {@link #getLoadParams()}, or if one or more
	 *                                  of the parameters given is invalid (e.g. wrong format, etc).
	 */
	boolean load( List<String> params ) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * Stops the database, preventing any further accesses or changes.
	 * <p>
	 * Any cached changes are flushed, and the connection to the backing storage
	 * system is terminated, releasing any associated resources.
	 * <p>
	 * After this method returns, any call made to this database, or a tree or map
	 * obtained from it will fail with an {@link IllegalStateException}. The exception
	 * would be calling this method again, which has no effect, or calling
	 * {@link #getLoadParams()}.
	 * <p>
	 * This method can only be called after the database is successfully loaded, that
	 * is, after a call to {@link #load(List)} returns <tt>true</tt>.
	 * 
	 * @throws IllegalStateException if the database hasn't been loaded yet.
	 */
	@Override
	void close() throws IllegalStateException;
	
	/**
	 * Loads in this database all the data present in the given database.
	 * <p>
	 * This database must not be currently managing any trees or maps. This means that no successful
	 * calls to a get# method can have been made, and that {@link #size()} must return 0.<br>
	 * It is acceptable that the storage system backing this instance already contain data
	 * for maps and trees. However, if any of the existing data is stored under the same name as
	 * a tree or map in the given database, the behavior is undefined.
	 * <p>
	 * After this method returns, all the trees and maps that were obtained from the given database
	 * will be String-String trees/maps (e.g. using the {@link StringTranslator} translator for both keys
	 * and values). In order to use them with different translators, it will be necessary to refresh the
	 * database by closing this instance and creating+loading a new one using the same set of parameters.
	 * 
	 * @param db The database to load into this one.
	 * @throws IllegalStateException if either database hasn't been successfully loaded yet, was already
	 *                               closed, or if this database has already created data trees or maps
	 *                               for external use.
	 */
	default void copyData( Database db ) throws IllegalStateException {
		
		if ( size() != 0 ) {
			throw new IllegalStateException( "This database already has trees or maps checked out." );
		}
		
		/* Copy trees */
		
		Collection<TreeEntry<?,?>> trees;
		try {
			trees = db.getDataTrees();
		} catch ( IllegalStateException e ) {
			throw new IllegalStateException( "Could not obtain data trees.", e );
		}
		
		for ( TreeEntry<?,?> tree : trees ) {
			
			@SuppressWarnings("unchecked")
			Tree<Object,Object> newTree = (Tree<Object,Object>) getTranslatedDataTree(
					tree.getName(), tree.getKeyTranslator(), tree.getValueTranslator() );
			for ( Graph.Entry<?,?> mapping : tree.getTree().entrySet() ) {
				
				newTree.add( mapping.getValue(), mapping.getPath() );
				
			}
			
		}
		
		/* Copy maps */
		
		Collection<MapEntry<?,?>> maps;
		try {
			maps = db.getDataMaps();
		} catch ( IllegalStateException e ) {
			throw new IllegalStateException( "Could not obtain data maps.", e );
		}
		
		for ( MapEntry<?,?> map : maps ) {
			
			@SuppressWarnings("unchecked")
			Map<Object,Object> newMap = (Map<Object,Object>) getTranslatedDataMap(
					map.getName(), map.getKeyTranslator(), map.getValueTranslator() );
			for ( Map.Entry<?,?> mapping : map.getMap().entrySet() ) {
				
				newMap.put( mapping.getKey(), mapping.getValue() );
				
			}
			
		}
		
	}
	
	/**
	 * A unit of storage managed by this database.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-26
	 * @param <K> The type of key used by the storage.
	 * @param <V> The type of value used by the storage.
	 * @param <T> The type of the storage unit.
	 */
	interface DatabaseEntry<K,V,T> {
		
		/**
		 * Retrieves the name that the storage unit is registered under.
		 * 
		 * @return The name of the storage unit.
		 */
		String getName();
		
		/**
		 * Retrieves the storage unit itself.
		 * 
		 * @return The storage unit.
		 */
		T getStorage();
		
		/**
		 * Retrieves the translator used by the storage unit to convert keys
		 * to strings.
		 * 
		 * @return The key translator.
		 */
		Translator<K> getKeyTranslator();
		
		/**
		 * Retrieves the translator used by the storage unit to convert values
		 * to strings.
		 * 
		 * @return The value translator.
		 */
		Translator<V> getValueTranslator();
		
	}
	
	/**
	 * A data tree managed by this database.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-26
	 * @param <K> The type of key used by the tree.
	 * @param <V> The type of value used by the tree.
	 */
	interface TreeEntry<K,V> extends DatabaseEntry<K,V,Tree<K,V>> {
		
		/**
		 * Retrieves the tree itself.
		 * 
		 * @return The tree.
		 * @see #getStorage()
		 */
		default Tree<K,V> getTree() {
			
			return getStorage();
			
		}
		
	}
	
	/**
	 * A data map managed by this database.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-26
	 * @param <K> The type of key used by the map.
	 * @param <V> The type of value used by the map.
	 */
	interface MapEntry<K,V> extends DatabaseEntry<K,V,Map<K,V>> {
		
		/**
		 * Retrieves the map itself.
		 * 
		 * @return The map.
		 * @see #getStorage()
		 */
		default Map<K,V> getMap() {
			
			return getStorage();
			
		}
		
	}
	
	/* Exception for database errors */
	
	/**
	 * Exception that indicates that an error occurred during normal functionality
	 * of the database.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-07
	 */
	public class DatabaseException extends RuntimeException {

		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = -7808033071268361758L;

		/**
		 * Constructs a new database exception with no cause.
		 * 
		 * @see RuntimeException#RuntimeException()
	     */
		public DatabaseException() {
			
			super();

		}

		/**
		 * Constructs a new database exception with the given detail message and cause.
		 * 
		 * @param message The detail message.
		 * @param cause The cause of this exception.
		 * @see RuntimeException#RuntimeException(String, Throwable)
		 */
		public DatabaseException( String message, Throwable cause ) {
			
			super( message, cause );
			
		}

		/**
		 * Constructs a new database exception with the given detail message and no cause.
		 * 
		 * @param message The detail message.
		 * @see RuntimeException#RuntimeException(String)
		 */
		public DatabaseException( String message ) {
			
			super( message );

		}

		/**
		 * Constructs a new database exception with the given cause.
		 * 
		 * @param cause The cause of this exception.
		 * @see RuntimeException#RuntimeException(Throwable)
		 */
		public DatabaseException( Throwable cause ) {
			
			super( cause );

		}
		
		
	}
	
}
