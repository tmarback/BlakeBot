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
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for elements that uses an XML format for encoding/decoding.
 * <p>
 * The encoding is done by writing the XML representation of the object into
 * a String, and returning the String as data. Decoding is the opposite process.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-23
 * @param <T> The type that can be translated.
 */
public class XMLTranslator<T> implements Translator<T> {
	
	private static final String ENCODING = "UTF-8";
	
	private final com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> translator;
	
	/**
	 * Initializes a translator that uses the given XML translator for conversion.
	 * 
	 * @param translator The translator to convert from object to XML.
	 */
	public XMLTranslator( com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator<T> translator ) {
		
		this.translator = translator;
		
	}
	
	@Override
	public Data toData( T obj ) throws TranslationException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			XMLStreamWriter outStream = XMLOutputFactory.newFactory().createXMLStreamWriter( out, ENCODING );
			translator.write( outStream, obj );
		} catch ( XMLStreamException e ) {
			throw new TranslationException( "Could not write XML object.", e );
		}
		try {
			return Data.stringData( out.toString( ENCODING ) );
		} catch ( UnsupportedEncodingException e ) {
			throw new TranslationException( "Failed to encode XML string.", e );
		}
		
	}

	@Override
	public T fromData( Data data ) throws TranslationException {
		
		if ( !data.isString() ) {
			throw new TranslationException( "Given data is not a String." );
		}

		ByteArrayInputStream in;
		try {
			in = new ByteArrayInputStream( data.getString().getBytes( ENCODING ) );
		} catch ( UnsupportedEncodingException e ) {
			throw new TranslationException( "Failed to decode XML string.", e );
		}
		try {
			XMLStreamReader inStream = XMLInputFactory.newFactory().createXMLStreamReader( in, ENCODING );
			return translator.read( inStream );
		} catch ( XMLStreamException e ) {
			throw new TranslationException( "Could not read XML object.", e );
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
