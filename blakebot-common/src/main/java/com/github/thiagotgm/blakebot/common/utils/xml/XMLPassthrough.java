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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.AbstractXMLWrapper;
import com.github.thiagotgm.blakebot.common.utils.XMLElement;
import com.github.thiagotgm.blakebot.common.utils.XMLElementIO;

/**
 * Pass-through wrapper for XML elements, that simply delegate to the wrapped object.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 * @param <T> Type being wrapped.
 */
public class XMLPassthrough<T extends XMLElement> extends AbstractXMLWrapper<T> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 5683205361380469002L;
    
    private final XMLElement.Factory<? extends T> factory;

    /**
     * Initializes a wrapper with no wrapped object.
     * 
     * @param factory The factory to use to create instances of the wrapped object.
     */
    public XMLPassthrough( XMLElement.Factory<? extends T> factory ) {
        
        this( null, factory );
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given object.
     *
     * @param obj The object to be wrapped.
     * @param factory The factory to use to create instances of the wrapped object.
     */
    public XMLPassthrough( T obj, XMLElement.Factory<? extends T> factory ) {
        
        super( obj );
        this.factory = factory;
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        T obj = factory.newInstance();
        obj.read( in );
        setObject( obj );
        
    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {

        if ( getObject() == null ) {
            throw new IllegalStateException( "Cannot write empty wrapper." );
        }
        
        if ( factory == null ) {
            XMLElementIO.write( out, getObject() );
        } else {
            getObject().write( out );
        }        
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @param factory The factory to use to create instances of the wrapped object.
     * @param <T> The type of object being wrapped by the created instance.
     * @return A new factory.
     */
    public static <T extends XMLElement> XMLElement.Factory<XMLPassthrough<T>>
            newFactory( XMLElement.Factory<? extends T> factory ) {
        
        return new Factory<>( factory );
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory<T extends XMLElement>
            implements XMLElement.Factory<XMLPassthrough<T>> {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -7250492754673819969L;
        
        private final XMLElement.Factory<? extends T> factory;
        
        /**
         * Instantiates a factory that creates wrappers that use the given factory.
         *
         * @param factory The factory to use to create instances of the wrapped object.
         */
        public Factory( XMLElement.Factory<? extends T> factory ) {
            
            this.factory = factory;
            
        }

        @Override
        public XMLPassthrough<T> newInstance() {

            return new XMLPassthrough<>( factory );
            
        }
        
    }

}
