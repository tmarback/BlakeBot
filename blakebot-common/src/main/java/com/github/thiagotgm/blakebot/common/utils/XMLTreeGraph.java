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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Extension of the tree graph that is also capable of being stored in an XML format.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-23
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public class XMLTreeGraph<K extends XMLElement, V extends XMLElement> extends TreeGraph<K,V>
        implements XMLGraph<K,V> {
    
    /**
     * Factory that instantiates new keys.
     */
    protected XMLElement.Factory<? extends K> keyFactory;
    
    /**
     * Factory that instantiates new values.
     */
    protected XMLElement.Factory<? extends V> valueFactory;
    
    /**
     * Instantiates a 
     *
     * @param keyFactory
     * @param valueFactory
     * @throws NullPointerException
     */
    public XMLTreeGraph( XMLElement.Factory<? extends K> keyFactory,
            XMLElement.Factory<? extends V> valueFactory ) throws NullPointerException {
        
        this.keyFactory = keyFactory;
        this.valueFactory = valueFactory;
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        // TODO Auto-generated method stub
        
    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

        // TODO Auto-generated method stub
        
    }
    
    /**
     * Node in a TreeGraph that is capable of being written to and read from an
     * XML stream.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-24
     */
    protected class XMLNode extends Node<XMLNode> implements XMLElement {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = 9093174498184503252L;

        /**
         * Text used in the opening and closing tags for the node element.
         */
        protected static final String NODE_TAG = "node";
        
        /**
         * Text used in the opening and closing tags for the key subelement.
         */
        protected static final String KEY_TAG = "key";
        
        /**
         * Text used in the opening and closing tags for the value subelement.
         */
        protected static final String VALUE_TAG = "value";
        
        @Override
        public XMLNode newInstance() {
            
            return new XMLNode();
            
        }
        
        /**
         * Reads the key from the stream.
         * <p>
         * The position of the stream cursor when the method starts is right after the opening tag
         * of the key element, and after the method ends its position should be right before the
         * closing tag of the key element. Thus, this method only needs to read the body of
         * the key element.
         *
         * @param in The stream to read the instance data from.
         * @return The key element. Cannot be null.
         * @throws XMLStreamException if an error occurred while reading.
         */
        protected K readKey( XMLStreamReader in ) throws XMLStreamException {
            
            if ( keyFactory != null ) {
                K key = keyFactory.newInstance();
                key.read( in );
                return key;
            } else {
                return XMLElementIO.read( in );
            }
            
        }
        
        /**
         * Reads the value from the stream.
         * <p>
         * The position of the stream cursor when the method starts is right after the opening tag
         * of the value element, and after the method ends its position should be right before the
         * closing tag of the value element. Thus, this method only needs to read the body of
         * the value element.
         *
         * @param in The stream to read the instance data from.
         * @return The value element. Cannot be null.
         * @throws XMLStreamException if an error occurred while reading.
         */
        protected V readValue( XMLStreamReader in ) throws XMLStreamException {
            
            if ( valueFactory != null ) {
                V value = valueFactory.newInstance();
                value.read( in );
                return value;
            } else {
                return XMLElementIO.read( in );
            }
            
        }

        /**
         * Reads the key, value, and children of this node from an XML stream.<br>
         * Any previously stored graph data is wiped and replaced.
         * <p>
         * If a subclass needs to replace only the way that the key and/or value
         * are read, {@link #readKey(XMLStreamWriter) readKey} and/or
         * {@link #readValue(XMLStreamWriter) readValue} should be overridden instead.
         */
        @Override
        public void read( XMLStreamReader in ) throws XMLStreamException {

            if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
                   in.getLocalName().equals( NODE_TAG ) ) {
                throw new XMLStreamException( "Cannot find node start tag." );
            }
            
            children.clear();
            key = null;
            value = null;
            
            boolean reading = false;
            
            while ( in.hasNext() ) { // Read the body of the element.
                
                switch ( in.next() ) {
                    
                    case XMLStreamConstants.START_ELEMENT:
                        
                        if ( reading ) {
                            throw new XMLStreamException(
                                    "Encountered start of element while another lement was being read." );
                        }
                        switch ( in.getLocalName() ) {
                            
                            case NODE_TAG: // Child node.
                                XMLNode child = newInstance();
                                child.read( in ); // Recursively reads child.
                                if ( children.containsKey( child.getKey() ) ) {
                                    throw new XMLStreamException(
                                            "Found child node with duplicate key." );
                                }
                                children.put( child.getKey(), child );
                                break;
                                
                            case KEY_TAG: // Node's key.
                                if ( key != null ) {
                                    throw new XMLStreamException( "More than one key found." );
                                }
                                reading = true;
                                key = readKey( in );
                                break;
                                
                            case VALUE_TAG: // Node's value.
                                if ( value != null ) {
                                    throw new XMLStreamException( "More than one value found." );
                                }
                                reading = true;
                                value = readValue( in );
                                break;
                            
                        }
                        break;
                        
                    case XMLStreamConstants.END_ELEMENT:
                        switch ( in.getLocalName() ) {
                            
                            case NODE_TAG: // End of node element.
                                if ( reading ) { // Was reading a subelement.
                                    throw new XMLStreamException(
                                            "Reached end of node while an element was being read." );
                                } else { // Done reading.
                                    return;
                                }
                                
                            case KEY_TAG:
                                if ( key == null ) {
                                    throw new XMLStreamException(
                                            "Reached end of key element without a value." );
                                }
                                reading = false;
                                break;
                                
                            case VALUE_TAG:
                                if ( value == null ) {
                                    throw new XMLStreamException(
                                            "Reached end of value element without a value." );
                                }
                                reading = false;
                                break;
                            
                        }
                        break;
                    
                }
                
            }
            throw new XMLStreamException( "Reached end of document before end of node element." );
            
        }
        
        /**
         * Writes the key to the stream.
         * <p>
         * The opening tag of the key element is written right before this method is called,
         * and the closing tag right after it returns, so this method only needs to write the
         * body of the key element.
         *
         * @param out The stream to write instance data to.
         * @throws XMLStreamException if an error occurred while writing.
         */
        protected void writeKey( XMLStreamWriter out ) throws XMLStreamException {
            
            if ( keyFactory != null ) {
                getKey().write( out );
            } else {
                XMLElementIO.write( out, getKey() );
            }
            
        }
        
        /**
         * Writes the value to the stream.
         * <p>
         * The opening tag of the value element is written right before this method is called,
         * and the closing tag right after it returns, so this method only needs to write the
         * body of the value element.
         *
         * @param out The stream to write instance data to.
         * @throws XMLStreamException if an error occurred while writing.
         */
        protected void writeValue( XMLStreamWriter out ) throws XMLStreamException {
            
            if ( valueFactory != null ) {
                getValue().write( out );
            } else {
                XMLElementIO.write( out, getValue() );
            }
            
        }

        /**
         * Writes the key, value, and children of this node to an XML stream.
         * <p>
         * If a subclass needs to replace only the way that the key and/or value
         * are written, {@link #writeKey(XMLStreamWriter) writeKey} and/or
         * {@link #writeValue(XMLStreamWriter) writeValue} should be overridden instead.
         */
        @Override
        public void write( XMLStreamWriter out ) throws XMLStreamException {

            out.writeStartElement( NODE_TAG );
            
            if ( getKey() != null ) { // Write key if there is one.
                out.writeStartElement( KEY_TAG );
                writeKey( out );
                out.writeEndElement();
            }
            
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
