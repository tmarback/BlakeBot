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
 * Provides convenience methods for handling XML-ready objects.
 * <p>
 * Can only be used with XMLElement subclasses that declare a no-arg constructor,
 * as {@link #read(XMLStreamReader, Class) reading} works by instantiating the class
 * using the no-arg constructor then calling {@link XMLElement#read(XMLStreamReader)}
 * on the created instance.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 */
public abstract class XMLElementIO {

    private static final String TAG = "element";
    private static final String CLASS_ATTRIBUTE = "class";
    
    /**
     * Reads an object of the given class from an XML stream.
     *
     * @param in The input stream to read from.
     * @param objClass The class of the object being read.
     * @return The object that was read from the stream.
     * @throws IllegalArgumentException if the given class could not be instantiated through
     *                                  a no-arg constructor.
     * @throws XMLStreamException if an error happened while parsing.
     */
    static <T extends XMLElement> T read( XMLStreamReader in, Class<T> objClass )
            throws IllegalArgumentException, XMLStreamException {
        
        try {
            T obj = objClass.newInstance();
            obj.read( in );
            return obj;
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new IllegalArgumentException( "Given class could not be instantiated.", e );
        }
        
    }
    
    /**
     * Writes the given object to the given XML stream, leaving also metadata so that
     * {@link #read(XMLStreamReader)} can identify the class to load from the written data.
     *
     * @param out The stream to output to.
     * @param element The element to write.
     * @throws XMLStreamException if there was an error while writing.
     */
    static void write( XMLStreamWriter out, XMLElement element ) throws XMLStreamException {
        
        out.writeStartElement( TAG );
        out.writeAttribute( CLASS_ATTRIBUTE, element.getClass().getName() );
        out.writeEndElement();
        
    }
    
    /**
     * Reads from the given XML stream an object written using 
     * {@link #write(XMLStreamWriter, XMLElement)}. The class of the object is inferred from
     * the metadata left by the write method.
     *
     * @param in The input stream to read from.
     * @return The object that was read from the stream.
     * @throws XMLStreamException if an error happened while parsing.
     */
    static XMLElement read( XMLStreamReader in ) throws XMLStreamException {
        
        if ( ( in.next() != XMLStreamConstants.START_ELEMENT ) ||
                in.getLocalName().equals( TAG ) ) { // Check start tag.
            throw new XMLStreamException( "Did not find element start." );
        }
        
        String className = in.getAttributeValue( null, CLASS_ATTRIBUTE );
        Class<?> attributeClass;
        try { // Read class from metadata.
            attributeClass = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            throw new XMLStreamException( "Element specifies a class that cannot be found." );
        }
        if ( !XMLElement.class.isAssignableFrom( attributeClass ) ) { // Ensure XMLElement.
            throw new XMLStreamException( "Element specifies a class that is not an XML element." );
        }
        
        @SuppressWarnings( "unchecked" )
        Class<? extends XMLElement> objClass = (Class<? extends XMLElement>) attributeClass;
        XMLElement element;
        try {
            element = read( in, objClass ); // Read object.
        } catch ( IllegalArgumentException e ) {
            throw new XMLStreamException( "Element class could not be instantiated.", e );
        }
        
        if ( ( in.next() != XMLStreamConstants.END_ELEMENT ) ||
                in.getLocalName().equals( TAG ) ) { // Check end tag.
            throw new XMLStreamException( "Did not find element end." );
        }
        
        return element;
        
    }

}
