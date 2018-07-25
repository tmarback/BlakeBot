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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a graph that links sequences of keys to values. The empty
 * path may or may not be valid depending on implementation.
 * <p>
 * Does not allow storing <tt>null</tt> values.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-16
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
@SuppressWarnings( "unchecked" )
public interface Graph<K,V> {
	
	/**
	 * Determines whether there is a mapping in this graph with the
	 * given path.
	 * 
	 * @param path The path to be checked.
	 * @return <tt>true</tt> if there is a mapping in the graph with such
	 *         a path. <tt>false</tt> otherwise.
	 */
	default boolean containsPath( List<K> path ) {
		
		return get( path ) != null;
		
	}
	
	/**
	 * Varargs version of {@link #containsPath(List)}.
	 * 
	 * @param path The path to be checked.
	 * @return <tt>true</tt> if there is a mapping in the graph with such
	 *         a path. <tt>false</tt> otherwise.
	 */
	default boolean containsPath( K... path ) {
		
		return containsPath( Arrays.asList( path ) );
		
	}
	
	/**
	 * Determines whether there is a mapping in this graph with the
	 * given value.
	 * 
	 * @param value The value to be checked.
	 * @return <tt>true</tt> if there is a mapping in the graph with such
	 *         a value. <tt>false</tt> otherwise.
	 */
	boolean containsValue( V value );
    
    /**
     * Retrieves the value mapped to the given sequence of keys.
     * <p>
     * A mapping will be matched if, for the corresponding values <tt>p</tt>
     * and <tt>m</tt> at each step in the path and mapping,
     * <tt>p==null ? m==null : p.equals(m)</tt>. 
     *
     * @param path The sequence of keys that map to the value.
     * @return The value linked to the given path, or null if there is none.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
	V get( List<K> path ) throws IllegalArgumentException;
	
	/**
	 * Varargs version of {@link #get(List)}.
	 * 
	 * @param path The sequence of keys that map to the value.
     * @return The value linked to the given path, or null if there is none.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
	 */
    default V get( K... path ) throws IllegalArgumentException {
    	
    	return get( Arrays.asList( path ) );
    	
    }
    
    /**
     * Retrieves the values mapped to each step of the given sequence of keys.<br>
     * Steps that do not exist or have no mapping are ignored.
     *
     * @param path The sequence of keys that map to the values.
     * @return The values linked to each step of the given path, in the order that
     *         the path is traversed (same order that the keys are given).
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    List<V> getAll( List<K> path ) throws IllegalArgumentException;
    
    /**
     * Varargs version of {@link #getAll(List)}.
     *
     * @param path The sequence of keys that map to the values.
     * @return The values linked to each step of the given path, in the order that
     *         the path is traversed (same order that the keys are given).
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    default List<V> getAll( K... path ) throws IllegalArgumentException {
    	
    	return getAll( Arrays.asList( path ) );
    	
    }
    
    /**
     * Maps a value to a sequence of keys, replacing the value currently mapped
     * to the path, if any.<br>
     * Optional operation.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @return The value previously mapped to that path, or <tt>null</tt> if there
     *         was none.
     * @throws UnsupportedOperationException if the set operation is not supported by this graph.
     * @throws NullPointerException if the value given is null.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    V set( V value, List<K> path ) throws UnsupportedOperationException, NullPointerException, IllegalArgumentException;
    
    /**
     * Varargs version of {@link #set(V, List)}.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @return The value previously mapped to that path, or <tt>null</tt> if there
     *         was none.
     * @throws UnsupportedOperationException if the set operation is not supported by this graph.
     * @throws NullPointerException if the value given is null.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
	default V set( V value, K... path )
			throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {
		
		return set( value, Arrays.asList( path ) );
		
	}
    
    /**
     * Maps a value to a sequence of keys only if there is no current mapping
     * for that path.<br>
     * Optional operation.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @return <tt>true</tt> if the value was added to the graph.<br>
     *         <tt>false</tt> if there is already a value mapped to the given path.
     * @throws UnsupportedOperationException if the add operation is not supported by this map.
     * @throws NullPointerException if the value given is null.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
	boolean add( V value, List<K> path )
			throws UnsupportedOperationException, NullPointerException, IllegalArgumentException;
	
	/**
     * Varargs version of {@link #add(V, List)}.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @return <tt>true</tt> if the value was added to the graph.<br>
     *         <tt>false</tt> if there is already a value mapped to the given path.
     * @throws UnsupportedOperationException if the add operation is not supported by this map.
     * @throws NullPointerException if the value given is null.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    default boolean add( V value, K... path )
    		throws UnsupportedOperationException, NullPointerException, IllegalArgumentException {
    	
    	return add( value, Arrays.asList( path ) );
    	
    }
    
    /**
     * Removes a mapping from this graph.<br>
     * Optional operation.
     *
     * @param path The sequence of keys that map to the value to be removed.
     * @return The removed value, or <tt>null</tt> if there is no mapping for the
     *         given path.
     * @throws UnsupportedOperationException if the remove operation is not supported by this map.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    V remove( List<K> path ) throws UnsupportedOperationException, IllegalArgumentException;
    
    /**
     * Varargs version of {@link #remove(List)}.
     *
     * @param path The sequence of keys that map to the value to be removed.
     * @return The removed value, or <tt>null</tt> if there is no mapping for the
     *         given path.
     * @throws UnsupportedOperationException if the remove operation is not supported by this map.
     * @throws IllegalArgumentException if the path is empty but such a path
     *         is not valid under the current implementation.
     */
    default V remove( K... path ) throws UnsupportedOperationException, IllegalArgumentException {
    	
    	return remove( Arrays.asList( path ) );
    	
    }
    
    /**
     * Returns a {@link Set} view of the paths contained in this graph. The set is backed by the graph,
     * so changes to the graph are reflected in the set, and vice-versa. If the graph is modified
     * while an iteration over the set is in progress (except through the iterator's own <tt>remove</tt>
     * operation), the results of the iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the graph, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     * 
     * @return A set view of the paths contained in this graph.
     */
    Set<List<K>> pathSet();
    
    /**
     * Returns a {@link Collection} view of the values contained in this graph. The collection is backed
     * by the graph, so changes to the graph are reflected in the collection, and vice-versa. If the graph
     * is modified while an iteration over the collection is in progress (except through the iterator's
     * own <tt>remove</tt> operation), the results of the iteration are undefined. The collection supports
     * element removal, which removes the corresponding mapping from the graph, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     * 
     * @return A collection view of the values contained in this graph.
     */
    Collection<V> values();
    
    /**
     * Returns a {@link Set} view of the mappings contained in this graph. The set is backed by the graph,
     * so changes to the graph are reflected in the set, and vice-versa. If the graph is modified while an
     * iteration over the set is in progress (except through the iterator's own <tt>remove</tt> operation,
     * or through the <tt>setValue</tt> operation on a graph entry returned by the iterator) the results of
     * the iteration are undefined. The set supports element removal, which removes the corresponding mapping
     * from the graph, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return A set view of the mappings contained in this graph.
     */
    Set<Entry<K,V>> entrySet();
    
    /**
     * Retrieves the amount of path-value mappings that are stored in
     * this graph.
     *
     * @return The amount of mappings in this graph.
     */
    int size();
    
    /**
     * Determines whether the graph is empty.
     *
     * @return <tt>true</tt> if this map contains no path-value mappings.
     *         <tt>false</tt> otherwise.
     */
    default boolean isEmpty() {
        
        return size() == 0;
        
    }
    
    /**
     * Removes all mappings from this graph.
     */
    void clear();
    
    /**
     * Compares this graph with the specified object for equality. Returns <tt>true</tt>
     * if the specified object is also a Graph, that contains the same values mapped
     * to the same paths.<br>
     * More formally, two graphs <tt>g1</tt> and <tt>g2</tt> are equal if
     * <tt>g1.entrySet().equals(g2.entrySet())</tt>.
     *
     * @param obj The object to compare to.
     * @return <tt>true</tt> if the specified object is equal to this graph, <tt>false</tt>
     *         otherwise.
     */
    @Override
    boolean equals( Object obj );
    
    /**
     * Calculates the hash code of the graph. The hash code of a graph is defined to be the sum of the hash codes
     * of each entry in the graph's entrySet() view. This ensures that <tt>g1.equals(g2)</tt> implies that
     * <tt>g1.hashCode()==g2.hashCode()</tt> for any two graphs <tt>g1</tt> and <tt>g2</tt>.
     *
     * @return The hash code of this Graph.
     */
    @Override
    int hashCode();
    
    /**
     * A path-value entry in a Graph. The {@link Graph#entrySet()} method returns a Collection view
     * of the graph with members of this class. The only way to obtain an entry is through the 
     * iterator of that set.<br>
     * If the backing graph is modified in any way other than the {@link #setValue(Object) setValue}
     * method of an Entry, the behavior is undefined.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-20
     * @param <K> Type of the keys that form the path.
     * @param <V> Type of the values stored in the graph.
     */
    static interface Entry<K,V> extends Map.Entry<List<K>,V> {
        
        /**
         * Retrieves the path represented by this Entry.<br>
         * The returned list is unmodifiable.
         *
         * @return The path of this entry. Will never be null, but may be empty.
         */
        List<K> getPath();
        
        /**
         * Retrieves the path represented by this Entry, stored in an array.<br>
         * Changes to the returned array are not reflected on the entry.
         * <p>
         * Convenience method for using the path directly into a Graph's vararg methods.
         *
         * @return An array containing the path of this entry. Will never be null, but may
         *         be empty.
         * @see #getPath()
         */
        default K[] getPathArray() {
            
            List<K> path = getPath();
            K[] array = (K[]) new Object[ path.size() ];
            return path.toArray( array );
            
        }
        
        /**
         * Same as {@link #getPath()}.
         * 
         * @return The path (key) to this mapping.
         */
        @Override
        default List<K> getKey() {
        	
        	return getPath();
        	
        }
        
        /**
         * Retrieves the value corresponding to this entry.
         * <p>
         * If the mapping has been removed from the backing map (by the iterator's <tt>remove</tt>
         * operation), the results of this call are undefined.
         *
         * @return The value of this entry.
         */
        @Override
        V getValue();
        
        /**
         * Sets the value of this entry (reflects on the backing Graph).
         * <p>
         * If the mapping has been removed from the backing map (by the iterator's <tt>remove</tt>
         * operation), the results of this call are undefined.
         *
         * @param value The value to set for this entry.
         * @return The previous value.
         * @throws NullPointerException if the value given is null.
         */
        @Override
        V setValue( V value ) throws NullPointerException;
        
        /**
         * Compares the specified object with this entry for equality. Returns <tt>true</tt>
         * if the given object is also an Entry and both entries represent the same mapping.
         * Two entries <tt>e1</tt> and <tt>e2</tt> represent the same mapping if they have both
         * the same path and the same value:
         * <p>
         * <code>
         * e1.getPath().equals(e2.getPath()) &amp;&amp; e1.getValue().equals(e2.getValue())
         * </code>
         *
         * @param obj The object to compare to.
         * @return <tt>true</tt> if this and the given object are entries that correspond to the
         *         same mapping. <tt>false</tt> otherwise.
         */
        @Override
        boolean equals( Object obj );
        
        /**
         * Generates the hash code of this entry.<br>
         * The hash code of a graph entry <tt>e</tt> is defined to be:
         * <p>
         * <code>
         * e.getPath().hashCode() ^ e.getValue().hashCode()
         * </code>
         * <p>
         * This ensures that <tt>e1.equals(e2)</tt> implies <tt>e1.hashCode()==e2.hashCode()</tt>
         * for any two Entries <tt>e1</tt> and <tt>e2</tt>.
         *
         * @return The hash code of this entry.
         */
        @Override
        int hashCode();
        
    }
    
}
