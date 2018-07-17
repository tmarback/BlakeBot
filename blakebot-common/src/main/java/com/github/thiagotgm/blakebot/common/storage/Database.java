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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.thiagotgm.blakebot.common.utils.Graph;
import com.github.thiagotgm.blakebot.common.utils.Tree;

/**
 * Database that provides persistent data storage in the form of tree-graphs and maps.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-16
 */
public abstract class Database implements Closeable {
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @return The tree.
	 * @throws NullPointerException If the tree name is null.
	 */
	public abstract Tree<String,String> getDataTree( String treeName ) throws NullPointerException;
	
	/**
	 * Obtains a data tree backed by this database that maps object paths to strings.
	 * 
	 * @param treeName The name of the tree.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The tree.
	 * @throws NullPointerException If the tree name or the translator is null.
	 * @param <K> The type of the keys that define connections on the tree.
	 */
	public abstract <K> Tree<K,String> getKeyTranslatedDataTree( String treeName,
			Translator<K> keyTranslator ) throws NullPointerException;
	
	/**
	 * Obtains a data tree backed by this database that maps string paths to objects.
	 * 
	 * @param treeName The name of the tree.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The tree.
	 * @throws NullPointerException If the tree name or the translator is null.
	 * @param <V> The type of the values stored in the tree.
	 */
	public abstract <V> Tree<String,V> getValueTranslatedDataTree( String treeName,
			Translator<V> valueTranslator ) throws NullPointerException;
	
	/**
	 * Obtains a data tree backed by this database that maps object paths to objects.
	 * 
	 * @param treeName The name of the tree.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The tree.
	 * @throws NullPointerException If the tree name or either of the translators is null.
	 * @param <K> The type of the keys that define connections on the tree.
	 * @param <V> The type of the values stored in the tree.
	 */
	public abstract <K,V> Tree<K,V> getTranslatedDataTree( String treeName,
			Translator<K> keyTranslator, Translator<V> valueTranslator ) throws NullPointerException;
	
	/**
	 * Obtains a data map backed by this database that maps strings to strings.
	 * 
	 * @param mapName The name of the map.
	 * @return The map.
	 * @throws NullPointerException If the map name is null.
	 */
	public Map<String,String> getDataMap( String mapName ) throws NullPointerException {
		
		return new TreeMap<>( getDataTree( mapName ) );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps objects to strings.
	 * 
	 * @param mapName The name of the map.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or the translator is null.
	 * @param <K> The type of the keys that define connections on the map.
	 */
	public <K> Map<K,String> getKeyTranslatedDataMap( String mapName,
			Translator<K> keyTranslator ) throws NullPointerException {
		
		return new TreeMap<>( getKeyTranslatedDataTree( mapName, keyTranslator ) );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps strings to objects.
	 * 
	 * @param mapName The name of the map.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or the translator is null.
	 * @param <V> The type of the values stored in the map.
	 */
	public <V> Map<String,V> getValueTranslatedDataMap( String mapName,
			Translator<V> valueTranslator ) throws NullPointerException {
		
		return new TreeMap<>( getValueTranslatedDataTree( mapName, valueTranslator ) );
		
	}
	
	/**
	 * Obtains a data map backed by this database that maps objects to objects.
	 * 
	 * @param mapName The name of the map.
	 * @param keyTranslator The translator to use to convert keys into strings.
	 * @param valueTranslator The translator to use to convert values into strings.
	 * @return The map.
	 * @throws NullPointerException If the map name or either of the translators is null.
	 * @param <K> The type of the keys that define connections on the map.
	 * @param <V> The type of the values stored in the map.
	 */
	public <K,V> Map<K,V> getTranslatedDataMap( String mapName,
			Translator<K> keyTranslator, Translator<V> valueTranslator ) throws NullPointerException {
		
		return new TreeMap<>( getTranslatedDataTree( mapName, keyTranslator, valueTranslator ) );
		
	}
	
	/**
	 * Retrieves all the data trees in this database.
	 * 
	 * @return The data trees of this database, keyed by their names.
	 */
	protected abstract Map<String,Tree<String,String>> getDataTrees();
	
	/**
	 * Loads in this database all the data present in the given database.
	 * <p>
	 * If the given database has overlapping keys with this one (same tree/map name, same path/key),
	 * the value that is already in this database is given preference (the one in the given database
	 * is discarded).
	 * 
	 * @param db The database to load into this one.
	 */
	private void load( Database db ) {
		
		for ( Map.Entry<String,Tree<String,String>> tree : db.getDataTrees().entrySet() ) {
			
			Tree<String,String> newTree = getDataTree( tree.getKey() );
			for ( Graph.Entry<String,String> mapping : tree.getValue().entrySet() ) {
				
				newTree.add( mapping.getValue(), mapping.getPathArray() );
				
			}
			
		}
		
	}
	
	/* Adapter class for a map. */
	
	/**
	 * Adapts a Tree to the Map interface. The backing Tree can still be used
	 * as a Tree, but the Map view is limited only to the mappings in the level
	 * 1 of the tree (a single key in the path).
	 * 
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-07-16
	 *
	 * @param <K> The type of the keys that define connections on the map.
	 * @param <V> The type of the values to be stored.
	 */
	private static class TreeMap<K,V> implements Map<K,V> {
		
		private Tree<K,V> tree;
		
		/**
		 * Creates a new TreeMap backed by the given tree.
		 * 
		 * @param backingTree The tree that backs the new map.
		 */
		public TreeMap( Tree<K,V> backingTree ) {
			
			this.tree = backingTree;
			
		}

		@Override
		public int size() {
			
			return tree.size( 1 ); // Map is level 1 of the tree.
			
		}

		@Override
		public boolean isEmpty() {
			
			return tree.isEmpty( 1 ); // Map is level 1 of the tree.
			
		}

		@Override
		public boolean containsKey( Object key ) {
			
			return tree.get( key ) != null;
					
		}

		@Override
		public boolean containsValue( Object value ) {

			
			for ( Map.Entry<K,V> entry : this.entrySet() ) {
				
				if ( entry.getValue().equals( value ) ) {
					return true;
				}
				
			};
			return false;
			
		}

		@Override
		public V get( Object key ) {
			
			return tree.get( key );
			
		}

		@Override
		@SuppressWarnings("unchecked")
		public V put( K key, V value ) {
			
			return tree.set( value, key );
			
		}

		@Override
		public V remove( Object key ) {
			
			return tree.remove( key );
			
		}

		@Override
		public void putAll( Map<? extends K, ? extends V> m ) {

			for ( Map.Entry<? extends K, ? extends V> entry : m.entrySet() ) {
				
				this.put( entry.getKey(), entry.getValue() );
				
			}
			
		}

		@Override
		public void clear() {
			
			tree.clear();
			
		}

		@Override
		public Set<K> keySet() {

			Set<K> keys = new HashSet<>();
			this.entrySet().stream().forEach( ( entry ) -> {
				
				keys.add( entry.getKey() );
				
			});
			return keys;
			
		}

		@Override
		public Collection<V> values() {
			
			Collection<V> values = new ArrayList<>( this.size() );
			this.entrySet().stream().forEach( ( entry ) -> {
				
				values.add( entry.getValue() );
				
			});
			return values;
			
		}

		@Override
		public Set<Entry<K,V>> entrySet() {

			Set<Entry<K,V>> entries = new HashSet<>();
			tree.entrySet( 1 ).stream().forEach( ( entry ) -> {
				
				entries.add( new Entry<K,V>() {

					Graph.Entry<K,V> backing = entry;
					
					@Override
					public K getKey() {
						
						return backing.getPath().get( 0 );
						
					}

					@Override
					public V getValue() {
						
						return backing.getValue();
						
					}

					@Override
					public V setValue( V value ) {
						
						return backing.setValue( value );
						
					}
					
				});
				
			}); // Map is level 1 of the tree.
			
			return entries;
			
		}
		
	}
	
}
