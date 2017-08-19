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

import java.util.List;

/**
 * Represents a graph that links sequences of keys to values.
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
     * Retrieves the value mapped to the given sequence of keys.
     *
     * @param path The sequence of keys that map to the value.
     * @return The value linked to the given path, or null if there is none.
     */
    V get( K... path );
    
    /**
     * Retrieves the values mapped to each step of the given sequence of keys.<br>
     * Steps that do not exist or have no mapping are ignored.
     *
     * @param path The sequence of keys that map to the values.
     * @return The values linked to each step of the given path, in the order that
     *         the path is traversed (same order that the keys are given).
     */
    List<V> getAll( K...path );
    
    /**
     * Maps a value to a sequence of keys, replacing the value currently mapped
     * to the path, if any.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @throws NullPointerException if the value given is null.
     */
    void set( V value, K... path ) throws NullPointerException;
    
    /**
     * Maps a value to a sequence of keys only if there is no current mapping
     * for that path.
     *
     * @param value The value to be stored on the path.
     * @param path The sequence of keys that map to the value.
     * @return true if the value was added to the graph.<br>
     *         false if there is already a value mapped to the given path.
     * @throws NullPointerException if the value given is null.
     */
    boolean add( V value, K... path ) throws NullPointerException;
    
    /**
     * Removes a mapping from this graph.
     *
     * @param path The sequence of keys that map to the value to be removed.
     * @return The removed value, or <tt>null</tt> if there is no mapping for the
     *         given path.
     */
    V remove( K... path );
    
}
