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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import com.github.thiagotgm.blakebot.common.utils.xml.XMLInteger;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLString;


public class XMLTreeGraphTest {
    
    private static final XMLTreeGraph<XMLString,XMLInteger> EXPECTED;
    
    static {
        
        EXPECTED = new XMLTreeGraph<XMLString,XMLInteger>(
                XMLString.newFactory(), XMLInteger.newFactory() );
        EXPECTED.add( new XMLInteger( 0 ) );
        EXPECTED.add( new XMLInteger( 34 ), new XMLString( "hi" ) );
        EXPECTED.add( new XMLInteger( 420 ), new XMLString( "hi" ), new XMLString( "I" ) );
        EXPECTED.add( new XMLInteger( 90 ), new XMLString( "hi" ), new XMLString( "I" ),
                new XMLString( "am" ) );
        EXPECTED.add( new XMLInteger( -29 ), new XMLString( "hi" ), new XMLString( "I" ),
                new XMLString( "am" ), new XMLString( "here" ) );
        
    }

    @Test
    public void testRead() throws XMLStreamException, FactoryConfigurationError {

        XMLTreeGraph<XMLString,XMLInteger> graph = new XMLTreeGraph<XMLString,XMLInteger>(
                XMLString.newFactory(), XMLInteger.newFactory() );
        InputStream stream = this.getClass().getResourceAsStream( "/TreeGraph.xml" );
        XMLStreamReader in = XMLInputFactory.newFactory().createXMLStreamReader( stream );
        while ( in.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip comments.
        graph.read( in );
        
        assertEquals( "Read graph is not correct.", EXPECTED, graph );
        
    }
    
    @Test
    public void testWrite() throws XMLStreamException, UnsupportedEncodingException {
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter out = XMLOutputFactory.newFactory().createXMLStreamWriter( outStream );
        out.writeStartDocument();
        EXPECTED.write( out );
        out.writeEndDocument();
        out.close();
        
        XMLTreeGraph<XMLString,XMLInteger> graph = new XMLTreeGraph<XMLString,XMLInteger>();
        InputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
        XMLStreamReader in = XMLInputFactory.newFactory().createXMLStreamReader( inStream );
        while ( in.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip comments.
        graph.read( in );
        
        assertEquals( "Read graph is not correct.", EXPECTED, graph );
        
    }

}
