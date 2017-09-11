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

package com.github.thiagotgm.blakebot.common.utils.xml;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.XMLElement;

/**
 * Adapter for a Collection of objects that allows writing the collection's contents
 * to an XML stream, or read them from one.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-10
 * @param <E> The type of elements being stored in the collection.
 */
public abstract class XMLCollection<E extends XMLElement> implements XMLElement, Collection<E> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -5857045540745890723L;

    /**
     * Local name of the XML element.
     */
    public static final String TAG = "collection";
    
    private final Collection<E> collection;
    private final XMLElement.Factory<? extends E> factory;

    /**
     * Instantiates an XMLColletion backed by the given collection that uses the given factory
     * to create new element instances.
     *
     * @param collection The backing collection.
     * @param factory The factory to create element instances with.
     */
    public XMLCollection( Collection<E> collection, XMLElement.Factory<? extends E> factory ) {
        
        this.collection = collection;
        this.factory = factory;
        
    }
    
    /**
     * Retrieves the tag that identifies the object.
     *
     * @return The object tag.
     */
    public String getTag() {
        
        return TAG;
        
    }

    /**
     * Reads the elements of the collection from an XML stream. The collection is cleared before
     * reading, so any pre-existing elements are removed.
     */
    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Did not find element start." );
        }
        
        collection.clear();
        while ( in.hasNext() ) { // Read each element.
            
            switch ( in.next() ) {
                
                case XMLStreamConstants.START_ELEMENT:
                    E elem = factory.newInstance();
                    elem.read( in );
                    collection.add( elem );
                    break;
                    
                case XMLStreamConstants.END_ELEMENT:
                    if ( in.getLocalName().equals( getTag() ) ) {
                        return; // Done reading.
                    } else {
                        throw new XMLStreamException( "Unexpected end element." );
                    }
                
            }
            
        }
        throw new XMLStreamException( "Unexpected end of document." );

    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException {

        out.writeStartElement( getTag() );
        for ( E elem : collection ) { // Write each element.
            
            elem.write( out );
            
        }
        out.writeEndElement();

    }

    /* Delegates to wrapped collection */

    @Override
    public int size() {

        return collection.size();
        
    }

    @Override
    public boolean isEmpty() {

        return collection.isEmpty();
        
    }

    @Override
    public boolean contains( Object o ) {

        return collection.contains( o );
        
    }

    @Override
    public Iterator<E> iterator() {

        return collection.iterator();
        
    }

    @Override
    public Object[] toArray() {

        return collection.toArray();
        
    }

    @Override
    public <T> T[] toArray( T[] a ) {

        return collection.toArray( a );
        
    }

    @Override
    public boolean add( E e ) {

        return collection.add( e );
        
    }

    @Override
    public boolean remove( Object o ) {

        return collection.remove( o );
        
    }

    @Override
    public boolean containsAll( Collection<?> c ) {

        return collection.containsAll( c );
        
    }

    @Override
    public boolean addAll( Collection<? extends E> c ) {

        return collection.addAll( c );
        
    }

    @Override
    public boolean removeAll( Collection<?> c ) {

        return collection.removeAll( c );
        
    }

    @Override
    public boolean retainAll( Collection<?> c ) {

        return collection.retainAll( c );
        
    }

    @Override
    public void clear() {

        collection.clear();
        
    }

    @Override
    public boolean equals( Object o ) {

        return collection.equals( o );
        
    }

    @Override
    public int hashCode() {

        return collection.hashCode();
        
    }
    
    @Override
    public String toString() {
        
        return collection.toString();
        
    }

}
