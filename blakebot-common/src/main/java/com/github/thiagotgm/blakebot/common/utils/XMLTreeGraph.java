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
public class XMLTreeGraph<K,V> extends TreeGraph<K,V> implements XMLGraph<K,V> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -4068743687358222428L;

    /**
     * Text used in the opening and closing tags for the overall graph element.
     */
    public static final String GRAPH_TAG = "treeGraph";
    
    /**
     * Factory that instantiates new keys.
     */
    protected XMLTranslator<K> keyTranslator;
    
    /**
     * Factory that instantiates new values.
     */
    protected XMLTranslator<V> valueTranslator;
    
    /**
     * Instantiates an XMLTreeGraph that uses the given translators to encode and decode elements
     * to/from XML.
     *
     * @param keyTranslator The translator for element keys.
     * @param valueTranslator The translator for element values.
     * @throws NullPointerException if either translator is <tt>null</tt>.
     */
    public XMLTreeGraph( XMLTranslator<K> keyTranslator, XMLTranslator<V> valueTranslator )
    		throws NullPointerException {
        
    	// Check translators.
    	if ( keyTranslator == null ) {
    		throw new NullPointerException( "Key translator cannot be null." );
    	}
    	if ( valueTranslator == null ) {
    		throw new NullPointerException( "Value translator cannot be null." );
    	}
    	
        setRoot( new XMLNode() );
        this.keyTranslator = keyTranslator;
        this.valueTranslator = valueTranslator;
        
    }
    
    /**
     * Convenience method for creating a new tree that uses XMLELements as keys.
     * <p>
     * Same effect as using the constructor, but lambdas can be used.
     * 
     * @param keyTranslator The translator for element keys.
     * @param valueTranslator The translator for element values.
     * @return The constructed tree.
     * @throws NullPointerException if either translator is <tt>null</tt>.
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     */
    public static <K extends XMLElement,V> XMLTreeGraph<K,V>
    		newTree( XMLElement.Translator<K> keyTranslator, XMLTranslator<V> valueTranslator )
    		throws NullPointerException {
    	
    	return new XMLTreeGraph<K,V>( keyTranslator, valueTranslator );
    	
    }
    
    /**
     * Convenience method for creating a new tree that uses XMLELements as values.
     * <p>
     * Same effect as using the constructor, but lambdas can be used.
     * 
     * @param keyTranslator The translator for element keys.
     * @param valueTranslator The translator for element values.
     * @return The constructed tree.
     * @throws NullPointerException if either translator is <tt>null</tt>.
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     */
    public static <K,V extends XMLElement> XMLTreeGraph<K,V>
			newTree( XMLTranslator<K> keyTranslator, XMLElement.Translator<V> valueTranslator )
			throws NullPointerException {

    	return new XMLTreeGraph<K,V>( keyTranslator, valueTranslator );
	
	}
    
    /**
     * Convenience method for creating a new tree that uses XMLELements as keys and values.
     * <p>
     * Same effect as using the constructor, but lambdas can be used.
     * 
     * @param keyTranslator The translator for element keys.
     * @param valueTranslator The translator for element values.
     * @return The constructed tree.
     * @throws NullPointerException if either translator is <tt>null</tt>.
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     */
    public static <K extends XMLElement,V extends XMLElement> XMLTreeGraph<K,V>
			newTree( XMLElement.Translator<K> keyTranslator, XMLElement.Translator<V> valueTranslator )
			throws NullPointerException {
		
		return new XMLTreeGraph<K,V>( keyTranslator, valueTranslator );
	
	}

    @Override
    @SuppressWarnings( "unchecked" )
    protected XMLNode getRoot() {
        
        return (XMLNode) super.getRoot();
        
    }
    
    /**
     * Retrieves the local name of the graph element.
     *
     * @return The tag (local name).
     */
    public String getTag() {
        
        return GRAPH_TAG;
        
    }

    /**
     * Reads a treeGraph from the given stream. If a factory for either key or value instances is
     * specified on the stream, and one wasn't specified on construction, the factory read from the
     * stream is used.
     */
    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Stream not in opening tag of expected graph." );
        }
        
        boolean hasRoot = false;
        nMappings = 0; // All previous mappings are deleted.
        while ( in.hasNext() ) { // Read each part of the graph.
            
            switch ( in.next() ) {
                
                case XMLStreamConstants.START_ELEMENT:
                    switch ( in.getLocalName() ) {
                            
                        case XMLNode.NODE_TAG: // Root node.
                            if ( hasRoot ) {
                                throw new XMLStreamException( "Duplicate tree root found." );
                            }
                            getRoot().read( in );
                            hasRoot = true; // Found tree root.
                            break;
                            
                        default:
                            throw new XMLStreamException( "Unexpected subelement found." );
                        
                    }
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    if ( in.getLocalName().equals( getTag() ) ) {
                        return; // Finished reading.
                    } else {
                        throw new XMLStreamException( "Unexpected closing tag found." );
                    }
                
            }
            
        }
        
        // Reached end of document was reached before closing tag of graph element.
        throw new XMLStreamException( "Unexpected end of document." );
        
    }
    
    /**
     * Writes the state of the graph to an XML stream.
     * <p>
     * In situations where the graph will always have key and value factories specified on construction,
     * calling this method with second argument <tt>false</tt> avoids writing unnecessary data, as the
     * written factories would be discarded when reading.
     *
     * @param out The stream to write data to.
     * @param includeFactories Whether the key and value factories, if any, should be included
     *                         in the saved data.
     * @throws XMLStreamException if an error happened while writing.
     */
    public void write( XMLStreamWriter out, boolean includeFactories ) throws XMLStreamException {
        
        out.writeStartElement( getTag() );
        getRoot().write( out );
        out.writeEndElement();
        
    }

    /**
     * Writes the state of the graph to an XML stream. The key and value factories are
     * included in the stream, if they exist.
     * <p>
     * Same as calling {@link #write(XMLStreamWriter, boolean)} with second argument <tt>true</tt>.
     */
    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

        write( out, true );
        
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
        public static final String NODE_TAG = "node";
        
        /**
         * Text used in the opening and closing tags for the key subelement.
         */
        protected static final String KEY_TAG = "key";
        
        /**
         * Text used in the opening and closing tags for the value subelement.
         */
        protected static final String VALUE_TAG = "value";
        
        /**
         * Constructs a XMLNode with no value or key.
         */
        public XMLNode() {
            
            super();
            
        }
        
        /**
         * Constructs a XMLNode with the given key.
         *
         * @param key The key of the node.
         * @throws NullPointerException if the given key is null.
         */
        public XMLNode( K key ) {
            
            super( key );
            
        }
        
        /**
         * Constructs a XMLNode with the given value and key.
         *
         * @param key The key of the node.
         * @param value The initial value of the node.
         * @throws NullPointerException if the given key is null.
         */
        public XMLNode( K key, V value ) {
            
            super( key, value );
            
        }
        
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
            
            return keyTranslator.read( in );
            
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
            
            return valueTranslator.read( in );
            
        }

        /**
         * Reads the key, value, and children of this node from an XML stream.<br>
         * Any previously stored graph data is wiped and replaced.
         * <p>
         * If a subclass needs to replace only the way that the key and/or value
         * are read, {@link #readKey(XMLStreamReader) readKey} and/or
         * {@link #readValue(XMLStreamReader) readValue} should be overridden instead.
         */
        @Override
        public void read( XMLStreamReader in ) throws XMLStreamException {

            if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
                  !in.getLocalName().equals( NODE_TAG ) ) {
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
                                    "Encountered start of element while another element was being read." );
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
                                reading = true; // Move to start of key element.
                                while ( in.next() != XMLStreamConstants.START_ELEMENT );
                                key = readKey( in );
                                break;
                                
                            case VALUE_TAG: // Node's value.
                                if ( value != null ) {
                                    throw new XMLStreamException( "More than one value found." );
                                }
                                reading = true; // Move to start of value element.
                                while ( in.next() != XMLStreamConstants.START_ELEMENT );
                                value = readValue( in );
                                break;
                                
                            default: // Unrecognized.
                                throw new XMLStreamException( "Unexpected subelement." );
                            
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
                                nMappings++; // Read a value, so one mapping found.
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
            
            keyTranslator.write( out, getKey() );
            
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
            
        	valueTranslator.write( out, getValue() );
            
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
