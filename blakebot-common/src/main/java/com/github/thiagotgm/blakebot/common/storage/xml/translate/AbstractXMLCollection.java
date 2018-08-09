/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * BlakeBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.common.storage.xml.translate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;

/**
 * Shared implementation for translators of Collection classes.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-10
 * @param <E> The type of elements being stored in the collection.
 * @param <T> The specific collection type.
 */
public abstract class AbstractXMLCollection<E,T extends Collection<E>> implements XMLTranslator<T> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -5857045540745890723L;
    
    private final Constructor<? extends T> collectionCtor;
    private final XMLTranslator<E> translator;

    /**
     * Instantiates an collection translator that uses instances of the given colletion class and
     * uses the given translator for the elements.
     *
     * @param collectionClass The class of collection to instantiate.
     * @param translator The translator to use for the collection elements.
     */
    public AbstractXMLCollection( Class<? extends T> collectionClass, XMLTranslator<E> translator )
    		throws IllegalArgumentException {
        
        try { // Get collection ctor.
			collectionCtor = collectionClass.getConstructor();
		} catch ( NoSuchMethodException | SecurityException e ) {
			throw new IllegalArgumentException( "Collection class does not have a public no-args constructor.", e );
		}
        
        try { // Check that ctor works.
			collectionCtor.newInstance();
		} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e ) {
			throw new IllegalArgumentException(
					"Collection class cannot be initialized using no-arg constructor.", e );
		}
        
        this.translator = translator;
        
    }
    
    /**
     * Retrieves the tag that identifies the collection object.
     *
     * @return The object tag.
     */
    public abstract String getTag();

    /**
     * Reads the elements of the collection from an XML stream. The collection is cleared before
     * reading, so any pre-existing elements are removed.
     */
    @Override
    public T read( XMLStreamReader in ) throws XMLStreamException {

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Did not find element start." );
        }
        
        T collection;
		try {
			collection = collectionCtor.newInstance();
		} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e ) {
			throw new XMLStreamException( "Could not create new element instance.", e );
		}
        while ( in.hasNext() ) { // Read each element.
            
            switch ( in.next() ) {
                
                case XMLStreamConstants.START_ELEMENT:
                    collection.add( translator.read( in ) );
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    if ( in.getLocalName().equals( getTag() ) ) {
                        return collection; // Done reading.
                    } else {
                        throw new XMLStreamException( "Unexpected end element." );
                    }
                
            }
            
        }
        throw new XMLStreamException( "Unexpected end of document." );

    }

    @Override
    public void write( XMLStreamWriter out, T instance ) throws XMLStreamException {

        out.writeStartElement( getTag() );
        for ( E elem : instance ) { // Write each element.
            
            translator.write( out, elem );
            
        }
        out.writeEndElement();

    }

}
