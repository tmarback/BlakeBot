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

package com.github.thiagotgm.blakebot.common.storage.xml;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLElement;
import com.github.thiagotgm.blakebot.common.storage.xml.XMLTreeGraph;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLInteger;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLString;
import com.github.thiagotgm.blakebot.common.utils.Utils;

/**
 * Unit tests for {@link XMLTreeGraph}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class XMLTreeGraphTest {
    
    private static final XMLTreeGraph<String,Integer> EXPECTED;
    
    static {
        
        EXPECTED = new XMLTreeGraph<>( new XMLString(), new XMLInteger() );
        EXPECTED.add( 0 );
        EXPECTED.add( 34, "hi" );
        EXPECTED.add( 420, "hi", "I" );
        EXPECTED.add( 90, "hi", "I", "am" );
        EXPECTED.add( -29, "hi", "I", "am", "here" );
        
    }
    
    private static final XMLElement.Translator<XMLTreeGraph<String,Integer>> TRANSLATOR = () -> {
    	
    	return new XMLTreeGraph<>( new XMLString(), new XMLInteger() );
    	
    };

    @Test
    public void testRead() throws XMLStreamException, FactoryConfigurationError {

        InputStream in = this.getClass().getResourceAsStream( "/TreeGraph.xml" );
        XMLTreeGraph<String,Integer> actual = Utils.readXMLDocument( in, TRANSLATOR );
        
        assertEquals( "Read graph is not correct.", EXPECTED, actual );
        
    }
    
    @Test
    public void testWrite() throws XMLStreamException, IOException {
     
        XMLTestHelper.testReadWrite( EXPECTED, TRANSLATOR );
        
    }

}
