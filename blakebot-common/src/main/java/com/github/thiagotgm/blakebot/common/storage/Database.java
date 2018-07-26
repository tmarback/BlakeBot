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
 * All maps and trees that were obtained from a database are closed when the database
 * itself is closed (the {@link #close()} method is called). Any calls made to this
 * database or derived maps or trees (other than {@link #close()} itself) after that
 * will fail with an {@link IllegalStateException}.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-16
 */
public abstract class Database implements Closeable {
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @return The tree.
	 * @throws NullPointerException if the tree name is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 */
	public Tree<String,String> getDataTree( String treeName )
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
		return getTranslatedDataTree( treeName, new StringTranslator(), new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data tree backed by this database that maps object paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The tree.
	 * @throws NullPointerException if the tree name or the translator is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <K> The type of the keys that define connections on the tree.
	 */
	public <K> Tree<K,String> getKeyTranslatedDataTree( String treeName, Translator<K> keyTranslator )
					throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
		return getTranslatedDataTree( treeName, keyTranslator, new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to objects.
	 * 
	 * @param treeName The name of the tree.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The tree.
	 * @throws NullPointerException if the tree name or the translator is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <V> The type of the values stored in the tree.
	 */
	public <V> Tree<String,V> getValueTranslatedDataTree( String treeName, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
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
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a map with the given name already exists, or if
	 *                                  a tree with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <K> The type of the keys that define connections on the tree.
	 * @param <V> The type of the values stored in the tree.
	 */
	public abstract <K,V> Tree<K,V> getTranslatedDataTree( String treeName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException;
	
	/**
	 * Obtains a data map backed by this database that maps strings to strings.
	 * 
	 * @param mapName The name of the map.
	 * @return The map.
	 * @throws NullPointerException if the map name is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 */
	public Map<String,String> getDataMap( String mapName )
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
		return getTranslatedDataMap( mapName, new StringTranslator(), new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps objects to strings.
	 * 
	 * @param mapName The name of the map.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The map.
	 * @throws NullPointerException if the map name or the translator is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <K> The type of the keys that define connections on the map.
	 */
	public <K> Map<K,String> getKeyTranslatedDataMap( String mapName, Translator<K> keyTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
		return getTranslatedDataMap( mapName, keyTranslator, new StringTranslator() );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps strings to objects.
	 * 
	 * @param mapName The name of the map.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or the translator is null.
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <V> The type of the values stored in the map.
	 */
	public <V> Map<String,V> getValueTranslatedDataMap( String mapName, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException {
		
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
	 * @throws IllegalStateException if the database is already closed.
	 * @throws IllegalArgumentException if a tree with the given name already exists, or if
	 *                                  a map with the given name already exists and it
	 *                                  uses incompatible translator types.
	 * @param <K> The type of the keys that define connections on the map.
	 * @param <V> The type of the values stored in the map.
	 */
	public abstract <K,V> Map<K,V> getTranslatedDataMap( String mapName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException;
	
	/**
	 * Retrieves the number of trees and maps contained by this database.<br>
	 * The same as <tt>getDataTrees().size() + getDataMaps().size()</tt>.
	 * <p>
	 * OBS: The value returned by this does not necessarily reflect the number of trees and
	 * maps stored in the storage system that backs this instance, but rather the amount
	 * of trees and maps that were created due to calls to get# methods and so are currently
	 * being managed by this instance. It is possible that the backing storage contains more
	 * trees or maps that haven't been requested yet.
	 * 
	 * @return The number of trees and maps in use.
	 */
	protected abstract int size();
	
	/**
	 * Retrieves all the data trees in this database.
	 * 
	 * @return The data trees of this database, keyed by their names.
	 * @throws IllegalStateException if the database is already closed.
	 */
	protected abstract Map<String,Tree<String,String>> getDataTrees() throws IllegalStateException;
	
	/**
	 * Retrieves all the data maps in this database.
	 * 
	 * @return The data maps of this database, keyed by their names.
	 * @throws IllegalStateException if the database is already closed.
	 */
	protected abstract Map<String,Map<String,String>> getDataMaps() throws IllegalStateException;
	
	/**
	 * Retrieves the names of the parameters required for {@link #load(List)}.
	 * 
	 * @return The load parameters.
	 */
	protected abstract List<String> getLoadParams();
	
	/**
	 * Loads/connects the database using the given parameters.
	 * 
	 * @param params The parameters to load the database with. Each argument in the list must
	 *               correspond to an argument named in the return of {@link #getLoadParams()}.
	 *               This implies that it is necessary that
	 *               <tt>params.size() == getLoadParams().size()</tt>.
	 * @return <tt>true</tt> if the database was successfully loaded.
	 *         <tt>false</tt> if an error occurred.
	 * @throws IllegalStateException if the database is already closed.
	 */
	protected abstract boolean load( List<String> params ) throws IllegalStateException;
	
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
	 * @throws IllegalStateException if either this or the given database is already closed, or if
	 *                               this database has already created data trees or maps for
	 *                               external use.
	 */
	void copyData( Database db ) throws IllegalStateException {
		
		if ( size() != 0 ) {
			throw new IllegalStateException( "This database already has trees or maps checked out." );
		}
		
		Map<String,Tree<String,String>> trees;
		try {
			trees = db.getDataTrees();
		} catch ( IllegalStateException e ) {
			throw new IllegalStateException( "Could not obtain data trees.", e );
		}
		
		for ( Map.Entry<String,Tree<String,String>> tree : trees.entrySet() ) {
			
			Tree<String,String> newTree = getDataTree( tree.getKey() );
			for ( Graph.Entry<String,String> mapping : tree.getValue().entrySet() ) {
				
				newTree.add( mapping.getValue(), mapping.getPathArray() );
				
			}
			
		}
		
	}
	
}
