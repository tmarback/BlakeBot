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
     * Reads an object from the given XML stream, wrapping it.
     */
    @Override
    public abstract void read( XMLStreamReader in ) throws XMLStreamException;

    /**
     * Writes the wrapped object to a stream.
     * 
     * @throws IllegalStateException if there is no object currently wrapped.
     */
    @Override
    public abstract void write( XMLStreamWriter out )
            throws XMLStreamException, IllegalStateException;

}
