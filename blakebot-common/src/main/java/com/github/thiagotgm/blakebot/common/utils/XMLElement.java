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

import java.io.Serializable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Type of object that can be stored in an XML format.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 */
public interface XMLElement extends Serializable {
    
    /**
     * Reads instance data from an XML stream.
     *
     * @param in The stream to read the instance data from. The <b>current</b> position of
     *           the stream should be the opening tag of the element to read.
     * @throws XMLStreamException if an error occurred while parsing.
     */
    void read( XMLStreamReader in ) throws XMLStreamException;
    
    /**
     * Writes the instance to an XML stream.
     *
     * @param out The stream to write data to.
     * @throws XMLStreamException if an error occurred while writing.
     */
    void write( XMLStreamWriter out ) throws XMLStreamException;

}
