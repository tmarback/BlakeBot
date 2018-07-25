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
	 */
	public Tree<String,String> getDataTree( String treeName )
			throws NullPointerException, IllegalStateException {
		
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
	 * @param <K> The type of the keys that define connections on the tree.
	 */
	public <K> Tree<K,String> getKeyTranslatedDataTree( String treeName,
			Translator<K> keyTranslator ) throws NullPointerException, IllegalStateException {
		
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
	 * @param <V> The type of the values stored in the tree.
	 */
	public <V> Tree<String,V> getValueTranslatedDataTree( String treeName,
			Translator<V> valueTranslator ) throws NullPointerException, IllegalStateException {
		
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
	 * @param <K> The type of the keys that define connections on the tree.
	 * @param <V> The type of the values stored in the tree.
	 */
	public abstract <K,V> Tree<K,V> getTranslatedDataTree( String treeName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException;
	
	/**
	 * Obtains a data map backed by this database that maps strings to strings.
	 * 
	 * @param mapName The name of the map.
	 * @return The map.
	 * @throws NullPointerException if the map name is null.
	 * @throws IllegalStateException if the database is already closed.
	 */
	public Map<String,String> getDataMap( String mapName )
			throws NullPointerException, IllegalStateException {
		
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
	 * @param <K> The type of the keys that define connections on the map.
	 */
	public <K> Map<K,String> getKeyTranslatedDataMap( String mapName,
			Translator<K> keyTranslator ) throws NullPointerException, IllegalStateException {
		
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
	 * @param <V> The type of the values stored in the map.
	 */
	public <V> Map<String,V> getValueTranslatedDataMap( String mapName,
			Translator<V> valueTranslator ) throws NullPointerException, IllegalStateException {
		
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
	 * @param <K> The type of the keys that define connections on the map.
	 * @param <V> The type of the values stored in the map.
	 */
	public abstract <K,V> Map<K,V> getTranslatedDataMap( String mapName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException;
	
	/**
	 * Retrieves all the data trees in this database.
	 * 
	 * @return The data trees of this database, keyed by their names.
	 * @throws IllegalStateException if the database is already closed.
	 */
	protected abstract Map<String,Tree<String,String>> getDataTrees() throws IllegalStateException;
	
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
	 */
	protected abstract boolean load( List<String> params );
	
	/**
	 * Loads in this database all the data present in the given database.
	 * <p>
	 * If the given database has overlapping keys with this one (same tree/map name, same path/key),
	 * the value that is already in this database is given preference (the one in the given database
	 * is discarded).
	 * 
	 * @param db The database to load into this one.
	 * @throws IllegalStateException if either this or the given database is already closed.
	 */
	void copyData( Database db ) throws IllegalStateException {
		
		for ( Map.Entry<String,Tree<String,String>> tree : db.getDataTrees().entrySet() ) {
			
			Tree<String,String> newTree = getDataTree( tree.getKey() );
			for ( Graph.Entry<String,String> mapping : tree.getValue().entrySet() ) {
				
				newTree.add( mapping.getValue(), mapping.getPathArray() );
				
			}
			
		}
		
	}
	
}
