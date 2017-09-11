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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.AbstractXMLWrapper;

/**
 * Common implementation for wrappers that store wrapped objects in XML format by converting
 * them to or from a string. The XML format of the wrapped element does not have any opening or closing
 * tags, it is only a single string.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 * @param <T> The type of object being wrapped.
 */
public abstract class XMLTextData<T> extends AbstractXMLWrapper<T> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -987321998278933537L;
    
    /**
     * Initializes a wrapper with no wrapped object.
     */
    public XMLTextData() {
        
        super();
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given object.
     *
     * @param obj The object to be wrapped.
     */
    public XMLTextData( T obj ) {
        
        super( obj );
        
    }
    
    /**
     * Retrieves the tag that identifies the object.
     *
     * @return The object tag.
     */
    public abstract String getTag();
    
    /**
     * Converts the given string to an object that will be wrapped.
     * 
     * @param str The string representation of an object to wrap.
     * @return The object represented by the string. If the given string could not be
     *         parsed to an object of the expected type, returns <tt>null</tt>.
     */
    protected abstract T fromString( String str );

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        if ( in.getEventType() != XMLStreamConstants.CHARACTERS ) {
            throw new XMLStreamException( "Could not find data." );
        }

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Did not find element start." );
        }

        T obj = null;
        while ( in.hasNext() ) { // Read each element.

            switch ( in.next() ) {

                case XMLStreamConstants.CHARACTERS:
                    obj = fromString( in.getText() );
                    if ( obj == null ) {
                        throw new XMLStreamException( "Could not read data." );
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if ( in.getLocalName().equals( getTag() ) ) {
                        if ( obj == null ) {
                            throw new XMLStreamException( "No data to be read." );
                        }
                        setObject( obj );
                        return; // Done reading.
                    } else {
                        throw new XMLStreamException( "Unexpected end element." );
                    }

            }

        }
        throw new XMLStreamException( "Unexpected end of document." );
        
    }
    
    /**
     * Converts the given wrapped object to a string format.
     * 
     * @param obj The object to convert.
     * @return The string representation of the given object.
     */
    protected abstract String toString( T obj );

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {

        if ( getObject() == null ) {
            throw new IllegalStateException( "No object currently wrapped." );
        }
        
        out.writeStartElement( getTag() );
        out.writeCharacters( toString( getObject() ) );
        out.writeEndElement();
        
    }

}
