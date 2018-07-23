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
 * Provides a way to write an object to an XML stream, or reconstruct an
 * object from an XML stream.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 *
 * @param <T> The type of object that can be translated.
 */
public interface XMLTranslator<T> extends Serializable {
	
	/**
	 * Reads (decodes) an instance from an XML stream.
	 * 
	 * @param in The stream to read from.
	 * @return The read instance.
	 * @throws XMLStreamException if an error was encountered while decoding.
	 */
	T read( XMLStreamReader in ) throws XMLStreamException;
	
	/**
	 * Writes (encodes) an instance to an XML stream.
	 * 
	 * @param out The stream to write to.
	 * @param instance The instance to encode.
	 * @throws XMLStreamException if an error was encountered while encoding.
	 */
	void write( XMLStreamWriter out, T instance ) throws XMLStreamException;

}
