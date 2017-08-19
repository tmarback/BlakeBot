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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a directed tree graph, eg a graph where the same keys in different orders
 * map to different values, and the graph always starts at a immutable <i>root</i> element
 * (represented by an empty path). 
 * <p>
 * The default behavior of the graph is to directly map each key to the next node in the
 * graph. Subclasses can alter this by creating a subclass of {@link Node} that overrides
 * {@link Node#getChild(K)} and {@link Node#getOrCreateChild(K)} and using an instance of
 * that subclass as root of the tree.
 * <p>
 * Can only be serialized properly if all the values stored are also Serializable.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-16
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public class TreeGraph<K,V> implements Graph<K,V>, Serializable {
    
    /**
     * UID that represents the class.
     */
    private static final long serialVersionUID = -8738939021703848608L;
    
    /**
     * Node that is the root of the tree.
     */
    protected Node root;
    
    /**
     * Constructs a TreeGraph with an empty root (the root node exists, but is empty).<br>
     * Same as calling {@link #TreeGraph(V)} with argument <b>null</b>.
     */
    public TreeGraph() {
        
        this( null );
        
    }
    
    /**
     * Constructs a TreeGraph where the root contains the given value.
     *
     * @param rootValue The value to be stored in the root of the tree.
     */
    public TreeGraph( V rootValue ) {
        
        this.root = new Node( rootValue );
        
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
    @SafeVarargs
    protected final Node getDescendant( Node parent, K... path ) {
        
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
    @SafeVarargs
    protected final Node getDescendant( K... path ) {
        
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
    @SafeVarargs
    protected final Node getMaxDescendant( Node parent, K... path ) {
        
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
    @SafeVarargs
    protected final Node getMaxDescendant( K... path ) {
        
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
    @SafeVarargs
    protected final Node getOrCreateDescendant( Node parent, K... path ) {
        
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
    @SafeVarargs
    protected final Node getOrCreateDescendant( K... path ) {
        
        return getOrCreateDescendant( root, path );
        
    }
    
    @Override
    @SafeVarargs
    final public V get( K... path ) {
        
        Node node = getDescendant( path );
        return ( node == null ) ? null : node.getValue();
        
    }
    
    @Override
    @SafeVarargs
    public final void set( V value, K... path ) throws NullPointerException {
        
        if ( value == null ) {
            throw new NullPointerException( "Value cannot be null." );
        }
        
        getOrCreateDescendant( path ).setValue( value );
        
    }
    
    @Override
    @SafeVarargs
    public final boolean add( V value, K... path ) throws NullPointerException {
        
        if ( value == null ) {
            throw new NullPointerException( "Value cannot be null." );
        }
        
        Node node = getDescendant( path );
        if ( node != null ) { // Node already exists.
            if ( node.getValue() != null ) {
                return false; // Already has an element.
            }
        } else { // Node does not exist.
            node = getOrCreateDescendant( path );
        }
        node.setValue( value );
        return true;
        
    }
    
    /**
     * A node in the tree. The exact behaviour of the graph can be changed by overriding
     * {@link #getChild(K)} and {@link #getOrCreateChild(K)}.
     * <p>
     * Can only be serialized properly if the value stored is also Serializable.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-17
     */
    protected class Node implements Serializable {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -2696663106727592184L;
        
        /**
         * Value stored inside this Node.
         */
        protected V value;
        /**
         * Children nodes of this Node.
         */
        protected final Map<K,Node> children;
        
        {
            
            children = new HashMap<>();
            
        }
        
        /**
         * Constructs a Node with no value.
         */
        public Node() {
            
            this.value = null;
            
        }
        
        /**
         * Constructs a Node with the given value.
         *
         * @param value The initial value of the node.
         */
        public Node( V value ) {
            
            this.value = value;
            
        }
        
        /**
         * Retrieves the value of the node.
         *
         * @return The value of the node, or <b>null</b> if none.
         */
        public V getValue() {
            
            return value;
            
        }
        
        /**
         * Sets the value of the node.
         *
         * @param value The new value of the node.
         */
        public void setValue( V value ) {
            
            this.value = value;
            
        }
        
        /**
         * Gets the child of this node that corresponds to the given key.
         *
         * @param key The key to get the child for.
         * @return The child that corresponds to the given key, or null if there is
         *         no such child.
         */
        public Node getChild( K key ) {
            
            return children.get( key );
            
        }
        
        /**
         * Retrieves all the children of this node and the keys that are mapped to them.
         *
         * @return The children of this node and their keys.
         */
        public Set<Map.Entry<K,? extends Node>> getChildren() {
            
            return new HashSet<>( children.entrySet() );
            
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
                child = new Node();
                children.put( key, child );
            }
            return child;
            
        }
        
        /**
         * Sets the value of child note that corresponds to the given key to
         * the given value. If there is no node that corresponds to the given key,
         * creates one.
         *
         * @param key The key to get the child for.
         * @param value The value to set for that child.
         */
        public final void setChild( K key, V value ) {
            
            getOrCreateChild( key ).setValue( value );
            
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
         * Compares this node with the specified object for equality. Returns <tt>true</tt>
         * if the specified object is also a node, that contains the same value as this node,
         * and whose children are equal to the children of this node.
         *
         * @param obj The object to compare to.
         * @return <tt>true</tt> if the specified object is equal to this node, <tt>false</tt>
         *         otherwise.
         */
        @Override
        public boolean equals( Object obj ) {
            
            if ( !( obj instanceof TreeGraph.Node ) ) {
                return false; // Not a Node instance.
            }
            
            @SuppressWarnings( "unchecked" )
            Node node = (Node) obj;
            if ( !( ( this.value == null ) ? node.value == null :
                                             this.value.equals( node.value ) ) ) {
                return false; // Different values.
            }
            
            return this.getChildren().equals( node.getChildren() );
            
        }
        
        /**
         * Calculates the hash code of the node. The code is calculated by calling
         * {@link Objects#hash(Object...)} on the Node's {@link #getValue() value} and
         * {@link #getChildren() children}.
         * <p>
         * Thus, if <tt>a</tt> and <tt>b</tt> are two instances of Node, <tt>a.equals(b)</tt>
         * implies that <tt>a.hashCode()==b.hashCode()</tt>.
         *
         * @return The hash code for this TreeMap.
         */
        @Override
        public int hashCode() {
            
            return Objects.hash( value, getChildren() );
            
        }
        
    }
    
    /**
     * Compares this graph with the specified object for equality. Returns <tt>true</tt>
     * if the specified object is also a TreeGraph, that contains the same values mapped
     * to the same paths.
     *
     * @param obj The object to compare to.
     * @return <tt>true</tt> if the specified object is equal to this graph, <tt>false</tt>
     *         otherwise.
     */
    @Override
    public boolean equals( Object obj ) {
        
        if ( !( obj instanceof TreeGraph ) ) {
            return false; // Not a TreeGraph instance.
        }
        
        @SuppressWarnings( "rawtypes" )
        TreeGraph graph = (TreeGraph) obj;
        return this.root.equals( graph.root );
        
    }
    
    /**
     * Calculates the hash code of the graph. The hash code is the same as the hash code
     * of the root node, that is, a composed value of all keys and values stored in
     * this graph.
     *
     * @return The hash code of this TreeMap.
     */
    @Override
    public int hashCode() {
        
        return root.hashCode();
        
    }

}
