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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLElement;
import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;
import sx.blah.discord.handle.obj.IWebhook;

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
     * Character encoding used by default for reading and writing XML streams.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    /**
     * Writes an XML document to a stream, where the content of the document is the given object,
     * translated using the given XML translator.
     *
     * @param out The stream to write to.
     * @param content The content of the document to be written.
     * @param translator The translator to use to encode the content.
     * @param encoding The character encoding to use.
     * @param <T> The type of object to be encoded.
     * @throws XMLStreamException if an error is encountered while writing.
     */
    public static <T> void writeXMLDocument( OutputStream out, T content, XMLTranslator<T> translator,
    		String encoding ) throws XMLStreamException {
    	
    	XMLStreamWriter outStream = XMLOutputFactory.newFactory().createXMLStreamWriter( out, encoding );
        outStream.writeStartDocument();
        translator.write( outStream, content );
        outStream.writeEndDocument();
    	
    }
    
    /**
     * Writes an XML document to a stream, where the content of the document is the given object,
     * translated using the given XML translator. Uses the
     * {@link #DEFAULT_ENCODING default character encoding}.
     * <p>
     * Same as calling {@link #writeXMLDocument(OutputStream, T, XMLTranslator, String)} with
     * fourth parameter {@value #DEFAULT_ENCODING}.
     *
     * @param out The stream to write to.
     * @param content The content of the document to be written.
     * @param translator The translator to use to encode the content.
     * @param <T> The type of object to be encoded.
     * @throws XMLStreamException if an error is encountered while writing.
     */
    public static <T> void writeXMLDocument( OutputStream out, T content, XMLTranslator<T> translator )
    		throws XMLStreamException {
    	
    	writeXMLDocument( out, content, translator, DEFAULT_ENCODING );
    	
    }
    
    /**
     * Writes an XML document to a stream, where the content of the document is the XMLElement
     * given.
     *
     * @param out The stream to write to.
     * @param content The content of the document to be written.
     * @param encoding The character encoding to use.
     * @throws XMLStreamException if an error is encountered while writing.
     */
    public static void writeXMLDocument( OutputStream out, XMLElement content, String encoding )
            throws XMLStreamException {
        
        writeXMLDocument( out, content, (XMLElement.Translator<XMLElement>) () -> {
        	
        	return null;
        	
        }, encoding );
        
    }
    
    /**
     * Writes an XML document to a stream, where the content of the document is the XMLElement
     * given. Uses the {@link #DEFAULT_ENCODING default character encoding}.
     * <p>
     * Same as calling {@link #writeXMLDocument(OutputStream, XMLElement, String)} with third parameter
     * {@value #DEFAULT_ENCODING}.
     *
     * @param out The stream to write to.
     * @param content The content of the document to be written.
     * @throws XMLStreamException if an error is encountered while writing.
     */
    public static void writeXMLDocument( OutputStream out, XMLElement content ) throws XMLStreamException {
        
        writeXMLDocument( out, content, DEFAULT_ENCODING );
        
    }
    
    /**
     * Reads an XML document from a stream, using the given translator the decode the content.
     * <p>
     * If there is any content after what is read by the translator, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     *
     * @param in The stream to read to.
     * @param translator The translator that will decode the document's content.
     * @param encoding The character encoding of the stream.
     * @param <T> The type of element that will be read.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T> T readXMLDocument( InputStream in, XMLTranslator<T> translator, String encoding )
            throws XMLStreamException {
        
        XMLStreamReader inStream = XMLInputFactory.newFactory().createXMLStreamReader( in, encoding );
        while ( inStream.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip comments.
        T content = translator.read( inStream );
        while ( inStream.hasNext() ) { inStream.next(); } // Go to end of document.
        
        return content;
        
    }
    
    /**
     * Reads an XML document from a stream, using the given translator the decode the content. 
     * Uses the {@link #DEFAULT_ENCODING default character encoding}.
     * <p>
     * Same as calling {@link #readXMLDocument(InputStream, XMLTranslator, String)} with third parameter
     * {@value #DEFAULT_ENCODING}.
     * <p>
     * If there is any content after what is read by the translator, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     *
     * @param in The stream to read to.
     * @param translator The translator that will decode the document's content.
     * @param encoding The character encoding of the stream.
     * @param <T> The type of element that will be read.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T> T readXMLDocument( InputStream in, XMLTranslator<T> translator )
            throws XMLStreamException {
    	
    	return readXMLDocument( in, translator, DEFAULT_ENCODING );
    	
    }
    
    /**
     * Reads an XML document from a stream, using the given translator the decode the content.
     * <p>
     * If there is any content after what is read by the translator, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     * <p>
     * Convenience method to allow the use of lambdas for the translator of an XML element.
     *
     * @param in The stream to read to.
     * @param translator The translator that will decode the document's content.
     * @param encoding The character encoding of the stream.
     * @param <T> The type of XML element that will be read.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T extends XMLElement> T readXMLDocument( InputStream in, XMLElement.Translator<T> translator,
    		String encoding ) throws XMLStreamException {
    	
    	return readXMLDocument( in, (XMLTranslator<T>) translator, encoding );
    	
    }
    
    /**
     * Reads an XML document from a stream, using the given translator the decode the content. 
     * Uses the {@link #DEFAULT_ENCODING default character encoding}.
     * <p>
     * Same as calling {@link #readXMLDocument(InputStream, XMLTranslator, String)} with third parameter
     * {@value #DEFAULT_ENCODING}.
     * <p>
     * If there is any content after what is read by the translator, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     * <p>
     * Convenience method to allow the use of lambdas for the translator of an XML element.
     *
     * @param in The stream to read to.
     * @param translator The translator that will decode the document's content.
     * @param encoding The character encoding of the stream.
     * @param <T> The type of XML element that will be read.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T extends XMLElement> T readXMLDocument( InputStream in, XMLElement.Translator<T> translator )
            throws XMLStreamException {
    	
    	return readXMLDocument( in, (XMLTranslator<T>) translator, DEFAULT_ENCODING );
    	
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
     * @param encoding The character encoding of the stream.
     * @param <T> The type of the XML element.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T extends XMLElement> T readXMLDocument( InputStream in, T content, String encoding )
            throws XMLStreamException {
        
        return readXMLDocument( in, (XMLElement.Translator<T>) () -> {
        	
        	return content;
        	
        }, encoding );
        
    }
    
    /**
     * Reads an XML document from a stream, where the content of the document is to be read
     * by the given XMLElement. Uses the {@link #DEFAULT_ENCODING default character encoding}.
     * <p>
     * Same as calling {@link #readXMLDocument(InputStream, T, String)} with third parameter
     * {@value #DEFAULT_ENCODING}.
     * <p>
     * If there is any content after what is read by the given XMLElement, that extra content
     * is ignored. This means that the stream will always be read until the end of the document
     * is found.
     *
     * @param in The stream to read to.
     * @param content The element that will read the document's content.
     * @param <T> The type of the XML element.
     * @throws XMLStreamException if an error is encountered while reading.
     */
    public static <T extends XMLElement> T readXMLDocument( InputStream in, T content )
    		throws XMLStreamException {
        
        return readXMLDocument( in, content, DEFAULT_ENCODING );
        
    }
    
    /* General purpose stuff */
    
    /**
     * Creates a string that represents the given object, using the type
     * of the object (user, guild, channel, etc) and its ID.
     * 
     * @param obj The object to get an ID string for.
     * @return The ID string.
     */
    public static String idString( IIDLinkedObject obj ) {
    	
    	StringBuilder builder = new StringBuilder();
    	
    	// Add identifier for object type.
    	if ( obj instanceof ICategory ) {
    		builder.append( "category" );
    	} else if ( obj instanceof IPrivateChannel ) {
    		builder.append( "privateChannel" );
    	} else if ( obj instanceof IVoiceChannel ) {
    		builder.append( "voiceChannel" );
    	} else if ( obj instanceof IChannel ) {
    		builder.append( "channel" );
    	} else if ( obj instanceof IEmoji ) {
    		builder.append( "emoji" );
    	} else if ( obj instanceof IGuild ) {
    		builder.append( "guild" );
    	} else if ( obj instanceof IMessage ) {
    		builder.append( "message" );
    	} else if ( obj instanceof IRole ) {
    		builder.append( "role" );
    	} else if ( obj instanceof IUser ) {
    		builder.append( "user" );
    	} else if ( obj instanceof IVoiceState ) {
    		builder.append( "voiceState" );
    	} else if ( obj instanceof IWebhook ) {
    		builder.append( "webhook" );
    	} else {
    		builder.append( '?' );
    	}
    	
    	builder.append( '#' ); // Add separator.
    	
    	builder.append( obj.getStringID() ); // Add ID.
    	
    	return builder.toString();
    	
    }
    
    /**
     * Creates ID strings for an array of objects.
     * 
     * @param arr The array of objects to get ID strings for.
     * @param <T> The type of the objects in the array.
     * @return The array with the ID strings for each object.
     * @see #idString(IIDLinkedObject)
     */
    public static <T extends IIDLinkedObject> String[] idString( T[] arr ) {
    	
    	String[] newArr = new String[arr.length];
    	
    	for ( int i = 0; i < arr.length; i++ ) {
    		
    		newArr[i] = idString( arr[i] ); // Get id string for each element.
    		
    	}
    	
    	return newArr;
    	
    }    
    
    private static final String SPECIAL_CHARACTER = "&";
    private static final String SPECIAL_CHARACTER_MARKER = "&amp";
    private static final String SEPARATOR = ";";
    private static final String SEPARATOR_MARKER = "&scln";
    private static final String EMPTY_MARKER = "&empty";
    private static final String NULL_MARKER = "&null";
    
    /**
     * Encodes a list of strings into a single string, that can later be decoded using
     * {@link #decode(String)}.
     * 
     * @param list The list to be encoded.
     * @return The encoded version of the list.
     */
    public static String encodeList( List<String> list ) {
    	
    	List<String> sanitized = new ArrayList<>( list.size() );
    	for ( String elem : list ) { // Sanitize each element of the list.
    		
    		if ( elem == null ) { // Null element.
    			sanitized.add( NULL_MARKER );
    		} else if ( elem.isEmpty() ) { // Empty string.
    			sanitized.add( EMPTY_MARKER );
    		} else { // Regular string.
	    		sanitized.add( elem
	    				.replace( SPECIAL_CHARACTER, SPECIAL_CHARACTER_MARKER ) // Replace special char.
	    				.replace( SEPARATOR, SEPARATOR_MARKER ) ); // Replace separator.
    		}
    		
    	}
    	return String.join( SEPARATOR, sanitized ); // Join sanitized strings.
    	
    }
    
    /**
     * Decodes a string that represents a list, as encoded by {@link #encodeList(List)}.
     * 
     * @param str The encoded version of the list.
     * @return The decoded list.
     */
    public static List<String> decodeList( String str ) {
    	
    	List<String> sanitized = Arrays.asList( str.split( SEPARATOR ) ); // Split sanitized strings.
    	List<String> list = new ArrayList<>( sanitized.size() );
    	for ( String elem : sanitized ) { // Un-sanitize each element.
    		
    		if ( elem.equals( NULL_MARKER ) ) { // Null element.
    			list.add( null );
    		} else if ( elem.equals( EMPTY_MARKER ) ) { // Empty string.
    			list.add( "" );
    		} else { // Regular string.
    			list.add( elem
    					.replace( SEPARATOR_MARKER, SEPARATOR ) // Restore separator.
    					.replace( SPECIAL_CHARACTER_MARKER, SPECIAL_CHARACTER ) ); // Restore special char.
    		}
    		
    	}
    	return list;
    	
    }

}
