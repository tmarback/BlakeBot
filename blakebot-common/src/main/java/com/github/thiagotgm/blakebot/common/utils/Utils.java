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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General purpose utilities.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-18
 */
public abstract class Utils {
    
    private static final Logger LOG = LoggerFactory.getLogger( Utils.class );
    
    /* Serializable-String methods */
    
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    /**
     * Encodes a Serializable into a String.
     *
     * @param obj The object to be encoded.
     * @return The String with the encoded object, or null if the encoding failed.
     * @see #stringToSerializable(String)
     */
    public static String serializableToString( Serializable obj ) {
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream( bytes ).writeObject( obj );
        } catch ( IOException e ) {
            LOG.error( "Error on encoding Serializable to String.", e );
            return null;
        }
        return encoder.encodeToString( bytes.toByteArray() );
        
    }
    
    /**
     * Decodes a Serializable from a String.
     *
     * @param str The String to decode.
     * @param <T> Type of the object being deserialized.
     * @return The decoded object, or null if decoding failed.
     */
    public static <T extends Serializable> T stringToSerializable( String str ) {
        
        ByteArrayInputStream bytes;
        bytes = new ByteArrayInputStream( decoder.decode( str ) );
        try {
            @SuppressWarnings( "unchecked" )
            T decoded = (T) new ObjectInputStream( bytes ).readObject();
            return decoded;
        } catch ( ClassNotFoundException e ) {
            LOG.error( "Class of encoded Serializable not found.", e );
        } catch ( IOException e ) {
            LOG.error( "Error on decoding Serializable from String.", e );
            e.printStackTrace();
        } catch ( ClassCastException e ) {
            LOG.error( "Deserialized object is not of the expected type.", e );
        }
        return null; // Error encountered.
        
    }
    
    /**
     * Encodes an object into a String. The object <i>must</i> be {@link Serializable}.
     *
     * @param obj The object to be encoded.
     * @return A String with the encoded object.
     * @throws NotSerializableException if the object does not implement the
     *                                  java.io.Serializable interface.
     */
    public static String encode( Object obj ) throws NotSerializableException {
        
        if ( obj instanceof Serializable ) {
            return serializableToString( (Serializable) obj );
        } else {
            throw new NotSerializableException( obj.getClass().getName() );
        }
        
    }
    
    /**
     * Decodes an object that was encoded with {@link #encode(Object)}.
     *
     * @param str The String with the encoded object.
     * @param <T> Type of the object being decoded.
     * @return The decoded object, or null if decoding failed.
     */
    public static <T> T decode( String str ) {
        
        try {
            @SuppressWarnings( "unchecked" )
            T obj = (T) stringToSerializable( str );
            return obj;
        } catch ( ClassCastException e ) {
            LOG.error( "Decoded object is not of the expected type.", e );
        }
        return null;
        
    }
    
    /* XML writing/reading methods */
    
    /**
     * Writes an XML document to a stream, where the content of the document is the XMLElement
     * given.
     *
     * @param out The stream to write to.
     * @param content The content of the document to be written.
     * @throws XMLStreamException if an error is encountered while writing.
     */
    public static void writeXMLDocument( OutputStream out, XMLElement content ) throws XMLStreamException {
        
        XMLStreamWriter outStream = XMLOutputFactory.newFactory().createXMLStreamWriter( out );
        outStream.writeStartDocument();
        content.write( outStream );
        outStream.writeEndDocument();
        
    }
    
    /**
     * Reads an XML document from a stream, where the content of the document is to be read
     * by the given XMLElement.
     * <p>
     * If there is any content after what is read by the given XMLElement, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     *
     * @param in The stream to read to.
     * @param content The element that will read the document's content.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static void readXMLDocument( InputStream in, XMLElement content ) throws XMLStreamException {
        
        XMLStreamReader inStream = XMLInputFactory.newFactory().createXMLStreamReader( in );
        while ( inStream.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip comments.
        content.read( inStream );
        while ( inStream.hasNext() ) { inStream.next(); } // Go to end of document.
        
    }

}
