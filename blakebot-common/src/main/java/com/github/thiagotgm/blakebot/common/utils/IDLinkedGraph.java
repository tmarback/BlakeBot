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
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.xml.XMLIDLinkedObject;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLPassthrough;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * XMLGraph that uses {@link IIDLinkedObject}s as keys.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-03
 * @param <V> The type of values being stored.
 */
public class IDLinkedGraph<V extends XMLElement> extends AbstractGraph<IIDLinkedObject,V>
        implements XMLGraph<IIDLinkedObject,V> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 345917961460461125L;

    /**
     * Local name of the XML element that represents this graph type.
     */
    public static final String TAG = "idLinkedGraph";
    
    private final IDLinkedTreeGraph graph;
    private final XMLWrappedGraph<IIDLinkedObject,V,XMLIDLinkedObject,XMLPassthrough<V>> wrappedGraph;

    /**
     * Instantiates a graph that uses the given client to obtain IDLinkedObject keys, and the given
     * factory to instantiate values.
     *
     * @param client The client to use to obtain keys.
     * @param valueFactory The factory to create value instances with. If null, the values will be
     *                     written and read using {@link XMLElementIO}.
     * @throws NullPointerException if the <tt>client</tt> is null.
     */
    public IDLinkedGraph( IDiscordClient client, XMLElement.Factory<? extends V> valueFactory )
            throws NullPointerException {
        
        if ( client == null ) {
            throw new NullPointerException( "Client cannot be null." );
        }
        
        XMLElement.Factory<XMLIDLinkedObject> keyWrapperFactory =
                XMLIDLinkedObject.newFactory( client );
        XMLElement.Factory<XMLPassthrough<V>> valueWrapperFactory =
                XMLPassthrough.newFactory( valueFactory );
        graph = new IDLinkedTreeGraph( keyWrapperFactory, valueWrapperFactory );
        wrappedGraph = new XMLWrappedGraph<>( graph, keyWrapperFactory, valueWrapperFactory );
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        graph.read( in );
        
    }
    
    /**
     * Writes the state of the graph to an XML stream. The value factory is not
     * included in the stream.
     */
    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

        graph.write( out, false );
        
    }

    @Override
    public V get( IIDLinkedObject... path ) {

        return wrappedGraph.get( path );
        
    }

    @Override
    public List<V> getAll( IIDLinkedObject... path ) {

        return wrappedGraph.getAll( path );
        
    }

    @Override
    public V set( V value, IIDLinkedObject... path )
            throws UnsupportedOperationException, NullPointerException {

        return wrappedGraph.set( value, path );
        
    }

    @Override
    public boolean add( V value, IIDLinkedObject... path )
            throws UnsupportedOperationException, NullPointerException {

        return wrappedGraph.add( value, path );
        
    }

    @Override
    public V remove( IIDLinkedObject... path ) throws UnsupportedOperationException {

        return wrappedGraph.remove( path );
        
    }

    @Override
    public Set<Graph.Entry<IIDLinkedObject,V>> entrySet() {

        return wrappedGraph.entrySet();
        
    }

    @Override
    public int size() {

        return wrappedGraph.size();
        
    }

    @Override
    public void clear() {

        wrappedGraph.clear();
        
    }
    
    /**
     * Specialized TreeGraph that uses IDLinkedObjects as keys. The nodes of the graph in XML format are
     * represented by the keys themselves.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-03
     */
    protected class IDLinkedTreeGraph extends
            XMLTreeGraph<XMLIDLinkedObject,XMLPassthrough<V>> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -4912643719874701832L;

        /**
         * Constructs a graph that uses the given factory to create new key and value instances.
         *
         * @param keyFactory The factory to use to create new key instances.
         * @param valueFactory The factory to use to create new value instances.
         */
        public IDLinkedTreeGraph( XMLElement.Factory<XMLIDLinkedObject> keyFactory, 
                XMLElement.Factory<XMLPassthrough<V>> valueFactory ) {

            super( keyFactory, valueFactory );
            
        }
        
        @Override
        public String getTag() {
            
            return TAG;
            
        }
        
        @Override
        public void write( XMLStreamWriter out, boolean includeFactory )
                throws XMLStreamException {
            
            out.writeStartElement( getTag() );
            if ( includeFactory ) {
                writeFactory( out, valueFactory, VALUE_FACTORY_TAG );
            }
            getRoot().write( out );
            out.writeEndElement();
            
        }
        
        /**
         * Node that uses its key (an XMLIDLinkedObject) to represent itself.
         *
         * @version 1.0
         * @author ThiagoTGM
         * @since 2017-09-03
         */
        protected class IDLinkedNode extends XMLNode {
            
            /**
             * UID that represents this class.
             */
            private static final long serialVersionUID = 7894249041169571477L;

            @Override
            public XMLNode newInstance() {
                
                return new IDLinkedNode();
                
            }
            
            @Override
            public void read( XMLStreamReader in ) throws XMLStreamException {
                
                if ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) {
                    throw new XMLStreamException( "Not in start element." );
                }
                
                /* Read key */
                XMLIDLinkedObject key = keyFactory.newInstance();
                key.readStart( in );
                setKey( key );

                children.clear();
                value = null;

                boolean reading = false;
                while ( in.hasNext() ) { // Read the body of the element.

                    switch ( in.next() ) {

                        case XMLStreamConstants.START_ELEMENT:

                            if ( reading ) {
                                throw new XMLStreamException(
                                        "Encountered start of element while another element was being read." );
                            }
                            if ( in.getLocalName().equals( VALUE_TAG ) ) {

                                if ( value != null ) {
                                    throw new XMLStreamException( "More than one value found." );
                                }
                                reading = true;
                                in.next(); // Move to start of value element.
                                value = readValue( in );
                                break;

                            } else { // Possibly a child.
                                XMLNode child = newInstance();
                                child.read( in ); // Recursively reads child.
                                if ( children.containsKey( child.getKey() ) ) {
                                    throw new XMLStreamException( "Found child node with duplicate key." );
                                }
                                children.put( child.getKey(), child );

                            }
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            if ( in.getLocalName().equals( getKey().getTag() ) ) {
                                return; // Done reading.
                            } else if ( in.getLocalName().equals( VALUE_TAG ) ) {
                                if ( value == null ) {
                                    throw new XMLStreamException( "Reached end of value element without a value." );
                                }
                                reading = false;
                            } else {
                                throw new XMLStreamException( "Unexpected end element." );
                            }
                            break;

                    }

                }
                throw new XMLStreamException( "Reached end of document before end of node element." );
                
            }
            
            @Override
            public void write( XMLStreamWriter out ) throws XMLStreamException {
                
                getKey().writeStart( out );
                
                if ( getValue() != null ) { // Write value if there is one.
                    out.writeStartElement( VALUE_TAG );
                    writeValue( out );
                    out.writeEndElement();
                }
                
                /* Recursively write each child */
                for ( XMLNode child : getChildren() ) {
                    
                    child.write( out );
                    
                }

                out.writeEndElement();
                
            }
            
        }

    }

}
