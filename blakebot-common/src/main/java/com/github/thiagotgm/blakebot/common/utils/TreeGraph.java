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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a directed tree graph, eg a graph where the same keys in different orders
 * map to different values, and the graph always starts at a immutable <i>root</i> element
 * (represented by an empty path). 
 * <p>
 * The default behavior of the graph is to directly map each key to the next node in the
 * graph. Subclasses can alter this by creating a subclass of {@link Node} that overrides
 * desired behavior then using an instance of that subclass as root.
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
     * How many mappings are stored in this graph.
     */
    protected int nMappings;
    
    /**
     * Constructs a TreeGraph with an empty root (the root node exists, but is empty).<br>
     * Same as calling {@link #TreeGraph(Object) TreeGraph(V)} with argument <b>null</b>.
     */
    public TreeGraph() {
        
        this( null );
        
    }
    
    /**
     * Constructs a TreeGraph where the root contains the given value.
     *
     * @param rootValue The value to be stored in the root of the tree.
     *                  If <tt>null</tt>, no value is stored in the root.
     */
    public TreeGraph( V rootValue ) {
        
        this.root = new Node( rootValue );
        this.nMappings = ( rootValue == null ) ? 0 : 1;
        
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
    public final V get( K... path ) {
        
        Node node = getDescendant( path );
        return ( node == null ) ? null : node.getValue();
        
    }
    
    @Override
    @SafeVarargs
    public final List<V> getAll( K...path ) {
        
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
    @SafeVarargs
    public final V set( V value, K... path ) throws NullPointerException {
        
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
    @SafeVarargs
    public final boolean add( V value, K... path ) throws NullPointerException {
        
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
    @SafeVarargs
    public final V remove( K... path ) {
        
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
        
        for ( int i = path.length - 1; i >= 0; i-- ) { // Cleans up any nodes that became irrelevant.
            
            if ( ( cur.getValue() == null ) && cur.getChildren().isEmpty() ) {
                cur = nodes.pop(); // Node has no value or children now, so delete it.
                cur.removeChild( path[i] );
            } else {
                break; // Found a node that can't be deleted.
            }
            
        }
        
        nMappings--; // A mapping was removed.
        return value; // Retrieve deleted value.
        
    }
    
    @Override
    public Set<Entry<K,V>> entrySet() {
        
        Collection<Entry<K,V>> entries = new ArrayList<>( size() );
        root.getEntries( entries, new Stack<>() ); // Get entries.
        return new HashSet<>( entries ); // Store in a set and return.
        
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
     * A node in the tree. The exact behavior of the graph can be changed by overriding
     * methods in a subclass then using the subclass as the root element.
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
        protected Map<K,Node> children;
        
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
         * Retrieves the path-value mapping entries for this node and its children,
         * placing them into the given entry collection.
         *
         * @param entries The collection to place the entries in.
         * @param path The path that maps to this node, where the bottom of the stack is
         *             the beginning of the path.
         */
        public void getEntries( Collection<Entry<K,V>> entries, Stack<K> path ) {
            
            if ( getValue() != null ) { // This node represents a mapping.
                entries.add( new TreeGraphEntry( path, this ) );
            }
            
            /* Recursively gets entries for each child */
            for ( Map.Entry<K,? extends Node> childEntry : getChildren() ) {
                
                path.push( childEntry.getKey() ); // Add child's key to path.
                childEntry.getValue().getEntries( entries, path );
                path.pop(); // Removes the child's key afterwards.
                
            }
            
        }
        
        /**
         * Writes the state of this instance to a stream.
         *
         * @param out The stream to write data to.
         * @throws IOException if there is an error while writing the state.
         */
        private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
            
            if ( this.value != null ) {
                out.writeBoolean( true ); // Mark that node has a value.
                out.writeObject( this.value ); // Write the value.
            } else {
                out.writeBoolean( false ); // Mark that node does not have a value.
            }
            
            /* Write children */
            Set<Map.Entry<K,? extends Node>> children = getChildren();
            out.writeInt( children.size() ); // Write amount of children.
            for ( Map.Entry<K,? extends Node> child : children ) {
                
                out.writeObject( child.getKey() ); // Write child's key.
                out.writeObject( child.getValue() ); // Write child.
                
            }
            
        }
        
        /**
         * Reads the state of this instance from a stream.
         *
         * @param in The stream to read data from.
         * @throws IOException if there is an error while reading the state.
         * @throws ClassNotFoundException if the class of a serialized value object cannot
         *                                be found.
         */
        private void readObject( java.io.ObjectInputStream in )
                throws IOException, ClassNotFoundException {
            
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
                
                K key;
                try { // Read key.
                    @SuppressWarnings( "unchecked" )
                    K tempKey = (K) in.readObject();
                    key = tempKey;
                } catch ( ClassCastException e ) {
                    throw new IOException( "Deserialized node key is not of the expected type.", e );
                }
                Node child;
                try { // Read child.
                    @SuppressWarnings( "unchecked" )
                    Node tempChild = (Node) in.readObject();
                    child = tempChild;
                } catch ( ClassCastException e ) {
                    throw new IOException( "Deserialized child node is not of the expected type.", e );
                }
                children.put( key, child ); // Store child.
                
            }
            
        }
        
        /**
         * Initializes instance data when this class is needed for deserialization
         * but there is no data available.
         *
         * @throws ObjectStreamException if an error occurred.
         */
        @SuppressWarnings( "unused" )
        private void readObjectNoData() throws ObjectStreamException {
            
            this.children = new HashMap<>();
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
    protected class TreeGraphEntry implements Entry<K,V> {
        
        private final List<K> path;
        private final Node node;
        
        /**
         * Constructs a new entry for the given path that is linked to the given node.
         *
         * @param path The path of this entry.
         * @param node The node that represents this entry.
         */
        public TreeGraphEntry( List<K> path, Node node ) {
            
            this.path = Collections.unmodifiableList( new ArrayList<>( path ) );
            this.node = node;
            
        }

        @Override
        public List<K> getPath() {

            return path;
            
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
