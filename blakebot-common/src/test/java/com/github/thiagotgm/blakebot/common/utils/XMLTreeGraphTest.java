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

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;

import com.github.thiagotgm.blakebot.common.utils.xml.XMLInteger;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLString;

/**
 * Unit tests for {@link XMLTreeGraph}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
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
        InputStream in = this.getClass().getResourceAsStream( "/TreeGraph.xml" );
        Utils.readXMLDocument( in, graph );
        
        assertEquals( "Read graph is not correct.", EXPECTED, graph );
        
    }
    
    @Test
    public void testWrite() throws XMLStreamException, IOException {
     
        XMLTreeGraph<XMLString,XMLInteger> graph = new XMLTreeGraph<XMLString,XMLInteger>();
        XMLTestHelper.testReadWrite( EXPECTED, graph );
        
    }

}
