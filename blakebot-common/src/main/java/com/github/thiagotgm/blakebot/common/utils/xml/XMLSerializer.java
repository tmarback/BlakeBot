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

package com.github.thiagotgm.blakebot.common.utils.xml;

import java.io.NotSerializableException;
import java.io.Serializable;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.AbstractXMLWrapper;
import com.github.thiagotgm.blakebot.common.utils.Utils;
import com.github.thiagotgm.blakebot.common.utils.XMLElement;

/**
 * XML Wrapper for {@link Serializable} objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 */
public class XMLSerializer<T extends Serializable> extends AbstractXMLWrapper<T> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -3875253346127541487L;

    private static final String SERIALIZED_TAG = "serialized";
    
    /**
     * Initializes a Serializer with no wrapped object.
     */
    public XMLSerializer() {
        
        super();
        
    }
    
    /**
     * Initializes a Serializer that initially wraps the given object.
     *
     * @param obj The object to be wrapped.
     */
    public XMLSerializer( T obj ) {
        
        super( obj );
        
    }
    
    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {
        
        if ( ( in.next() != XMLStreamConstants.START_ELEMENT ) ||
                in.getLocalName().equals( SERIALIZED_TAG ) ) { // Check start tag.
            throw new XMLStreamException( "Did not find element start." );
        }
        
        if ( in.next() != XMLStreamConstants.CHARACTERS ) {
            throw new XMLStreamException( "Missing encoded text." );
        }
        String encoded = in.getText(); // Get encoded text.
        
        if ( ( in.next() != XMLStreamConstants.END_ELEMENT ) ||
                in.getLocalName().equals( SERIALIZED_TAG ) ) { // Check end tag.
            throw new XMLStreamException( "Did not find element end." );
        }
        
        try {
            @SuppressWarnings( "unchecked" ) // Decode from string and check if castable
            T obj = (T) Utils.stringToSerializable( encoded );      // to expected type.
            setObject( obj );
        } catch ( ClassCastException e ) {
            throw new XMLStreamException( "Encoded object does not correspond to expected type." );
        }
        
    }
    
    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {
        
        if ( getObject() == null ) {
            throw new IllegalStateException( "No object to write." );
        }
        
        out.writeStartElement( SERIALIZED_TAG );
        try { // Encode into a Serializable string.
            out.writeCharacters( Utils.encode( getObject() ) );
        } catch ( NotSerializableException e ) {
            throw new XMLStreamException( "Element could not be serialized for encoding." );
        }
        out.writeEndElement();
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @param <T> The type of object that the created wrappers wrap.
     * @return A new factory.
     */
    public static <T extends Serializable> XMLElement.Factory<XMLSerializer<T>> newFactory() {
        
        return new Factory<>();
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-21
     * @param <T> The type of object that the created wrapper instances wrap.
     */
    private static class Factory<T extends Serializable> implements XMLElement.Factory<XMLSerializer<T>> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -7998089274806104807L;

        @Override
        public XMLSerializer<T> newInstance() {

            return new XMLSerializer<>();
            
        }
        
    }

}
