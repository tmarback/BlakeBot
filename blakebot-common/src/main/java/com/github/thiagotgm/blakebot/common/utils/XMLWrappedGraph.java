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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Convenience wrapper for XMLGraphs that store non-XML elements using wrappers, hiding the (un)wrapping
 * mechanics to provide access to the graph directly using the non-XML elements.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-31
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 * @param <KW> The type of wrapper used to wrap keys.
 * @param <VW> The type of wrapper used to wrap values.
 */
public class XMLWrappedGraph<K,V,KW extends XMLWrapper<K>,VW extends XMLWrapper<V>>
        extends AbstractGraph<K,V> implements XMLGraph<K,V> {
    
    /**
     * UID that represents the class.
     */
    private static final long serialVersionUID = 4246845092955858657L;
    
    private final XMLGraph<KW,VW> graph;
    private final XMLElement.Factory<KW> keyWrapperFactory;
    private final XMLElement.Factory<VW> valueWrapperFactory;
    
    /**
     * Instantiates a wrapped graph that is backed by the given graph.
     *
     * @param graph The graph that should back the wrapped graph.
     * @param keyWrapperFactory The factory to use to instantiate key wrappers.
     * @param valueWrapperFactory The factory to use to instantiate value wrappers.
     * @throws NullPointerException if any of the arguments is <tt>null</tt>.
     */
    public XMLWrappedGraph( XMLGraph<KW,VW> graph, XMLElement.Factory<KW> keyWrapperFactory,
            XMLElement.Factory<VW> valueWrapperFactory ) throws NullPointerException {
        
        if ( ( graph == null ) || 
             ( keyWrapperFactory == null ) || ( valueWrapperFactory == null ) ) {
            throw new NullPointerException( "Arguments cannot be null." );
        }
        
        this.keyWrapperFactory = keyWrapperFactory;
        this.valueWrapperFactory = valueWrapperFactory;
        this.graph = graph;
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        graph.read( in );
        
    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

        graph.write( out );
        
    }
    
    /**
     * Wraps the given object in a wrapper created by the given factory.
     *
     * @param obj The object to be wrapped.
     * @param wrapperFactory The factory that creates wrappers.
     * @param <T> The object type.
     * @param <TW> The type of wrapper that wraps the object.
     * @return The wrapper with the given wrapped object.
     */
    private <T,TW extends XMLWrapper<T>> TW wrap( T obj, XMLElement.Factory<TW> wrapperFactory ) {
        
        TW wrapper = wrapperFactory.newInstance();
        wrapper.setObject( obj );
        return wrapper;
        
    }
    
    /**
     * Wraps a path into key wrappers.
     *
     * @param path The path to be wrapped.
     * @return The wrapped path.
     */
    private KW[] wrap( K[] path ) {
        
        @SuppressWarnings( "unchecked" )
        KW[] wrappedPath = (KW[]) new XMLWrapper[ path.length ];
        for ( int i = 0; i < path.length; i++ ) {
            
            wrappedPath[i] = wrap( path[i], keyWrapperFactory );
            
        }
        return wrappedPath;
        
    }

	@SafeVarargs
    @Override
    public final V get( K... path ) {

    	VW wrap = graph.get( wrap( path ) );
        return wrap == null ? null : wrap.getObject();
        
    }

    @SafeVarargs
    @Override
    public final List<V> getAll( K... path ) {

        List<VW> wrapped = graph.getAll( wrap( path ) );
        List<V> unwrapped = new ArrayList<>( wrapped.size() );
        wrapped.stream().forEachOrdered( key -> unwrapped.add( key.getObject() ) );
        return unwrapped;
        
    }

    @SafeVarargs
    @Override
    public final V set( V value, K... path ) throws UnsupportedOperationException, NullPointerException {

        VW wrap = graph.set( wrap( value, valueWrapperFactory ), wrap( path ) );
        return wrap == null ? null : wrap.getObject();
        
    }

    @SafeVarargs
    @Override
    public final boolean add( V value, K... path ) throws NullPointerException {

        return graph.add( wrap( value, valueWrapperFactory ), wrap( path ) );
        
    }

    @SafeVarargs
    @Override
    public final V remove( K... path ) throws UnsupportedOperationException {

        VW wrap = graph.remove( wrap( path ) );
        return wrap == null ? null : wrap.getObject();
        
    }

    @Override
    public Set<Graph.Entry<K,V>> entrySet() {

        Set<Graph.Entry<K,V>> entries = new HashSet<>();
        graph.entrySet().stream().forEach( ( entry ) -> {
            
            List<K> path = new ArrayList<>( entry.getPath().size() );
            entry.getPath().stream().forEachOrdered( key -> path.add( key.getObject() ) );
            entries.add( new WrappedEntry( path, entry ) );
            
        });
        return entries;
        
    }
    
    @Override
    public Set<Graph.Entry<K,V>> entrySet( int level ) {

        Set<Graph.Entry<K,V>> entries = new HashSet<>();
        graph.entrySet().stream().forEach( ( entry ) -> {
            
        	if ( entry.getPath().size() == level ) {
	            List<K> path = new ArrayList<>( entry.getPath().size() );
	            entry.getPath().stream().forEachOrdered( key -> path.add( key.getObject() ) );
	            entries.add( new WrappedEntry( path, entry ) );
        	}
            
        });
        return entries;
        
    }

    @Override
    public int size() {

        return graph.size();
        
    }

    @Override
    public void clear() {

        graph.clear();
        
    }
    
    /**
     * Entry that is backed by an entry in the wrapper graph.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-31
     */
    protected class WrappedEntry extends AbstractEntry {
        
        private final Entry<KW,VW> entry;
        
        /**
         * Constructs a new entry for the given path that is linked to the given entry.
         *
         * @param path The path of this entry.
         * @param entry The entry in the wrapped graph that backs this entry.
         */
        public WrappedEntry( List<K> path, Entry<KW,VW> entry ) {
            
            super( path );
            this.entry = entry;
            
        }

        @Override
        public V getValue() {

            return entry.getValue().getObject();
            
        }

        @Override
        public V setValue( V value ) throws NullPointerException {

            if ( value == null ) {
                throw new NullPointerException( "Value cannot be null." );
            }
            
            return entry.setValue( wrap( value, valueWrapperFactory ) ).getObject();
                    
        }
        
    }

}
