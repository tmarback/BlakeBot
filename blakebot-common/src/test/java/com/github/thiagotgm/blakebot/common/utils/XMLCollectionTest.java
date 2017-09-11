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
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.github.thiagotgm.blakebot.common.utils.xml.XMLCollection;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLList;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLString;

/**
 * Unit tests for {@link XMLCollection}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class XMLCollectionTest {
    
    private static final XMLCollection<XMLString> EXPECTED;
    
    static {
        
        EXPECTED = new XMLList<>( new ArrayList<>( 5 ), XMLString.newFactory() );
        EXPECTED.add( new XMLString( "hi" ) );
        EXPECTED.add( new XMLString( "potato salad" ) );
        EXPECTED.add( new XMLString( "spaceship" ) );
        EXPECTED.add( new XMLString( "lul" ) );
        EXPECTED.add( new XMLString( "Best Girl (TM)" ) );
        
    }

    @Test
    public void testRead() throws XMLStreamException {

        XMLCollection<XMLString> collection = new XMLList<>( new ArrayList<>( 5 ),
                XMLString.newFactory() );
        InputStream in = this.getClass().getResourceAsStream( "/Collection.xml" );
        Utils.readXMLDocument( in, collection );
        
        assertEquals( "Read collection is not correct.", EXPECTED, EXPECTED );
        
    }
    
    @Test
    public void testWrite() throws XMLStreamException, IOException {
        
        XMLCollection<XMLString> collection = new XMLList<>( new ArrayList<>( 5 ),
                XMLString.newFactory() );
        XMLTestHelper.testReadWrite( EXPECTED, collection );
        
    }

}
