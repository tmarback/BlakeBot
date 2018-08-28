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

package com.github.thiagotgm.blakebot.common.storage.xml;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.TreeGraph;

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
     * Text used in the opening and closing tags for a node element.
     */
    public static final String NODE_TAG = "node";
    
    /**
     * Text used in the opening and closing tags for a key subelement of a node.
     */
    protected static final String KEY_TAG = "key";
    
    /**
     * Text used in the opening and closing tags for a value subelement of a node.
     */
    protected static final String VALUE_TAG = "value";
    
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
    
    /**
     * Retrieves the local name of the graph element.
     *
     * @return The tag (local name).
     */
    public String getTag() {
        
        return GRAPH_TAG;
        
    }

    /**
     * Reads the key, value, and children of a node from an XML stream.
     * 
     * @param in The stream to read from.
     * @return The read node.
     * @throws XMLStreamException if an error occurred while reading.
     */
    public Node readNode( XMLStreamReader in ) throws XMLStreamException {

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( NODE_TAG ) ) {
            throw new XMLStreamException( "Cannot find node start tag." );
        }
        
        K key = null;
        V value = null;
        Collection<Node> children = new LinkedList<>();
        
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
                            Node child = readNode( in ); // Recursively reads child.
                            children.add( child );
                            break;
                            
                        case KEY_TAG: // Node's key.
                            if ( key != null ) {
                                throw new XMLStreamException( "More than one key found." );
                            }
                            reading = true; // Move to start of key element.
                            while ( in.next() != XMLStreamConstants.START_ELEMENT );
                            key = keyTranslator.read( in );
                            break;
                            
                        case VALUE_TAG: // Node's value.
                            if ( value != null ) {
                                throw new XMLStreamException( "More than one value found." );
                            }
                            reading = true; // Move to start of value element.
                            while ( in.next() != XMLStreamConstants.START_ELEMENT );
                            value = valueTranslator.read( in );
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
                            	try {
                            		return new Node( key, value, children );
                            	} catch ( IllegalArgumentException e ) {
                            		throw new XMLStreamException(
                            				"Read multiple children nodes with the same key" );
                            	}
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
                            
                        case NODE_TAG: // Root node.
                            if ( hasRoot ) {
                                throw new XMLStreamException( "Duplicate tree root found." );
                            }
                            root = readNode( in );
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
     * Writes the key, value, and children of a node to an XML stream.
     * 
     * @param out The stream to write to.
     * @param node The node to write.
     * @throws XMLStreamException if an error occurred while writing.
     */
    public void writeNode( XMLStreamWriter out, Node node ) throws XMLStreamException {

        out.writeStartElement( NODE_TAG );
        
        if ( node.getKey() != null ) { // Write key if there is one.
            out.writeStartElement( KEY_TAG );
            keyTranslator.write( out, node.getKey() );
            out.writeEndElement();
        }
        
        if ( node.getValue() != null ) { // Write value if there is one.
            out.writeStartElement( VALUE_TAG );
            valueTranslator.write( out, node.getValue() );
            out.writeEndElement();
        }
        
        /* Recursively write each child */
        for ( Node child : node.getChildren() ) {
            
            writeNode( out, child );
            
        }
        
        out.writeEndElement();
        
    }

    /**
     * Writes the state of the graph to an XML stream.
     */
    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

    	out.writeStartElement( getTag() );
        writeNode( out, root );
        out.writeEndElement();
        
    }

}
