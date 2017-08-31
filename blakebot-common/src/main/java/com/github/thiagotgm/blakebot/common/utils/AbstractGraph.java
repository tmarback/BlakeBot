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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of common methods in a graph.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-31
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public abstract class AbstractGraph<K,V> implements Graph<K,V> {

    @Override
    public boolean equals( Object obj ) {
        
        if ( !( obj instanceof Graph ) ) {
            return false; // Not a Graph instance.
        }
        
        Graph<?,?> graph = (Graph<?,?>) obj;
        return this.entrySet().equals( graph.entrySet() );
        
    }
    
    @Override
    public int hashCode() {
        
        int hash = 0;
        for ( Entry<K,V> entry : entrySet() ) {
            // Adds the hash of each entry.
            hash += entry.hashCode();
            
        }
        return hash;
        
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder( entrySet().toString() );
        builder.setCharAt( 0, '{' ); // Set ends of mapping list.
        builder.setCharAt( builder.length() - 1, '}' );
        return builder.toString();
        
    }
    
    /**
     * Shared implementation of an entry in the graph.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-20
     */
    protected abstract class AbstractEntry implements Entry<K,V> {
        
        private final List<K> path;
        
        /**
         * Constructs a new entry for the given path.
         *
         * @param path The path of this entry.
         */
        public AbstractEntry( List<K> path ) {
            
            this.path = Collections.unmodifiableList( new ArrayList<>( path ) );
            
        }

        @Override
        public List<K> getPath() {

            return path;
            
        }
        
        @Override
        public boolean equals( Object obj ) {
            
            if ( !( obj instanceof Entry ) ) {
                return false; // Not an Entry instance.
            }
            
            Entry<?,?> entry = (Entry<?,?>) obj;
            return this.getPath().equals( entry.getPath() ) &&
                   this.getValue().equals( entry.getValue() );
            
        }
        
        @Override
        public int hashCode() {
            
            return getPath().hashCode() ^ getValue().hashCode();
            
        }
        
        @Override
        public String toString() {
            
            return String.format( "%s=%s", getPath().toString(), getValue().toString() );
            
        }
        
    }

}
