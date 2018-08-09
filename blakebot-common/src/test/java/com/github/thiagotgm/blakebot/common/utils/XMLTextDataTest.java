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

package com.github.thiagotgm.blakebot.common.utils;

import org.junit.Test;

import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLBoolean;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLByte;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLDouble;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLFloat;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLInteger;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLLong;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLShort;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLString;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLTextData;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Tests for XMLTextData elements.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class XMLTextDataTest {
    
    /**
     * Reads data from a stream, testing if the read data matches the expected value.
     *
     * @param in The stream to read from.
     * @param translator The translator to read data with.
     * @param expected The expected value.
     * @throws XMLStreamException if an error was encountered.
     */
    private <T> void readTest( XMLStreamReader in, XMLTextData<T> translator, T expected )
            throws XMLStreamException {
        
        while ( in.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip space.
        T actual = translator.read( in );
        assertEquals( "Read value does not match expected.", expected, actual );
        
    }

    @Test
    public void testRead() throws XMLStreamException {
        
        InputStream in = this.getClass().getResourceAsStream( "/TextData.xml" );
        XMLStreamReader inStream = XMLInputFactory.newFactory().createXMLStreamReader( in );
        while ( inStream.next() != XMLStreamConstants.START_ELEMENT ) {} // Skip comments.
        
        /* Test booleans */
        readTest( inStream, new XMLBoolean(), true );
        readTest( inStream, new XMLBoolean(), false );
        
        /* Test byte */
        readTest( inStream, new XMLByte(), (byte) 5 );
        readTest( inStream, new XMLByte(), (byte) -34 );
        
        /* Test short */
        readTest( inStream, new XMLShort(), (short) 646 );
        readTest( inStream, new XMLShort(), (short) -344 );
        
        /* Test int */
        readTest( inStream, new XMLInteger(), 2523 );
        readTest( inStream, new XMLInteger(), -2424 );
        readTest( inStream, new XMLInteger(), 0 );
        
        /* Test long */
        readTest( inStream, new XMLLong(), 51515L );
        readTest( inStream, new XMLLong(), -24444L );
        
        /* Test float */
        readTest( inStream, new XMLFloat(), 734.27f );
        readTest( inStream, new XMLFloat(), -344.44f );
        
        /* Test double */
        readTest( inStream, new XMLDouble(), 959457.254526 );
        readTest( inStream, new XMLDouble(), -5453.35555 );
        
        /* Test String */
        readTest( inStream, new XMLString(), "Hi I am here" );
        
    }
    
    @Test
    public void testBooleanWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( true, new XMLBoolean() );
        XMLTestHelper.testReadWrite( false, new XMLBoolean() );
        
    }
    
    @Test
    public void testByteWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( (byte) 4, new XMLByte() );
        XMLTestHelper.testReadWrite( (byte) -7, new XMLByte() );
        
    }
    
    @Test
    public void testShortWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( (short) 44, new XMLShort() );
        XMLTestHelper.testReadWrite( (short) -98, new XMLShort() );
        
    }
    
    @Test
    public void testIntWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( 444, new XMLInteger() );
        XMLTestHelper.testReadWrite( -989, new XMLInteger() );
        XMLTestHelper.testReadWrite( 0, new XMLInteger() );
        
    }
    
    @Test
    public void testLongWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( 4444L, new XMLLong() );
        XMLTestHelper.testReadWrite( -9891L, new XMLLong() );
        
    }
    
    @Test
    public void testFloatWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( 4444.4f, new XMLFloat() );
        XMLTestHelper.testReadWrite( -9891.87f, new XMLFloat() );
        
    }
    
    @Test
    public void testDoubleWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( 4444.44, new XMLDouble() );
        XMLTestHelper.testReadWrite( -234656.8723, new XMLDouble() );
        
    }
    
    @Test
    public void testStringWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( "Potato is here", new XMLString() );
        
    }

}
