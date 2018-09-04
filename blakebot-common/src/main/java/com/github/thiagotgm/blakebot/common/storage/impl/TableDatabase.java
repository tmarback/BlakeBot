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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.translate.ListTranslator;
import com.github.thiagotgm.blakebot.common.utils.Tree;

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

		return Tree.mappedTree( newMap( dataName, new ListTranslator<>( keyTranslator ), valueTranslator ) );
		
	}
	
	/* Partial implementation of the map. */
	
	/**
	 * Base implementation of the Map interface.
	 * <p>
	 * All the methods provided here are based on using other methods
	 * of the Map interface. Implementations are heavily encouraged to override
	 * these with algorithms that are more optimized for the specific database
	 * service they use (in terms of speed and/or memory and/or network use).
	 * 
     * @version 1.0
     * @author ThiagoTGM
     * @since 2018-09-04
	 * @param <K> The type of keys used by the Map.
	 * @param <V> The type of values stored by the Map.
	 */
	protected abstract class AbstractTableMap<K,V> implements Map<K,V> {

		@Override
		public boolean isEmpty() {

			return size() == 0;
			
		}
		
		@Override
		public boolean containsKey( Object key ) {
			
			return get( false ) != null;
			
		}

		@Override
		public boolean containsValue( Object value ) {
			
			for ( V val : values() ) {
				
				if ( value == null ? val == null : value.equals( val ) ) {
					return true;
				}
				
			}
			return false;
			
		}

		@Override
		public void putAll( Map<? extends K,? extends V> m ) {

			// Put each entry.
			for ( Map.Entry<? extends K,? extends V> entry : m.entrySet() ) {
				
				put( entry.getKey(), entry.getValue() );
				
			}
			
		}

		@Override
		public void clear() {

			for ( Iterator<Map.Entry<K,V>> iter = entrySet().iterator(); iter.hasNext(); ) {
				
				iter.next();
				iter.remove();
				
			}
			
		}
		
		@Override
		public boolean equals( Object o ) {
			
			return ( o instanceof Map ) && entrySet().equals( ( (Map<?,?>) o ).entrySet() );
			
		}
		
		@Override
		public int hashCode() {
			
			int sum = 0;
			
			for ( Map.Entry<K,V> entry : entrySet() ) {
				
				sum += entry.hashCode();
				
			}
			
			return sum;
			
		}
		
		@Override
		public Set<K> keySet() {
			
			return new KeySet();
			
		}

		@Override
		public Collection<V> values() {

			return new ValueCollection();
			
		}
		
		/* Base data structures */
		
		/**
		 * Shared implementations for the Collection and Set views of the map.
		 * 
		 * @version 1.0
	     * @author ThiagoTGM
	     * @since 2018-09-04
		 * @param <E> The type of objects in the collection.
		 */
		private abstract class AbstractCollection<E> implements Collection<E> {
			
			@Override
			public int size() {

				return AbstractTableMap.this.size();
				
			}

			@Override
			public boolean isEmpty() {
				
				return AbstractTableMap.this.isEmpty();
						
			}
			
			@Override
			public Object[] toArray() {
				
				return toArray( new Object[0] );
				
			}

			@Override
			public <T> T[] toArray( T[] a ) {
				
				List<E> elems = new LinkedList<>(); 
				for ( E elem : this ) { // Get all elements.
					
					elems.add( elem );
					
				}
				return elems.toArray( a ); // Put into array.
				
			}

			@Override
			public boolean add( E e ) {

				throw new UnsupportedOperationException();
				
			}
			
			@Override
			public boolean containsAll( Collection<?> c ) {

				for ( Object elem : c ) {
					
					if ( !contains( elem ) ) {
						return false; // Found not contained.
					}
					
				}
				return true;
				
			}

			@Override
			public boolean addAll( Collection<? extends E> c ) {
				
				throw new UnsupportedOperationException();
				
			}

			@Override
			public boolean retainAll( Collection<?> c ) {

				boolean changed = false;
				
				for ( Iterator<E> iter = iterator(); iter.hasNext(); ) {
					
					if ( !c.contains( iter.next() ) ) {
						iter.remove();
						changed = true;
					}
					
				}
				
				return changed;
				
			}
			
			@Override
			public void clear() {

				AbstractTableMap.this.clear();
				
			}
			
		} // End of AbstractCollection.
		
		/**
		 * Shared implementations for the Set views of the map.
		 * 
		 * @version 1.0
	     * @author ThiagoTGM
	     * @since 2018-09-04
		 * @param <E> The type of objects in the set.
		 */
		private abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E> {
			
			@Override
			public boolean removeAll( Collection<?> c ) {
				
				boolean changed = false;
				
				for ( Object elem : c ) {
					
					if ( remove( elem ) ) {
						changed = true;
					}
					
				}
				
				return changed;
				
			}
			
			@Override
			public boolean equals( Object o ) {
				
				if ( !( o instanceof Set ) ) {
					return false;
				}
				
				Set<?> other = (Set<?>) o;
				
				return ( size() == other.size() ) && containsAll( other );
				
			}
			
			@Override
			public int hashCode() {
				
				int sum = 0;
				
				for ( E elem : this ) {
					
					sum += elem == null ? 0 : elem.hashCode();
					
				}
				
				return sum;
				
			}
			
		} // End of AbstractSet.
		
		/* Implementations for Set and Collection views */
		
		/**
		 * Base implementation of the Map's key set view.
		 * <p>
		 * All the methods provided here are based on using other Set operations,
		 * the map itself, and the map's entry set view. Implementations are heavily
		 * encouraged to override these with algorithms that are more optimized for
		 * the specific database service they use (in terms of speed and/or memory
		 * and/or network use).<br>
		 * In the case a more specific implementation is used,
		 * {@link AbstractTableMap#keySet()} must be overridden to use that implementation.
		 * 
	     * @version 1.0
	     * @author ThiagoTGM
	     * @since 2018-09-04
		 */
		protected class KeySet extends AbstractSet<K> {

			@Override
			public boolean contains( Object o ) {

				return AbstractTableMap.this.containsKey( o );
				
			}

			@Override
			public Iterator<K> iterator() {

				final Iterator<Map.Entry<K,V>> backing = AbstractTableMap.this.entrySet().iterator();
				return new Iterator<K>() {

					@Override
					public boolean hasNext() {

						return backing.hasNext();
						
					}

					@Override
					public K next() {

						return backing.next().getKey();
						
					}

					@Override
					public void remove() {
						
						backing.remove(); // Delegate.
						
					}
					
				};
				
			}

			@Override
			public boolean remove( Object o ) {

				if ( contains( o ) ) { // Key exists.
					AbstractTableMap.this.remove( o ); // Remove it.
					return true;
				} else { // Key doesn't exist.
					return false;
				}
				
			}
			
		} // End of class AbstractKeySet.
		
		/**
		 * Base implementation of the Map's value collection view.
		 * <p>
		 * All the methods provided here are based on using other Collection operations,
		 * the map itself, and the map's entry set view. Implementations are heavily
		 * encouraged to override these with algorithms that are more optimized for
		 * the specific database service they use (in terms of speed and/or memory
		 * and/or network use).<br>
		 * In the case a more specific implementation is used,
		 * {@link AbstractTableMap#values()} must be overridden to use that implementation.
		 * 
	     * @version 1.0
	     * @author ThiagoTGM
	     * @since 2018-09-04
		 */
		protected class ValueCollection extends AbstractCollection<V> {

			@Override
			public boolean contains( Object o ) {

				return AbstractTableMap.this.containsValue( o );
				
			}

			@Override
			public Iterator<V> iterator() {

				final Iterator<Map.Entry<K,V>> backing = AbstractTableMap.this.entrySet().iterator();
				return new Iterator<V>() {

					@Override
					public boolean hasNext() {

						return backing.hasNext();
						
					}

					@Override
					public V next() {

						return backing.next().getValue();
						
					}

					@Override
					public void remove() {
						
						backing.remove(); // Delegate.
						
					}
					
				};
				
			}

			@Override
			public boolean remove( Object o ) {

				for ( Iterator<V> iter = iterator(); iter.hasNext(); ) {
					// Search through values.
					V next = iter.next();
					if ( o == null ? next == null : o.equals( next ) ) {
						iter.remove();
						return true; // Found object.
					}
					
				}
				return false; // Didn't find object.
				
			}

			@Override
			public boolean removeAll( Collection<?> c ) {

				boolean changed = false;
				
				for ( Iterator<V> iter = iterator(); iter.hasNext(); ) {
					
					if ( c.contains( iter.next() ) ) {
						iter.remove(); // Remove matching element.
						changed = true;
					}
					
				}
				
				return changed;
				
			}
			
		} // End of AbstractValueCollection.
	
		/**
		 * Base implementation of the Map's entry set view.
		 * <p>
		 * All the methods provided here are based on using other Set operations
		 * and the map itself. Implementations are heavily encouraged to override
		 * these with algorithms that are more optimized for the specific database
		 * service they use (in terms of speed and/or memory and/or network use).
		 * 
	     * @version 1.0
	     * @author ThiagoTGM
	     * @since 2018-09-04
		 */
		protected abstract class AbstractEntrySet extends AbstractSet<Map.Entry<K,V>> {

			@Override
			public boolean contains( Object o ) {

				if ( !( o instanceof Map.Entry ) ) {
					return false; // Wrong type.
				}
				
				Map.Entry<?,?> other = (Map.Entry<?,?>) o;
				
				if ( !AbstractTableMap.this.containsKey( other.getKey() ) ) {
					return false; // No mapping with this key.
				}
				
				V value = AbstractTableMap.this.get( other.getKey() );
				return value == null ? other.getValue() == null : value.equals( other.getValue() );
				
			}

			@Override
			public boolean remove( Object o ) {
				
				if ( !( o instanceof Map.Entry ) ) {
					return false; // Not an entry.
				}
				
				Map.Entry<?,?> other = (Map.Entry<?,?>) o;
				
				if ( contains( other ) ) { // Entry exists.
					AbstractTableMap.this.remove( other.getKey() ); // Delete based on key.
					return true;
				} else { // Entry does not match any mapping.
					return false;
				}

			}
			
			/**
			 * Convenience base implementation for a map entry that
			 * provides {@link #equals(Object)} and {@link #hashCode()}.
			 * 
			 * @version 1.0
		     * @author ThiagoTGM
		     * @since 2018-09-04
			 */
			protected abstract class AbstractEntry implements Map.Entry<K,V> {
				
				@Override
				public boolean equals( Object o ) {
					
					if ( !( o instanceof Map.Entry ) ) {
						return false; // Wrong type.
					}
					
					Map.Entry<?,?> other = (Map.Entry<?,?>) o;
					
					K key = getKey();
					V value = getValue();
					
					return ( key == null ? other.getKey() == null :
						                   key.equals( other.getKey() ) ) &&
						   ( value == null ? other.getValue() == null :
							                 value.equals( other.getValue() ) );
					
				}
				
				@Override
				public int hashCode() {
					
					K key = getKey();
					V value = getValue();
					
					return ( key == null ? 0 : key.hashCode() ) ^
				         ( value == null ? 0 : value.hashCode() );

					
				}
				
			}
			
		} // End of AbstractEntrySet.
		
	} // End of class AbstractTableMap.
	
}
