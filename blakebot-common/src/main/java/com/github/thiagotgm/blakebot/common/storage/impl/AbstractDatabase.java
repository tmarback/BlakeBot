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

package com.github.thiagotgm.blakebot.common.storage.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.blakebot.common.storage.Cache;
import com.github.thiagotgm.blakebot.common.storage.Database;
import com.github.thiagotgm.blakebot.common.storage.DatabaseStats;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.utils.Tree;

/**
 * Provides implementation of behavior that is common to all Database
 * implementations.
 * <p>
 * Subclasses are expected to set {@link #loaded} to <tt>true</tt> upon
 * a successful execution of {@link #load(List)}, and to set
 * {@link #closed} to <tt>true</tt> when {@link #close()} is called
 * ({@link #loaded} should <b>not</b> be set back to <tt>false</tt> after
 * {@link #close()} is called).<br>
 * All methods implemented here will check the value of those variables
 * to ensure proper database state, and trees and maps received from
 * {@link #newMap(String, Translator, Translator)} and
 * {@link #newTree(String, Translator, Translator)} will be wrapped 
 * so that they will automatically stop working after {@link #closed}
 * is set to <tt>true</tt> (all operations will then throw an
 * {@link IllegalStateException}).
 * <p>
 * The tree and map wrappers also automatically provide buffering for the
 * get() operation, using the cache size specified by the settings at
 * start time (if the size is changed, it will not take effect until the
 * next time the program is started). No other operations are buffered,
 * although subclasses are free to use their own internal caches for
 * other operations.<br>
 * OBS: Using operations in the Set views of wrapped maps and trees that
 * change the associated map/tree will immediately invalidate the entire
 * cache for that map/tree.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-26
 * @see Cache
 */
public abstract class AbstractDatabase implements Database {
	
	private static final int CACHE_SIZE = Settings.getIntSetting( "Cache size" );
	
	/**
	 * Trees currently managed by the database.
	 */
	private final Map<String,TreeEntry<?,?>> trees;
	/**
	 * Maps currently managed by the database.
	 */
	private final Map<String,MapEntry<?,?>> maps;
	
	/**
	 * Whether the database is currently loaded.
	 */
	protected volatile boolean loaded;
	/**
	 * Whether the database is currently closed.
	 */
	protected volatile boolean closed;
	
	/**
	 * Initializes the database.
	 */
	public AbstractDatabase() {
		
		trees = new HashMap<>();
		maps = new HashMap<>();
		
		loaded = false;
		closed = false;
		
	}
	
	/**
	 * Checks that the database is already loaded and not closed yet, throwing an exception
	 * otherwise.
	 * 
	 * @throws IllegalStateException if the database is not loaded yet or already closed.
	 */
	private void checkState() throws IllegalStateException {
		
		if ( !loaded ) {
			throw new IllegalStateException( "Database not loaded yet." );
		}
		
		if ( closed ) {
			throw new IllegalStateException( "Database already closed." );
		}
		
	}
	
	/**
	 * Creates a new data tree backed by the storage system.
	 * <p>
	 * The arguments are guaranteed to not be <tt>null</tt>.
	 * 
	 * @param dataName The name that identifies the data set.
	 * @param keyTranslator The translator to use to convert keys in the path to Strings.
	 * @param valueTranslator The translator to use to convert values to Strings.
	 * @param <K> The type of keys in the tree paths.
	 * @param <V> The type of values to be stored in the tree.
	 * @return The data tree.
	 * @throws DatabaseException if an error occurred while obtaining the tree.
	 */
	protected abstract <K,V> Tree<K,V> newTree( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException;
	
	@Override
	public synchronized <K,V> Tree<K,V> getTranslatedDataTree( String treeName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		checkState();
		
		if ( ( treeName == null ) || ( keyTranslator == null ) || ( valueTranslator == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		Tree<K,V> tree;
		TreeEntry<?,?> entry = trees.get( treeName ); // Check if there is already an entry.
		if ( entry == null ) { // No entry.
			if ( maps.containsKey( treeName ) ) { // Check if map name.
				throw new IllegalArgumentException( "Given name is assigned to a map." );
			}
			
			// Create and record new tree, within a wrapper.
			tree = new DatabaseTree<>( newTree( treeName, keyTranslator, valueTranslator ) );
			trees.put( treeName, new TreeEntryImpl<>( treeName, tree, keyTranslator, valueTranslator ) );
		} else { // Found entry.
			if ( keyTranslator.getClass() != entry.getKeyTranslator().getClass() ) {
				throw new IllegalArgumentException( "Given key translator is of a different class "
						+ "than the existing key translator." );
			} // Check that translators match.
			
			if ( valueTranslator.getClass() != entry.getValueTranslator().getClass() ) {
				throw new IllegalArgumentException( "Given value translator is of a different class "
						+ "than the existing value translator." );
			}
			
			@SuppressWarnings("unchecked")
			Tree<K,V> t = (Tree<K,V>) entry.getTree(); // Translators match - tree is the correct type.
			tree = t;
		}
		
		return tree;
		
	}
	
	/**
	 * Creates a new data map backed by the storage system.
	 * <p>
	 * The arguments are guaranteed to not be <tt>null</tt>.
	 * 
	 * @param dataName The name that identifies the data set.
	 * @param keyTranslator The translator to use to convert keys to Strings.
	 * @param valueTranslator The translator to use to convert values to Strings.
	 * @param <K> The type of keys in the map.
	 * @param <V> The type of values to be stored in the map.
	 * @return The data map.
	 * @throws DatabaseException if an error occurred while obtaining the map.
	 */
	protected abstract <K,V> Map<K,V> newMap( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException;
	
	@Override
	public synchronized <K,V> Map<K,V> getTranslatedDataMap( String mapName,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException, IllegalStateException, IllegalArgumentException,
			DatabaseException {
		
		checkState();
		
		if ( ( mapName == null ) || ( keyTranslator == null ) || ( valueTranslator == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		Map<K,V> map;
		MapEntry<?,?> entry = maps.get( mapName ); // Check if there is already an entry.
		if ( entry == null ) { // No entry.
			if ( trees.containsKey( mapName ) ) { // Check if tree name.
				throw new IllegalArgumentException( "Given name is assigned to a tree." );
			}
			
			// Create and record new map, within a wrapper.
			map = new DatabaseMap<>( newMap( mapName, keyTranslator, valueTranslator ) );
			maps.put( mapName, new MapEntryImpl<>( mapName, map, keyTranslator, valueTranslator ) );
		} else { // Found entry.
			if ( keyTranslator.getClass() != entry.getKeyTranslator().getClass() ) {
				throw new IllegalArgumentException( "Given key translator is of a different class "
						+ "than the existing key translator." );
			} // Check that translators match.
			
			if ( valueTranslator.getClass() != entry.getValueTranslator().getClass() ) {
				throw new IllegalArgumentException( "Given value translator is of a different class "
						+ "than the existing value translator." );
			}
			
			@SuppressWarnings("unchecked")
			Map<K,V> m = (Map<K,V>) entry.getMap(); // Translators match - map is the correct type.
			map = m;
		}
		
		return map;
		
	}
	
	@Override
	public int size() throws IllegalStateException {
		
		checkState();
		
		return trees.size() + maps.size();
		
	}
	
	@Override
	public Collection<TreeEntry<?,?>> getDataTrees() throws IllegalStateException {
		
		checkState();
		
		return Collections.unmodifiableCollection( trees.values() );
		
	}
	
	@Override
	public Collection<MapEntry<?,?>> getDataMaps() throws IllegalStateException {
		
		checkState();
		
		return Collections.unmodifiableCollection( maps.values() );
		
	}

	/* Entry implementations */
	
	/**
	 * Implementation of a database entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the storage.
	 * @param <V> The type of value used by the storage.
	 * @param <T> The type of the storage unit.
	 */
	protected abstract class DatabaseEntryImpl<K,V,T> implements DatabaseEntry<K,V,T> {
		
		private final String name;
		private final T storage;
		private final Translator<K> keyTranslator;
		private final Translator<V> valueTranslator;
		
		/**
		 * Initializes a database entry for the given storage unit, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage unit is registered under.
		 * @param storage The storage unit.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public DatabaseEntryImpl( String name, T storage, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			if ( ( name == null ) || ( storage == null ) || ( keyTranslator == null ) ||
					( valueTranslator == null ) ) {
				throw new NullPointerException( "Arguments can't be null." );
			}
			
			this.name = name;
			this.storage = storage;
			this.keyTranslator = keyTranslator;
			this.valueTranslator = valueTranslator;
			
		}
		
		@Override
		public String getName() {
			
			return name;
			
		}
		
		@Override
		public T getStorage() {
			
			return storage;
			
		}
		
		@Override
		public Translator<K> getKeyTranslator() {
			
			return keyTranslator;
			
		}
		
		@Override
		public Translator<V> getValueTranslator() {
			
			return valueTranslator;
			
		}
		
	}
	
	/**
	 * Implementation of a database tree entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the tree.
	 * @param <V> The type of value used by the tree.
	 */
	protected class TreeEntryImpl<K,V> extends DatabaseEntryImpl<K,V,Tree<K,V>> implements TreeEntry<K,V> {
		
		/**
		 * Initializes a tree entry for the given storage tree, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage tree is registered under.
		 * @param tree The storage tree.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public TreeEntryImpl( String name, Tree<K,V> tree, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			super( name, tree, keyTranslator, valueTranslator );
			
		}

	}
	
	/**
	 * Implementation of a database map entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the map.
	 * @param <V> The type of value used by the map.
	 */
	protected class MapEntryImpl<K,V> extends DatabaseEntryImpl<K,V,Map<K,V>> implements MapEntry<K,V> {
		
		/**
		 * Initializes a map entry for the given storage map, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage unit is registered under.
		 * @param map The storage map.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public MapEntryImpl( String name, Map<K,V> map, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			super( name, map, keyTranslator, valueTranslator );
			
		}
		
	}
	
	/* Pass-through wrappers that check for the database being closed */
	
	/**
	 * Iterator that iterates over data in the database. 
	 * <p>
	 * When a call is made to the iterator, it checks if the database is already closed. If
	 * it is, the call fails with a {@link IllegalStateException}. Else, the call is passed
	 * through to the backing iterator.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <E> The type of object that the iterator retrieves.
	 */
	private class DatabaseIterator<E> implements Iterator<E> {
		
		private final Iterator<E> backing;
		private final Object changeMonitor;
		private final Cache<?,?> cache;
		
		/**
		 * Instantiates an iterator backed by the given database iterator.
		 * 
		 * @param backing The iterator that backs this.
		 * @param changeMonitor The monitor to be synchronized under when performing
		 *                      a change to the database.
		 * @param cache The cache being used by the data.
		 */
		public DatabaseIterator( Iterator<E> backing, Object changeMonitor, Cache<?,?> cache ) {
			
			this.backing = backing;
			this.changeMonitor = changeMonitor;
			this.cache = cache;
			
		}

		@Override
		public boolean hasNext() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.hasNext();
			
		}

		@Override
		public E next() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.next();
			
		}
		
		@Override
		public void remove() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			synchronized ( changeMonitor ) {
				
				backing.remove();
				cache.clear(); // Invalidate cache.
				
			}
			
		}
		
		@Override
		public boolean equals( Object o ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.equals( o );
			
		}
		
		@Override
		public int hashCode() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.hashCode();
			
		}
		
	}
	
	/**
	 * Collection that represents data in the database. 
	 * <p>
	 * When a call is made to the collection, it checks if the database is already closed. If
	 * it is, the call fails with a {@link IllegalStateException}. Else, the call is passed
	 * through to the backing collection.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <E> The type of object in the collection.
	 */
	private class DatabaseCollection<E> implements Collection<E> {
		
		private final Collection<E> backing;
		private final Object changeMonitor;
		private final Cache<?,?> cache;
		
		/**
		 * Instantiates a collection backed by the given database collection.
		 * 
		 * @param backing The collection that backs this.
		 * @param changeMonitor The monitor to be synchronized under when performing
		 *                      a change to the database.
		 * @param cache The cache being used by the data.
		 */
		public DatabaseCollection( Collection<E> backing, Object changeMonitor, Cache<?,?> cache ) {
			
			this.backing = backing;
			this.changeMonitor = changeMonitor;
			this.cache = cache;
			
		}

		@Override
		public int size() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.size();
			
		}

		@Override
		public boolean isEmpty() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.isEmpty();
			
		}

		@Override
		public boolean contains( Object o ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.contains( o );
			
		}

		@Override
		public Iterator<E> iterator() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseIterator<>( backing.iterator(), changeMonitor, cache );
			
		}

		@Override
		public Object[] toArray() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.toArray();
			
		}

		@Override
		public <T> T[] toArray( T[] a ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.toArray( a );
			
		}

		@Override
		public boolean add( E e ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
		
			return backing.add( e );
			
		}

		@Override
		public boolean remove( Object o ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			synchronized ( changeMonitor ) {
				
				cache.clear(); // Invalidate cache.
				return backing.remove( o );
			
			}
			
		}

		@Override
		public boolean containsAll( Collection<?> c ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.containsAll( c );
			
		}

		@Override
		public boolean addAll( Collection<? extends E> c ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.addAll( c );
			
		}

		@Override
		public boolean removeAll( Collection<?> c ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			synchronized ( changeMonitor ) {
				
				cache.clear(); // Invalidate cache.
				return backing.removeAll( c );
				
			}
			
		}

		@Override
		public boolean retainAll( Collection<?> c ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			synchronized ( changeMonitor ) {
				
				cache.clear(); // Invalidate cache.
				return backing.retainAll( c );
			
			}
			
		}

		@Override
		public void clear() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			synchronized ( changeMonitor ) {
			
				cache.clear(); // Invalidate cache.
				backing.clear();
			
			}
			
		}
		
		@Override
		public boolean equals( Object o ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.equals( o );
			
		}
		
		@Override
		public int hashCode() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.hashCode();
			
		}
		
	}
	
	/**
	 * Set that represents data in the database. 
	 * <p>
	 * When a call is made to the set, it checks if the database is already closed. If
	 * it is, the call fails with a {@link IllegalStateException}. Else, the call is passed
	 * through to the backing set.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <E> The type of object in the set.
	 */
	private class DatabaseSet<E> extends DatabaseCollection<E> implements Set<E> {
		
		/**
		 * Instantiates a set backed by the given database set.
		 * 
		 * @param backing The set that backs this.
		 * @param changeMonitor The monitor to be synchronized under when performing
		 *                      a change to the database.
		 * @param cache The cache being used by the data.
		 */
		public DatabaseSet( Set<E> backing, Object changeMonitor, Cache<?,?> cache ) {
			
			super( backing, changeMonitor, cache );
			
		}
		
	}
	
	/* 
	 * Wrappers for trees and maps that provides common functionality, such as checking if
	 * the database is open and caching data.
	 */
	
	/**
	 * Tree that represents data in the database. 
	 * <p>
	 * When a call is made to the tree, it checks if the database is already closed. If
	 * it is, the call fails with a {@link IllegalStateException}. Else, the call is passed
	 * through to the backing tree.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <K> The type of key in the paths used by the tree.
	 * @param <V> The type of values stored in the tree.
	 */
	private class DatabaseTree<K,V> implements Tree<K,V> {
		
		private final Tree<K,V> backing;
		private final Cache<List<K>,V> cache;
		
		/**
		 * Instantiates a tree backed by the given database tree.
		 * 
		 * @param backing The tree that backs this.
		 */
		public DatabaseTree( Tree<K,V> backing ) {
			
			this.backing = backing;
			this.cache = new Cache<>( CACHE_SIZE );
			
		}
		
		@Override
		public boolean containsPath( List<K> path ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.containsPath( path );
			
		}

		@Override
		public boolean containsValue( V value ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.containsValue( value );
			
		}

		@Override
		public synchronized V get( List<K> path ) throws IllegalArgumentException {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			V value = cache.get( path ); // Look in cache.
			
			if ( value == null ) { // Not in cache.				
				long start = System.currentTimeMillis();
				value = backing.get( path ); // Look in database.
				long elapsed = System.currentTimeMillis() - start;
				
				if ( value == null ) { // Not in database (fetch fail).
					DatabaseStats.addDbFetchFailure( elapsed );
				} else { // In database (fetch success).
					DatabaseStats.addDbFetchSuccess( elapsed );
					DatabaseStats.addCacheMiss(); // Value exists, just wasn't in cache.
					cache.put( path, value ); // Cache found value.
				}
			} else { // Found in cache.
				DatabaseStats.addCacheHit();
			}
			
			return value;
			
		}

		@Override
		public List<V> getAll( List<K> path ) throws IllegalArgumentException {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.getAll( path );
			
		}

		@Override
		public synchronized V set( V value, List<K> path )
				throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.update( path, value ); // Updates previously cached value, if any.
			
			return backing.set( value, path );
			
		}

		@Override
		public boolean add( V value, List<K> path )
				throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.add( value, path );
			
		}

		@Override
		public synchronized V remove( List<K> path ) throws UnsupportedOperationException, IllegalArgumentException {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.remove( path ); // Remove previously cached value, if any.
			
			return backing.remove( path );
			
		}

		@Override
		public Set<List<K>> pathSet() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseSet<>( backing.pathSet(), this, cache );
			
		}

		@Override
		public Collection<V> values() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseCollection<>( backing.values(), this, cache );
			
		}

		@Override
		public Set<Entry<K,V>> entrySet() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseSet<>( backing.entrySet(), this, cache );
			
		}

		@Override
		public int size() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.size();
			
		}
		
		@Override
		public boolean isEmpty() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.isEmpty();
			
		}

		@Override
		public synchronized void clear() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.clear();
			backing.clear();
			
		}
		
		@Override
		public boolean equals( Object obj ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.equals( obj );
			
		}
		
		@Override
		public int hashCode() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.hashCode();
			
		}
		
	}

	/**
	 * Map that represents data in the database. 
	 * <p>
	 * When a call is made to the map, it checks if the database is already closed. If
	 * it is, the call fails with a {@link IllegalStateException}. Else, the call is passed
	 * through to the backing map.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <K> The type of keys used by the map.
	 * @param <V> The type of values stored in the map.
	 */
	private class DatabaseMap<K,V> implements Map<K,V> {
		
		private final Map<K,V> backing;
		private final Cache<K,V> cache;
		
		/**
		 * Instantiates a map backed by the given database map.
		 * 
		 * @param backing The map that backs this.
		 */
		public DatabaseMap( Map<K,V> backing ) {
			
			this.backing = backing;
			this.cache = new Cache<>( CACHE_SIZE );
			
		}

		@Override
		public int size() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.size();
			
		}

		@Override
		public boolean isEmpty() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.isEmpty();
			
		}

		@Override
		public boolean containsKey( Object key ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.containsKey( key );
			
		}

		@Override
		public boolean containsValue( Object value ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.containsValue( value );
			
		}

		@Override
		public synchronized V get( Object key ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			V value = cache.get( key ); // Look in cache.
			
			if ( value == null ) { // Not in cache.				
				long start = System.currentTimeMillis();
				value = backing.get( key ); // Look in database.
				long elapsed = System.currentTimeMillis() - start;
				
				if ( value == null ) { // Not in database (fetch fail).
					DatabaseStats.addDbFetchFailure( elapsed );
				} else { // In database (fetch success).
					DatabaseStats.addDbFetchSuccess( elapsed );
					DatabaseStats.addCacheMiss(); // Value exists, just wasn't in cache.
					@SuppressWarnings("unchecked")
					K theKey = (K) key;
					cache.put( theKey, value ); // Cache found value.
				}
			} else { // Found in cache.
				DatabaseStats.addCacheHit();
			}
			
			return value;
			
		}

		@Override
		public synchronized V put( K key, V value ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.update( key, value ); // Update previously cached value, if any.
			
			return backing.put( key, value );
			
		}

		@Override
		public synchronized V remove( Object key ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.remove( key ); // Remove previously cached value, if any.
			
			return backing.remove( key );
			
		}

		@Override
		public synchronized void putAll( Map<? extends K,? extends V> m ) {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			for ( Map.Entry<? extends K,? extends V> entry : m.entrySet() ) {
				
				// Update previously cached value, if any.
				cache.update( entry.getKey(), entry.getValue() );
				
			}
			
			backing.putAll( m );
			
		}

		@Override
		public synchronized void clear() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			cache.clear();
			backing.clear();
			
		}

		@Override
		public Set<K> keySet() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseSet<>( backing.keySet(), this, cache );
			
		}

		@Override
		public Collection<V> values() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseCollection<>( backing.values(), this, cache );
			
		}

		@Override
		public Set<Entry<K,V>> entrySet() {

			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return new DatabaseSet<>( backing.entrySet(), this, cache );
			
		}
		
		@Override
		public boolean equals( Object o ) {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.equals( o );
			
		}
		
		@Override
		public int hashCode() {
			
			if ( closed ) {
				throw new IllegalStateException( "The backing database is already closed." );
			}
			
			return backing.hashCode();
			
		}
		
	}

}
