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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.Tree;
import com.github.thiagotgm.blakebot.common.utils.Utils;

/**
 * Superclass for table-based databases. Provides an implementation of Tree that
 * is backed by a map from the database.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-10
 */
public abstract class TableDatabase extends AbstractDatabase {

	@Override
	protected <K,V> Tree<K,V> newTree( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {

		return new TableTree<>( newMap( dataName, new StringTranslator(), valueTranslator ), keyTranslator );
		
	}
	
	/**
	 * Tree that uses a map as backing storage, by converting the path
	 * into an address string and using that as keys in the map.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-10
	 * @param <K> The type of keys in the path.
	 * @param <V> The type of values being stored.
	 */
	private class TableTree<K,V> implements Tree<K,V> {
		
		private final Map<String,V> backing;
		private final Translator<K> keyTranslator;
		
		/**
		 * Instantiates a tree that is backed by the given map and uses the given
		 * translator to convert keys in the path to strings.
		 * 
		 * @param backing The backing map.
		 * @param keyTranslator The translator for keys.
		 */
		public TableTree( Map<String,V> backing, Translator<K> keyTranslator ) {
			
			this.backing = backing;
			this.keyTranslator = keyTranslator;
			
		}
		
		/**
		 * Obtains the String that represents a path.
		 * 
		 * @param path The path to get the address string for.
		 * @return The address string.
		 * @throws DatabaseException if an error occurred while translating the path.
		 */
		private String getAddressString( List<K> path ) throws DatabaseException {
			
			List<String> translatedPath = new ArrayList<>( path.size() );
			for ( K key : path ) { // Translate each key in the path.
				
				try {
					translatedPath.add( keyTranslator.encode( key ) );
				} catch ( IOException e ) {
					throw new DatabaseException( "Could not translate key.", e );
				}
				
			}
			return Utils.encodeList( translatedPath ); // Encode into a string.
			
		}
		
		/**
		 * Obtains the path that is represented by the given String.
		 * 
		 * @param address The address string.
		 * @return The decoded path.
		 * @throws DatabaseException if an error occurred while translating the path.
		 */
		private List<K> getFromAddressString( String address ) throws DatabaseException {
			
			List<String> translatedPath = Utils.decodeList( address ); // Decode from string.
			List<K> path = new ArrayList<>();
			for ( String key : translatedPath ) { // Decode each key.
				
				try {
					path.add( keyTranslator.decode( key ) );
				} catch ( IOException e ) {
					throw new DatabaseException( "Could not translate key.", e );
				}
				
			}
			return path;
			
		}

		@Override
		public boolean containsValue( V value ) {

			return backing.containsValue( value );
			
		}

		@Override
		public V get( List<K> path ) throws IllegalArgumentException {
			
			return backing.get( getAddressString( path ) );
			
		}

		@Override
		public List<V> getAll( List<K> path ) throws IllegalArgumentException {

			List<V> result = new LinkedList<>();
			for ( int i = 0; i <= path.size(); i++ ) { // Check each step.
				
				V value = get( path.subList( 0, i ) );
				if ( value != null ) { // Found a value for this step.
					result.add( value );
				}
				
			}
			return new ArrayList<>( result );
			
		}

		@Override
		public V set( V value, List<K> path )
				throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {

			return backing.put( getAddressString( path ), value );
			
		}

		@Override
		public boolean add( V value, List<K> path )
				throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {

			String address = getAddressString( path );
			if ( backing.containsKey( address ) ) { // Already has a mapping.
				return false;
			} else { // No mapping yet.
				backing.put( address, value );
				return true;
			}
			
		}

		@Override
		public V remove( List<K> path ) throws UnsupportedOperationException, IllegalArgumentException {

			return backing.remove( getAddressString( path ) );
			
		}

		@Override
		public Set<List<K>> pathSet() {

			final Set<String> backing = this.backing.keySet();
			return new Set<List<K>>() {

				@Override
				public int size() {

					return backing.size();
					
				}

				@Override
				public boolean isEmpty() {

					return backing.isEmpty();
					
				}

				@Override
				public boolean contains( Object o ) {

					if ( !( o instanceof List ) ) {
						return false; // Not a list.
					}
					
					try {
						@SuppressWarnings("unchecked")
						List<K> path = (List<K>) o;
						return backing.contains( getAddressString( path ) );
					} catch ( ClassCastException e ) {
						return false; // Wrong type of list.
					}
					
				}

				@Override
				public Iterator<List<K>> iterator() {

					final Iterator<String> backingIter = backing.iterator();
					return new Iterator<List<K>>() {

						@Override
						public boolean hasNext() {

							return backingIter.hasNext();
							
						}

						@Override
						public List<K> next() {

							return getFromAddressString( backingIter.next() );
							
						}
						
						@Override
						public void remove() {
							
							backingIter.remove();
							
						}

					};
					
				}

				@Override
				public Object[] toArray() {

					return toArray( new Object[0] );
					
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T[] toArray( T[] a ) {
					
					Object[] content = backing.toArray();
					a = (T[]) Array.newInstance( a.getClass().getComponentType(), content.length );
					for ( int i = 0; i < content.length; i++ ) {
						
						a[i] = (T) getFromAddressString( (String) content[i] );
						
					}
					return a;
					
					
				}

				@Override
				public boolean add( List<K> e ) {

					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean remove( Object o ) {

					if ( !( o instanceof List ) ) {
						return false; // Not a list.
					}
					
					try {
						@SuppressWarnings("unchecked")
						List<K> path = (List<K>) o;
						return backing.remove( getAddressString( path ) );
					} catch ( ClassCastException e ) {
						return false; // Wrong type of list.
					}
					
				}

				@Override
				public boolean containsAll( Collection<?> c ) {

					for ( Object o : c ) {
						
						if ( !contains( o ) ) {
							return false;
						}
						
					}
					return true;
					
				}

				@Override
				public boolean addAll( Collection<? extends List<K>> c ) {
					
					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean retainAll( Collection<?> c ) {
					
					Collection<List<K>> removalList = new LinkedList<>();
					for ( List<K> k : this ) {
						
						if ( !c.contains( k ) ) {
							removalList.add( k );
						}
						
					}
					for ( List<K> k : removalList ) {
						
						remove( k );
						
					}
					return !removalList.isEmpty();
					
				}

				@Override
				public boolean removeAll( Collection<?> c ) {
						
					boolean changed = false;
					for ( Object o : c ) {
						
						if ( remove( o ) ) {
							changed = true;
						}
						
					}
					return changed;
					
				}

				@Override
				public void clear() {

					backing.clear();
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					return ( o instanceof Set ) && ( ( (Set<?>) o ).size() == size() )
							&& containsAll( (Set<?>) o );
					
				}
				
				@Override
				public int hashCode() {
					
					int code = 0;
					for ( List<K> path : this ) {
						
						code += path.hashCode();
						
					}
					return code;
					
				}
				
			};
			
		}

		@Override
		public Collection<V> values() {

			return backing.values();
			
		}

		@Override
		public Set<Entry<K,V>> entrySet() {

			final Set<Map.Entry<String,V>> backing = this.backing.entrySet();
			return new Set<Entry<K,V>>() {
				
				/**
				 * Graph entry backed by a Map entry.
				 * 
				 * @version 1.0
				 * @author ThiagoTGM
				 * @since 2018-08-10
				 */
				final class BackedEntry implements Entry<K,V>{
					
					private final List<K> path;
					private final Map.Entry<String,V> backing;
					
					/**
					 * Instantiates an entry backed by the given Map entry.
					 * 
					 * @param backing The backing entry.
					 */
					public BackedEntry( Map.Entry<String,V> backing ) {
						
						this.backing = backing;
						this.path = getFromAddressString( backing.getKey() );
						
					}

					@Override
					public List<K> getPath() {

						return new ArrayList<>( path );
						
					}

					@Override
					public V getValue() {

						return backing.getValue();
						
					}

					@Override
					public V setValue( V value ) throws NullPointerException {

						return backing.setValue( value );
						
					}
					
					@Override
					@SuppressWarnings("unchecked")
					public boolean equals( Object o ) {
						
						return ( o instanceof Entry ) && getPath().equals( ( (Entry<K,V>) o ).getPath() )
								&& getValue().equals( ( (Entry<K,V>) o ).getValue() );
						
					}
					
					@Override
					public int hashCode() {
						
						return getPath().hashCode() ^ getValue().hashCode();
						
					}
					
				}
				
				/**
				 * Map entry backed by a Graph entry.
				 * 
				 * @version 1.0
				 * @author ThiagoTGM
				 * @since 2018-08-10
				 */
				final class ReverseBackedEntry implements Map.Entry<String,V> {

					private final String key;
					private final Entry<K,V> backing;
					
					/**
					 * Instantiates an entry backed by the given Graph entry.
					 * 
					 * @param backing The backing entry.
					 */
					public ReverseBackedEntry( Entry<K,V> backing ) {
						
						this.backing = backing;
						this.key = getAddressString( backing.getPath() );
						
					}
					
					@Override
					public String getKey() {

						return key;
						
					}

					@Override
					public V getValue() {

						return backing.getValue();
						
					}

					@Override
					public V setValue( V value ) {

						return backing.setValue( value );
						
					}
					
					@Override
					@SuppressWarnings("unchecked")
					public boolean equals( Object o ) {
						
						return ( o instanceof Map.Entry )
								&& getKey().equals( ( (Map.Entry<String,V>) o ).getKey() )
								&& getValue().equals( ( (Map.Entry<String,V>) o ).getValue() );
						
					}
					
					@Override
					public int hashCode() {
						
						return getKey().hashCode() ^ getValue().hashCode();
						
					}
					
				}

				@Override
				public int size() {

					return backing.size();
					
				}

				@Override
				public boolean isEmpty() {

					return backing.isEmpty();
					
				}

				@Override
				public boolean contains( Object o ) {

					if ( !( o instanceof Entry ) ) {
						return false; // Not an entry.
					}
					
					try {
						@SuppressWarnings("unchecked")
						Entry<K,V> entry = (Entry<K,V>) o;
						return backing.contains( new ReverseBackedEntry( entry ) );
					} catch ( ClassCastException e ) {
						return false; // Wrong type of entry.
					}
					
				}

				@Override
				public Iterator<Entry<K,V>> iterator() {

					final Iterator<Map.Entry<String,V>> backingIter = backing.iterator();
					return new Iterator<Entry<K,V>>() {

						@Override
						public boolean hasNext() {

							return backingIter.hasNext();
							
						}

						@Override
						public Entry<K,V> next() {

							return new BackedEntry( backingIter.next() );
							
						}
						
						@Override
						public void remove() {
							
							backingIter.remove();
							
						}

					};
					
				}

				@Override
				public Object[] toArray() {

					return toArray( new Object[0] );
					
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T[] toArray( T[] a ) {
					
					Object[] content = backing.toArray();
					a = (T[]) Array.newInstance( a.getClass().getComponentType(), content.length );
					for ( int i = 0; i < content.length; i++ ) {
						
						a[i] = (T) new BackedEntry( (Map.Entry<String,V>) content[i] );
						
					}
					return a;
					
					
				}

				@Override
				public boolean add( Entry<K,V> e ) {

					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean remove( Object o ) {

					if ( !( o instanceof Entry ) ) {
						return false; // Not an entry.
					}
					
					try {
						@SuppressWarnings("unchecked")
						Entry<K,V> entry = (Entry<K,V>) o;
						return backing.remove( new ReverseBackedEntry( entry ) );
					} catch ( ClassCastException e ) {
						return false; // Wrong type of entry.
					}
					
				}

				@Override
				public boolean containsAll( Collection<?> c ) {

					for ( Object o : c ) {
						
						if ( !contains( o ) ) {
							return false;
						}
						
					}
					return true;
					
				}

				@Override
				public boolean addAll( Collection<? extends Entry<K,V>> c ) {
					
					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean retainAll( Collection<?> c ) {
					
					Collection<Entry<K,V>> removalList = new LinkedList<>();
					for ( Entry<K,V> e : this ) {
						
						if ( !c.contains( e ) ) {
							removalList.add( e );
						}
						
					}
					for ( Entry<K,V> e : removalList ) {
						
						remove( e );
						
					}
					return !removalList.isEmpty();
					
				}

				@Override
				public boolean removeAll( Collection<?> c ) {
						
					boolean changed = false;
					for ( Object o : c ) {
						
						if ( remove( o ) ) {
							changed = true;
						}
						
					}
					return changed;
					
				}

				@Override
				public void clear() {

					backing.clear();
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					return ( o instanceof Set ) && ( ( (Set<?>) o ).size() == size() )
							&& containsAll( (Set<?>) o );
					
				}
				
				@Override
				public int hashCode() {
					
					int code = 0;
					for ( Entry<K,V> entry : this ) {
						
						code += entry.hashCode();
						
					}
					return code;
					
				}
				
			};
			
		}

		@Override
		public int size() {
			
			return backing.size();
			
		}

		@Override
		public void clear() {

			backing.clear();
			
		}
		
		@Override
		public boolean equals( Object o ) {
			
			return ( o instanceof Tree ) && entrySet().equals( ( (Tree<?,?>) o ).entrySet() );
			
		}
		
		@Override
		public int hashCode() {
			
			return entrySet().hashCode();
			
		}
		
	}

}
