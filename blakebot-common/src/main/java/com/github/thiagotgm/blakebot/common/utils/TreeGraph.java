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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Implementation of the {@link Tree} interface. The root node (empty path) is accessible.
 * <p>
 * Can only be serialized properly if all the values stored are also Serializable.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-16
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public class TreeGraph<K,V> extends AbstractGraph<K,V> implements Tree<K,V>, Serializable {
    
    /**
     * UID that represents the class.
     */
    private static final long serialVersionUID = -8738939021703848608L;
    
    /**
     * Node that is the root of the tree.
     */
    protected Node root;
    
    /**
     * How many mappings are stored in this graph.
     */
    protected int nMappings;
    
    /**
     * Constructs a TreeGraph with an empty root (the root node exists, but is empty).<br>
     * Same as calling {@link #TreeGraph(Object) TreeGraph(V)} with argument <b>null</b>.
     */
    public TreeGraph() {
        
        this.root = new Node();
        this.nMappings = 0;
        
    }
    
    /**
     * Constructs a TreeGraph where the root contains the given value.
     *
     * @param rootValue The value to be stored in the root of the tree.
     *                  If <tt>null</tt>, no value is stored in the root.
     */
    public TreeGraph( V rootValue ) {
        
        this();
        this.root.setValue( rootValue );
        if ( rootValue != null ) {
            this.nMappings++;
        }
        
    }
    
    @Override
    public boolean containsValue( V value ) {
    	
    	if ( value == null ) {
    		return false; // No null values allowed.
    	}
    	
    	return root.findValue( value, new Stack<>() ) != null;
    	
    }
    
    /**
     * Retrieves the descendant of a given element that represent the given sequence
     * of keys.
     *
     * @param parent The parent element.
     * @param path The sequence of keys that represent the descendants.
     * @return The descendant. If no keys given, the parent.<br>
     *         If there is not an element that corresponds to the given path, null.
     */
    protected Node getDescendant( Node parent, List<K> path ) {
        
        Node element = parent;
        for ( K next : path ) {
            
            element = element.getChild( next );
            if ( element == null ) {
                return null;
            }
            
        }
        return element;
        
    }
    
    /**
     * Retrieves the descendant of the root element that represent the given sequence
     * of keys.
     *
     * @param path The sequence of keys that represent the descendants.
     * @return The descendant. If no keys given, the root.<br>
     *         If there is not an element that corresponds to the given path, null.
     */
    protected Node getDescendant( List<K> path ) {
        
        return getDescendant( root, path );
        
    }
    
    /**
     * Retrieves the descendant of a given element that represent the given sequence
     * of keys.<br>
     * If there is no descendant that corresponds to the full path, retrieves the
     * one that corresponds to as much of it as possible (without skipping parts of
     * the path). May be the given element itself.
     *
     * @param parent The parent element.
     * @param path The sequence of keys that represent the descendants.
     * @return The farthest descendant found. If no keys given, the parent.
     */
    protected Node getMaxDescendant( Node parent, List<K> path ) {
        
        Node element = parent;
        for ( K obj : path ) {
            
            Node child = element.getChild( obj );
            if ( child == null ) {
                return element;
            }
            element = child;
            
        }
        return element;
        
    }
    
    /**
     * Retrieves the descendant of the root element that represent the given sequence
     * of keys.<br>
     * If there is no descendant that corresponds to the full path, retrieves the
     * one that corresponds to as much of it as possible (without skipping parts of
     * the path). May be the root iself.
     *
     * @param path The sequence of keys that represent the descendants.
     * @return The farthest descendant found. If no keys given, the root.
     */
    protected Node getMaxDescendant( List<K> path ) {
        
        return getMaxDescendant( root, path );
        
    }
    
    /**
     * Retrieves the descendant of a given element that represent the given sequence
     * of keys.<br>
     * If the child doesn't exist, creates it (as well as intermediate descendants
     * as necessary).
     *
     * @param parent The parent element.
     * @param path The sequence of objects that represent the descendants.
     * @return The descendant. If no objects given, the parent.
     */
    protected Node getOrCreateDescendant( Node parent, List<K> path ) {
        
        Node element = parent;
        for ( K obj : path ) {
            
            element = element.getOrCreateChild( obj );
            
        }
        return element;
        
    }
    
    /**
     * Retrieves the descendant of the root element that represent the given sequence
     * of objects.<br>
     * If the child doesn't exist, creates it (as well as intermediate descendants
     * as necessary).
     *
     * @param path The sequence of objects that represent the descendants.
     * @return The descendant. If no objects given, the root.
     */
    protected Node getOrCreateDescendant( List<K> path ) {
        
        return getOrCreateDescendant( root, path );
        
    }
    
    @Override
    public V get( List<K> path ) {
        
        Node node = getDescendant( path );
        return ( node == null ) ? null : node.getValue();
        
    }
    
    @Override
    public List<V> getAll( List<K> path ) {
        
        Node cur = root;
        List<V> values = new LinkedList<>();
        for ( K key : path ) {
            
            cur = cur.getChild( key );
            if ( cur != null ) { // Add child's value, if there is one.
                if ( cur.getValue() != null ) {
                    values.add( cur.getValue() );
                }
            } else {
                break; // No child for this key.
            }
            
        }
        return values;
        
    }
    
    @Override
    public V set( V value, List<K> path ) throws NullPointerException {
        
        if ( value == null ) {
            throw new NullPointerException( "Value cannot be null." );
        }
        
        V old = getOrCreateDescendant( path ).setValue( value );
        if ( old == null ) { // There wasn't a mapping to this path yet,
            nMappings++;     // so a new mapping was added.
        }
        return old;
        
    }
    
    @Override
    public boolean add( V value, List<K> path ) throws NullPointerException {
        
        if ( value == null ) {
            throw new NullPointerException( "Value cannot be null." );
        }
        
        Node node = getOrCreateDescendant( path );
        if ( node.getValue() != null ) {
            return false; // Already has a value.
        }
        node.setValue( value );
        nMappings++; // A mapping was added.
        return true;
        
    }
    
    @Override
    public V remove( List<K> path ) {
        
        Stack<Node> nodes = new Stack<>();
        Node cur = root;
        for ( K key : path ) {
            
            if ( cur != null ) {
                nodes.add( cur );
            } else {
                return null; // Path has no mapping.
            }
            cur = cur.getChild( key );
            
        }
        
        V value = cur.getValue(); // Store the value of the node of the full path.
        if ( value == null ) {
            return null; // There is already no value for this path.
        }
        cur.setValue( null ); // Delete its value.
        
        for ( int i = path.size() - 1; i >= 0; i-- ) { // Cleans up any nodes that became irrelevant.
            
            if ( ( cur.getValue() == null ) && cur.getChildren().isEmpty() ) {
                cur = nodes.pop(); // Node has no value or children now, so delete it.
                cur.removeChild( path.get( i ) );
            } else {
                break; // Found a node that can't be deleted.
            }
            
        }
        
        nMappings--; // A mapping was removed.
        return value; // Retrieve deleted value.
        
    }
    
    @Override
    public Set<List<K>> pathSet() {
    	
    	return new Set<List<K>>() {

			@Override
			public int size() {

				return TreeGraph.this.size();
				
			}

			@Override
			public boolean isEmpty() {

				return TreeGraph.this.isEmpty();
				
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public boolean contains( Object o ) {

				if ( o instanceof List ) {
					return containsPath( (List<K>) o );
				} else {
					return false; // Wrong type.
				}
				
			}

			@Override
			public Iterator<List<K>> iterator() {
				
				final Iterator<Entry<K,V>> backing = entrySet().iterator();
				return new Iterator<List<K>>() {

					@Override
					public boolean hasNext() {
						
						return backing.hasNext();
						
					}

					@Override
					public List<K> next() {

						return backing.next().getPath();
						
					}
					
					@Override
					public void remove() {
						
						backing.remove();
						
					}
					
				};
				
			}

			@Override
			public Object[] toArray() {

				return toArray( new Object[0] );
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> T[] toArray( T[] a )  {

				Entry<K,V>[] arr = entrySet().toArray( new Entry[0] );
				
				if ( a.length < arr.length ) { // Resize if necessary.
					a = (T[]) Array.newInstance( a.getClass().getComponentType(), arr.length );
				}
				
				int i;
				for ( i = 0; i < arr.length; i++ ) {
					
					try {
						a[i] = (T) arr[i].getPath(); // Insert each element.
					} catch ( ClassCastException e ) {
		        		throw new ArrayStoreException( "Cannot store in given array type." );
		        	}
					
				}
				if ( i < a.length ) {
		        	
		        	a[i] = null; // Fill next space with null, if necessary.
		        	
		        }
				
				return a;
				
			}

			@Override
			public boolean add( List<K> e ) {

				throw new UnsupportedOperationException( "Set view does not support adding." );
				
			}

			@Override
			public boolean remove( Object o ) {

				if ( contains( o ) ) {
					@SuppressWarnings("unchecked")
					List<K> path = (List<K>) o; // Being in graph implies right type.
					TreeGraph.this.remove( path );
					return true; // Removed entry.
				} else {
					return false; // Not contained in graph.
				}
				
			}

			@Override
			public boolean containsAll( Collection<?> c ) {
				
				for ( Object o : c ) {
					
					if ( !contains( o ) ) {
						return false; // Found element that is not contained.
					}
					
				}
				return true; // All contained.
				
			}

			@Override
			public boolean addAll( Collection<? extends List<K>> c ) {
				
				throw new UnsupportedOperationException( "Set view does not support adding." );
				
			}

			@Override
			public boolean retainAll( Collection<?> c ) {
				
				List<List<K>> toRemove = new LinkedList<>();
				for ( List<K> path : this ) {
					
					if ( !c.contains( path ) ) { // Not in given collection.
						toRemove.add( path ); // Mark for deletion.
					}
					
				}
				
				for ( List<K> path : toRemove ) {
					
					remove( path ); // Remove each marked entry.
					
				}
				
				return !toRemove.isEmpty();
				
			}

			@Override
			public boolean removeAll( Collection<?> c ) {
				
				boolean changed = false;
				for ( Object o : c ) {
					
					if ( remove( o ) ) { // Try to remove element.
						changed = true;
					}
					
				}
				return changed;

			}

			@Override
			public void clear() {
				
				TreeGraph.this.clear();
				
			}
			
			@Override
			public boolean equals( Object o ) {
				
				if ( !( o instanceof Set ) ) {
					return false;
				}
				
				Set<?> other = (Set<?>) o;
				
				if ( other.size() != this.size() ) {
					return false; // Must be of the same size.
				}
				
				for ( Object elem : other ) {
					
					if ( !contains( elem ) ) {
						return false; // One element not in this set.
					}
					
				}
				
				return true;
				
			}
			
			@Override
			public int hashCode() {
				
				int sum = 0;
				for ( List<K> elem : this ) {
					
					sum += elem.hashCode();
					
				}
				return sum;
				
			}
			
			@Override
			public String toString() {
				
				StringBuilder builder = new StringBuilder();
				
				builder.append( '[' );
				for ( List<K> elem : this ) {
					
					builder.append( elem.toString() );
					builder.append( ", " );
					
				}
				builder.delete( builder.length() - 2, builder.length() );
				builder.append( ']' );
				
				return builder.toString();
				
			}
    		
    	};
    	
    }
    
    @Override
    public Collection<V> values() {
    	
    	return new Collection<V>() {

			@Override
			public int size() {

				return TreeGraph.this.size();
				
			}

			@Override
			public boolean isEmpty() {

				return TreeGraph.this.isEmpty();
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean contains( Object o ) {

				try {
					return containsValue( (V) o );
				} catch ( ClassCastException e ) {
					return false; // Wrong type.
				}
				
			}

			@Override
			public Iterator<V> iterator() {
				
				final Iterator<Entry<K,V>> backing = entrySet().iterator();
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
						
						backing.remove();
						
					}
					
				};
				
			}

			@Override
			public Object[] toArray() {

				return toArray( new Object[0] );
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> T[] toArray( T[] a )  {

				Entry<K,V>[] arr = entrySet().toArray( new Entry[0] );
				
				if ( a.length < arr.length ) { // Resize if necessary.
					a = (T[]) Array.newInstance( a.getClass().getComponentType(), arr.length );
				}
				
				int i;
				for ( i = 0; i < arr.length; i++ ) {
					
					try {
						a[i] = (T) arr[i].getValue(); // Insert each element.
					} catch ( ClassCastException e ) {
		        		throw new ArrayStoreException( "Cannot store in given array type." );
		        	}
					
				}
				if ( i < a.length ) {
		        	
		        	a[i] = null; // Fill next space with null, if necessary.
		        	
		        }
				
				return a;
				
			}

			@Override
			public boolean add( V e ) {

				throw new UnsupportedOperationException( "Collection view does not support adding." );
				
			}

			@Override
			public boolean remove( Object o ) {

				if ( contains( o ) ) {
					@SuppressWarnings("unchecked")
					V value = (V) o; // Being in graph implies right type.
					remove( root.findValue( value, new Stack<K>() ) );
					return true; // Removed entry.
				} else {
					return false; // Not contained in graph.
				}
				
			}

			@Override
			public boolean containsAll( Collection<?> c ) {
				
				for ( Object o : c ) {
					
					if ( !contains( o ) ) {
						return false; // Found element that is not contained.
					}
					
				}
				return true; // All contained.
				
			}

			@Override
			public boolean addAll( Collection<? extends V> c ) {
				
				throw new UnsupportedOperationException( "Collection view does not support adding." );
				
			}

			@Override
			public boolean retainAll( Collection<?> c ) {
				
				List<V> toRemove = new LinkedList<>();
				for ( V value : this ) {
					
					if ( !c.contains( value ) ) { // Not in given collection.
						toRemove.add( value ); // Mark for deletion.
					}
					
				}
				
				for ( V value : toRemove ) {
					
					remove( value ); // Remove each marked entry.
					
				}
				
				return !toRemove.isEmpty();
				
			}

			@Override
			public boolean removeAll( Collection<?> c ) {
				
				boolean changed = false;
				for ( Object o : c ) {
					
					if ( remove( o ) ) { // Try to remove element.
						changed = true;
					}
					
				}
				return changed;

			}

			@Override
			public void clear() {
				
				TreeGraph.this.clear();
				
			}
			
			@Override
			public boolean equals( Object o ) {
				
				if ( !( o instanceof Collection ) ) {
					return false;
				}
				
				Collection<?> other = (Collection<?>) o;
				
				if ( other.size() != this.size() ) {
					return false; // Must be of the same size.
				}
				
				for ( Object elem : other ) {
					
					if ( !contains( elem ) ) {
						return false; // One element not in this set.
					}
					
				}
				
				return true;
				
			}
			
			@Override
			public int hashCode() {
				
				int sum = 0;
				for ( V elem : this ) {
					
					sum += elem.hashCode();
					
				}
				return sum;
				
			}
			
			@Override
			public String toString() {
				
				StringBuilder builder = new StringBuilder();
				
				builder.append( '[' );
				for ( V elem : this ) {
					
					builder.append( elem.toString() );
					builder.append( ", " );
					
				}
				builder.delete( builder.length() - 2, builder.length() );
				builder.append( ']' );
				
				return builder.toString();
				
			}
    		
    	};
    	
    }
    
    /**
     * Retrieves the set of path-value mappings <i>currently</i> stored in this
     * graph. Changes to the graph are <i>not</i> reflected in the set and vice-versa,
     * except for the {@link Entry#setValue(V)} method.
     * 
     * @return The current entry set.
     */
    protected Set<Entry<K,V>> getEntries() {
    	
    	Set<Entry<K,V>> entries = new HashSet<>();
        root.getEntries( entries, new Stack<>() ); // Get entries.
        return entries;
    	
    }
    
    @Override
    public Set<Entry<K,V>> entrySet() {
        
        return new Set<Entry<K,V>>() {

			@Override
			public int size() {
				
				return TreeGraph.this.size();
				
			}

			@Override
			public boolean isEmpty() {

				return TreeGraph.this.isEmpty();
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean contains( Object o ) {
				
				if ( o instanceof Entry ) {
					Entry<K,V> entry = (Entry<K,V>) o;
					V mapped = TreeGraph.this.get( entry.getPath() );
					// Return null means no entry for the path.
					return mapped == null ? false : mapped.equals( entry.getValue() );
				} else {
					return false;
				}
						
			}

			@Override
			public Iterator<Entry<K,V>> iterator() {
				
				Set<Entry<K,V>> entries = getEntries();
				final Iterator<Entry<K,V>> backing = entries.iterator();
				return new Iterator<Entry<K,V>>() {
					
					private Entry<K,V> last = null;

					@Override
					public boolean hasNext() {

						return backing.hasNext();
						
					}

					@Override
					public Entry<K,V> next() {

						Entry<K,V> next = backing.next();
						last = next;
						return next;
						
					}
					
					@Override
					public void remove() {
						
						backing.remove(); // Delegate validity checks.
						TreeGraph.this.remove( last.getPath() );
						
					}
					
				};
				
			}

			@Override
			public Object[] toArray() {

				return toArray( new Object[0] );
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> T[] toArray( T[] a ) throws ArrayStoreException {
				
				Set<Entry<K,V>> entries = getEntries();
		        
		        if ( a.length < entries.size() ) { // Check if need to resize.
		        	a = (T[]) Array.newInstance( a.getClass().getComponentType(), entries.size() );
		        }
		        
		        int i = 0;
		        for ( Entry<K,V> entry : entries ) {
		        	
		        	try {
		        	a[i] = (T) entry; // Fill each entry in array.
		        	} catch ( ClassCastException e ) {
		        		throw new ArrayStoreException( "Cannot store in given array type." );
		        	}
		        	
		        }
		        if ( i < a.length ) {
		        	
		        	a[i] = null; // Fill next space with null, if necessary.
		        	
		        }
		        
		        return a;
		        
			}

			@Override
			public boolean add( Entry<K,V> e ) {

				throw new UnsupportedOperationException( "Set view does not support adding." );
				
			}

			@Override
			public boolean remove( Object o ) {

				if ( contains( o ) ) {
					@SuppressWarnings("unchecked")
					Entry<K,V> entry = (Entry<K,V>) o; // Being in graph implies right type.
					TreeGraph.this.remove( entry.getPath() );
					return true; // Removed entry.
				} else {
					return false; // Not contained in graph.
				}
				
			}

			@Override
			public boolean containsAll( Collection<?> c ) {
				
				for ( Object o : c ) {
					
					if ( !contains( o ) ) {
						return false; // Found element that is not contained.
					}
					
				}
				return true; // All contained.
				
			}

			@Override
			public boolean addAll( Collection<? extends Entry<K, V>> c ) {
				
				throw new UnsupportedOperationException( "Set view does not support adding." );
				
			}

			@Override
			public boolean retainAll( Collection<?> c ) {
				
				List<Entry<K,V>> toRemove = new LinkedList<>();
				for ( Entry<K,V> entry : this ) {
					
					if ( !c.contains( entry ) ) { // Not in given collection.
						toRemove.add( entry ); // Mark for deletion.
					}
					
				}
				
				for ( Entry<K,V> entry : toRemove ) {
					
					remove( entry ); // Remove each marked entry.
					
				}
				
				return !toRemove.isEmpty();
				
			}

			@Override
			public boolean removeAll( Collection<?> c ) {
				
				boolean changed = false;
				for ( Object o : c ) {
					
					if ( remove( o ) ) { // Try to remove element.
						changed = true;
					}
					
				}
				return changed;

			}

			@Override
			public void clear() {

				TreeGraph.this.clear();
				
			}
			
			@Override
			public boolean equals( Object o ) {
				
				if ( !( o instanceof Set ) ) {
					return false;
				}
				
				Set<?> other = (Set<?>) o;
				
				if ( other.size() != this.size() ) {
					return false; // Must be of the same size.
				}
				
				for ( Object elem : other ) {
					
					if ( !contains( elem ) ) {
						return false; // One element not in this set.
					}
					
				}
				
				return true;
				
			}
			
			@Override
			public int hashCode() {
				
				int sum = 0;
				for ( Entry<K,V> elem : this ) {
					
					sum += elem.hashCode();
					
				}
				return sum;
				
			}
			
			@Override
			public String toString() {
				
				StringBuilder builder = new StringBuilder();
				
				builder.append( '[' );
				for ( Entry<K,V> elem : this ) {
					
					builder.append( elem.toString() );
					builder.append( ", " );
					
				}
				builder.delete( builder.length() - 2, builder.length() );
				builder.append( ']' );
				
				return builder.toString();
				
			}
        	
        };
        
    }

    
    @Override
    public int size() {
        
        return nMappings;
        
    }
    
    @Override
    public void clear() {
        
        this.root = new Node(); // Delete all nodes.
        this.nMappings = 0; // Reset counter.
        
    }
    
    /**
     * A node in the tree.
     * <p>
     * Can only be serialized properly if the value stored is also Serializable.
     *
     * @version 1.1
     * @author ThiagoTGM
     * @since 2017-08-17
     */
    protected class Node implements Serializable {
        
		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = 8085251836873812411L;

		/**
		 * Key that identifies this node in its parent.
		 */
		protected K key;

		/**
		 * Value stored inside this Node.
		 */
		protected V value;

		/**
		 * Children nodes of this Node.
		 */
		protected Map<K,Node> children;

		/**
		 * Constructs a Node with no value, key, or children.
		 */
		public Node() {

			this( null );

		}

		/**
		 * Constructs a Node with the given key, and no value or chilren.
		 *
		 * @param key The key of the node.
		 */
		public Node( K key ) {

			this( key, null );

		}

		/**
		 * Constructs a Node with the given value and key, and no children.
		 *
		 * @param key The key of the node.
		 * @param value The initial value of the node.
		 */
		public Node( K key, V value ) {

			this( key, value, null );

		}
		
		/**
		 * Constructs a Node with the given value, key, and children.
		 *
		 * @param key The key of the node. 
		 * @param value The initial value of the node.
		 * @param children The initial children of the node, If <tt>null</tt>, the
		 *                 node is initialized with no children (same as if given an
		 *                 empty Collection).
		 * @throws IllegalArgumentException if there are two or more children in the given
		 *                                  Collection with the same key.
		 */
		public Node( K key, V value, Collection<Node> children ) throws IllegalArgumentException {
			
			this.key = key;
			this.value = value;
			this.children = new HashMap<>();
			
			if ( children != null ) { // Received children. 
				for ( Node child : children ) { // Add all children.
					
					if ( this.children.put( child.getKey(), child ) != null ) {
						throw new IllegalArgumentException( "Multiple children with the same key." );
					}
					
				}
			}
			
		}
              
        /**
         * Retrieves the key that identifies this node.
         *
         * @return The key of the node.
         */
        public K getKey() {
            
            return key;
            
        }
        
        /**
         * Retrieves the value of the node.
         *
         * @return The value of the node, or <tt>null</tt> if none.
         */
        public V getValue() {
            
            return value;
            
        }
        
        /**
         * Sets the value of the node.
         *
         * @param value The new value of the node.
         * @return The previous value of the node, or <tt>null</tt> if none.
         */
        public V setValue( V value ) {
            
            V oldValue = this.value;
            this.value = value;
            return oldValue;
            
        }
        
        /**
         * Gets the child of this node that corresponds to the given key.
         *
         * @param key The key to get the child for.
         * @return The child that corresponds to the given key, or <tt>null</tt> if there is
         *         no such child.
         */
        public Node getChild( K key ) {
            
            return children.get( key );
            
        }
        
        /**
         * Retrieves all the children of this node.
         *
         * @return The children of this node.
         */
        public Collection<Node> getChildren() {
            
            return children.values();
            
        }
        
        /**
         * Gets the child of this node that corresponds to the given key.<br>
         * Creates it if it does not exist.
         *
         * @param key The key to get the child for.
         * @return The child that corresponds to the given key.
         */
        public Node getOrCreateChild( K key ) {
            
        	Node child = children.get( key );
            if ( child == null ) {
                child = new Node( key );
                children.put( key, child );
            }
            return child;
            
        }
        
        /**
         * Sets the value of the child node that corresponds to the given key to
         * the given value. If there is no node that corresponds to the given key,
         * creates one.
         *
         * @param key The key to get the child for.
         * @param value The value to set for that child.
         * @return The previous value in the child, or <tt>null</tt> if none.
         */
        public final V setChild( K key, V value ) {
            
            return getOrCreateChild( key ).setValue( value );
            
        }
        
        /**
         * Adds a child node that corresponds to the given key and has the given value.<br>
         * If there is already a child for that key, does nothing.
         *
         * @param key The key the child corresponds to.
         * @param value The value of the child.
         * @return true if the child was added. false if there is already a child that
         *         corresponds to the given key.
         */
        public final boolean addChild( K key, V value ) {
            
            Node child = getChild( key );
            if ( child != null ) {
                return false; // Child with that key already exists.
            } else {
                setChild( key, value );
                return true;
            }
            
        }
        
        /**
         * Removes the child node that corresponds to the given key.
         *
         * @param key The key the child corresponds to.
         * @return The deleted child, or null if there is no child for that key.
         */
        public Node removeChild( K key ) {
            
            return children.remove( key );
            
        }
        
        /**
         * Attempts to find a value in the subtree rooted by this node, returning the
         * total path to the node that contains that value.
         * 
         * @param value The value to find.
         * @param path The path to this node's parent (should be empty if this is the 
         *             root of the full tree).
         * @return The path to the node that contains the given value, or <tt>null</tt>
         *         if there is no node in this subtree with that value.
         * @throws NullPointerException if value is <tt>null</tt>.
         */
        public List<K> findValue( V value, Stack<K> path ) throws NullPointerException {
        	
        	if ( value == null ) {
        		throw new NullPointerException( "Value to find cannot be null." );
        	}
        	
        	if ( getKey() != null ) {
                path.push( getKey() ); // Add this node's path.
            }
            
            if ( value.equals( getValue() ) ) { // This node has the value.
                return new ArrayList<>( path ); // Return current path.
            }
            
            /* Recursively search each child */
            for ( Node child : getChildren() ) {
                
                List<K> result = child.findValue( value, path );
                if ( result != null ) {
                	return result; // Found in a subtree.
                }
                
            }
            
            if ( getKey() != null ) {
                path.pop(); // Remove this node's path.
            }
            
            return null; // Not found.
            
        }
        
        /**
         * Retrieves the path-value mapping entries for this node and its children,
         * placing them into the given entry set.
         *
         * @param entries The set to place the entries in.
         * @param path The path that maps to this node, where the bottom of the stack is
         *             the beginning of the path.
         */
        public void getEntries( Set<Entry<K,V>> entries, Stack<K> path ) {
            
            if ( getKey() != null ) {
                path.push( getKey() ); // Add this node's path.
            }
            
            if ( getValue() != null ) { // This node represents a mapping.
                entries.add( new TreeGraphEntry( path, this ) );
            }
            
            /* Recursively gets entries for each child */
            for ( Node child : getChildren() ) {
                
                child.getEntries( entries, path );
                
            }
            
            if ( getKey() != null ) {
                path.pop(); // Remove this node's path.
            }
            
        }
        
        /**
         * Writes the state of this instance to a stream.
         * <p>
         * Writes the key and value if they exist (using a boolean for each to identify
         * whether they do or not exist), then the number of children, then each child
         * node. In this way, the internal Map of children is not serialized, reducing
         * the space overhead.
         *
         * @param out The stream to write data to.
         * @throws IOException if there is an error while writing the state.
         */
        private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
            
            if ( this.key != null ) {
                out.writeBoolean( true ); // Mark that node has a key.
                out.writeObject( key ); // Write the key.
            } else {
                out.writeBoolean( false ); // Mark that node does not have a key.
            }
            if ( this.value != null ) {
                out.writeBoolean( true ); // Mark that node has a value.
                out.writeObject( this.value ); // Write the value.
            } else {
                out.writeBoolean( false ); // Mark that node does not have a value.
            }
            
            /* Write children */
            Collection<Node> children = getChildren();
            out.writeInt( children.size() ); // Write amount of children.
            for ( Node child : children ) {
                
                out.writeObject( child ); // Write child.
                
            }
            
        }
        
        /**
         * Reads the state of this instance from a stream.
         *
         * @param in The stream to read data from.
         * @throws IOException if there is an error while reading the state.
         * @throws ClassNotFoundException if the class of a serialized value object cannot
         *                                be found.
         * @see #writeObject(java.io.ObjectOutputStream)
         */
        private void readObject( java.io.ObjectInputStream in )
                throws IOException, ClassNotFoundException {
            
            if ( in.readBoolean() ) { // Check if a key is stored.
                try {
                    @SuppressWarnings( "unchecked" )
                    K key = (K) in.readObject();
                    this.key = key;
                } catch ( ClassCastException e ) {
                    throw new IOException( "Deserialized node key is not of the expected type.", e );
                }
            }
            if ( in.readBoolean() ) { // Check if a value is stored.
                try {
                    @SuppressWarnings( "unchecked" )
                    V value = (V) in.readObject();
                    this.value = value;
                } catch ( ClassCastException e ) {
                    throw new IOException( "Deserialized node value is not of the expected type.", e );
                }
            }
            
            /* Read children */
            int childNum = in.readInt(); // Retrieve amount of children.
            this.children = new HashMap<>();
            for ( int i = 0; i < childNum; i++ ) { // Read each child.
                
                Node child;
                try { // Read child.
                    @SuppressWarnings( "unchecked" )
                    Node tempChild = (Node) in.readObject();
                    child = tempChild;
                } catch ( ClassCastException e ) {
                    throw new IOException( "Deserialized child node is not of the expected type.", e );
                }
                children.put( child.getKey(), child ); // Store child.
                
            }
            
        }
        
        /**
         * Initializes instance data when this class is needed for deserialization
         * but there is no data available.
         * <p>
         * Key and value are initialized to null, and the map of children is empty.
         *
         * @throws ObjectStreamException if an error occurred.
         */
        @SuppressWarnings( "unused" )
        private void readObjectNoData() throws ObjectStreamException {
            
            this.children = new HashMap<>();
            this.key = null;
            this.value = null;
            
        }
        
    }
    
    /**
     * Represents an entry in the TreeGraph.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-20
     */
    protected class TreeGraphEntry extends AbstractEntry {
        
        private final Node node;
        
        /**
         * Constructs a new entry for the given path that is linked to the given node.
         *
         * @param path The path of this entry.
         * @param node The node that represents this entry.
         */
        public TreeGraphEntry( List<K> path, Node node ) {
            
            super( path );
            this.node = node;
            
        }

        @Override
        public V getValue() {

            return node.getValue();
            
        }

        @Override
        public V setValue( V value ) throws NullPointerException {

            if ( value == null ) {
                throw new NullPointerException( "Value cannot be null." );
            }
            
            return node.setValue( value );
            
        }
        
    }

}
