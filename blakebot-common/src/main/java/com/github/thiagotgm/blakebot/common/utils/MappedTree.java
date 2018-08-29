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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tree that uses a map as backing storage, by using the path list as a key.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-28
 * @param <K> The type of keys in the path.
 * @param <V> The type of values being stored.
 */
class MappedTree<K,V> implements Tree<K,V> {
	
	private final Map<List<K>,V> backing;
	
	/**
	 * Instantiates a graph that is backed by the given map.
	 * 
	 * @param backing The backing map.
	 */
	public MappedTree( Map<List<K>,V> backing ) {
		
		this.backing = backing;
		
	}

	@Override
	public boolean containsValue( V value ) {

		return backing.containsValue( value );
		
	}

	@Override
	public V get( List<K> path ) throws IllegalArgumentException {
		
		return backing.get( path );
		
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

		return backing.put( path, value );
		
	}

	@Override
	public boolean add( V value, List<K> path )
			throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {

		if ( backing.containsKey( path ) ) { // Already has a mapping.
			return false;
		} else { // No mapping yet.
			backing.put( path, value );
			return true;
		}
		
	}

	@Override
	public V remove( List<K> path ) throws UnsupportedOperationException, IllegalArgumentException {

		return backing.remove( path );
		
	}

	@Override
	public Set<List<K>> pathSet() {

		return this.backing.keySet();
		
	}

	@Override
	public Collection<V> values() {

		return backing.values();
		
	}

	@Override
	public Set<Entry<K,V>> entrySet() {

		final Set<Map.Entry<List<K>,V>> backing = this.backing.entrySet();
		return new Set<Entry<K,V>>() {
			
			/**
			 * Graph entry backed by a Map entry.
			 * 
			 * @version 1.0
			 * @author ThiagoTGM
			 * @since 2018-08-10
			 * @param <GK> Type of the keys that form a path in the graph.
			 * @param <GV> Type of values stored in the graph.
			 */
			final class BackedEntry<GK,GV> implements Entry<GK,GV>{
				
				private final Map.Entry<List<GK>,GV> backing;
				
				/**
				 * Instantiates an entry backed by the given Map entry.
				 * 
				 * @param backing The backing entry.
				 */
				public BackedEntry( Map.Entry<List<GK>,GV> backing ) {
					
					this.backing = backing;
					
				}

				@Override
				public List<GK> getPath() {

					return backing.getKey();
					
				}

				@Override
				public GV getValue() {

					return backing.getValue();
					
				}

				@Override
				public GV setValue( GV value ) throws NullPointerException {

					return backing.setValue( value );
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					return ( o instanceof Entry ) && getPath().equals( ( (Entry<?,?>) o ).getPath() )
							&& getValue().equals( ( (Entry<?,?>) o ).getValue() );
					
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
			 * @param <MK> Type of the keys in the map.
			 * @param <MV> Type of values stored in the map.
			 */
			final class ReverseBackedEntry<MK,MV> implements Map.Entry<List<MK>,MV> {

				private final Entry<MK,MV> backing;
				
				/**
				 * Instantiates an entry backed by the given Graph entry.
				 * 
				 * @param backing The backing entry.
				 */
				public ReverseBackedEntry( Entry<MK,MV> backing ) {
					
					this.backing = backing;
					
				}
				
				@Override
				public List<MK> getKey() {

					return backing.getPath();
					
				}

				@Override
				public MV getValue() {

					return backing.getValue();
					
				}

				@Override
				public MV setValue( MV value ) {

					return backing.setValue( value );
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					return ( o instanceof Map.Entry )
							&& getKey().equals( ( (Map.Entry<?,?>) o ).getKey() )
							&& getValue().equals( ( (Map.Entry<?,?>) o ).getValue() );
					
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
				
				return backing.contains( new ReverseBackedEntry<>( (Entry<?,?>) o ) );
				
			}

			@Override
			public Iterator<Entry<K,V>> iterator() {

				final Iterator<Map.Entry<List<K>,V>> backingIter = backing.iterator();
				return new Iterator<Entry<K,V>>() {

					@Override
					public boolean hasNext() {

						return backingIter.hasNext();
						
					}

					@Override
					public Entry<K,V> next() {

						return new BackedEntry<>( backingIter.next() );
						
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
					
					a[i] = (T) new BackedEntry<>( (Map.Entry<List<K>,V>) content[i] );
					
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
				
				return backing.remove( new ReverseBackedEntry<>( (Entry<?,?>) o ) );
				
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