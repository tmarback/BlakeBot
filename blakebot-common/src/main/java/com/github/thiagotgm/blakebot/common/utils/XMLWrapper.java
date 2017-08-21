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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Wrapper for objects that is allows writing/reading them to/from XML streams.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 */
public interface XMLWrapper<T> extends XMLElement {
    
    /**
     * Retrieves the wrapped object.
     *
     * @return The wrapped object, or null if there is none.
     */
    T getObject();
    
    /**
     * Sets the wrapped object.
     *
     * @param obj The object to be wrapped.
     */
    void setObject( T obj );

    /**
     * Reads an object from the given XML stream, wrapping it.
     */
    @Override
    void read( XMLStreamReader in ) throws XMLStreamException;

    /**
     * Writes the wrapped object to a stream.
     * 
     * @throws IllegalStateException if there is no object currently wrapped.
     */
    @Override
    void write( XMLStreamWriter out )
            throws XMLStreamException, IllegalStateException;
    
    /**
     * Determines if the given object is equal to this. Returns <tt>true</tt> if the given
     * object is also an XMLWrapper, and it wraps the same object. That is, two XML wrappers
     * <tt>w1</tt> and <tt>w2</tt> are equal if:
     * <p>
     * <code>
     * w1.getObject()==null ? w2.getObject()==null : w1.getObject().equals(w2.getObject())
     * </code>
     *
     * @param obj The object to check for equality.
     * @return <tt>true</tt> if the given object is an XMLWrapper that wraps the same object
     *         as this wrapper. <tt>false</tt> otherwise.
     */
    @Override
    boolean equals( Object obj );
    
    /**
     * Calculates the hash code of this wrapper. The hash code of a wrapper is defined as the
     * same hash code of the object it wraps, or 0 if it does not wrap any object. That is:
     * <p>
     * <code>
     * getObject()==null ? 0 : getObject().hashCode()
     * </code>
     * <p>
     * This ensures that <tt>w1.equals(w2)</tt> implies <tt>w1.hashCode()==w2.hashCode()</tt>
     * for any two XMLWrappers <tt>w1</tt> and <tt>w2</tt>.
     *
     * @return The hash code of the wrapper.
     */
    @Override
    int hashCode();
    
    /**
     * Factory that creates instances of a wrapper for a certain type
     * of object.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-21
     * @param <T> The type of object that the created wrapper instances wrap.
     */
    @FunctionalInterface
    static interface Factory<T> extends XMLElement.Factory<XMLWrapper<T>> {}

}
