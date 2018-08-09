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

package com.github.thiagotgm.blakebot.common.storage.translate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for elements that uses an XML format for encoding/decoding into a String.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-23
 * @param <T> The type that can be translated.
 */
public class XMLTranslator<T> implements Translator<T> {
	
	/**
     * Character encoding used by default for XML.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
	
	private final com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> translator;
	private final String encoding;

	/**
	 * Initializes a translator that uses the given charset encoding for String conversion.
	 * 
	 * @param translator The translator to convert from object to XML.
	 * @param encoding The charset to use.
	 */
	public XMLTranslator( com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> translator,
			String encoding ) {
		
		this.translator = translator;
		this.encoding = encoding;
		
	}
	
	/**
	 * Initializes a translator that uses the {@link #DEFAULT_ENCODING default encoding}
	 * for String conversion.
	 * 
	 * @param translator The translator to convert from object to XML.
	 */
	public XMLTranslator( com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> translator ) {
		
		this( translator, DEFAULT_ENCODING );
		
	}
	
	@Override
	public String encode( T obj ) throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			XMLStreamWriter outStream = XMLOutputFactory.newFactory().createXMLStreamWriter( out, encoding );
			translator.write( outStream, obj );
		} catch ( XMLStreamException e ) {
			throw new IOException( "Could not write XML object.", e );
		}
		return out.toString( encoding );
		
	}

	@Override
	public T decode( String str ) throws IOException {

		ByteArrayInputStream in = new ByteArrayInputStream( str.getBytes( encoding ) );
		try {
			XMLStreamReader inStream = XMLInputFactory.newFactory().createXMLStreamReader( in, encoding );
			return translator.read( inStream );
		} catch ( XMLStreamException e ) {
			throw new IOException( "Could not read XML object.", e );
		}
		
	}
	
	/**
	 * Retrieves the backing translator being used to convert objects to XML.
	 * 
	 * @return The backing translator.
	 */
	public com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> getXMLTranslator() {
		
		return translator;
		
	}

}
