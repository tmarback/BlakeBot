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
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLCollection;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLString;

/**
 * Unit tests for {@link XMLCollection}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-11
 */
public class XMLCollectionTest {
    
    private static final Collection<String> EXPECTED = new ArrayList<>( 5 );
    
    @BeforeClass
	public static void setUpExpected() throws Exception {
    	
    	EXPECTED.add( "hi" );
        EXPECTED.add( "potato salad" );
        EXPECTED.add( "spaceship" );
        EXPECTED.add( "lul" );
        EXPECTED.add( "Best Girl (TM)" );
        
	}

    @Test
    public void testRead() throws XMLStreamException {

        InputStream in = this.getClass().getResourceAsStream( "/Collection.xml" );
        @SuppressWarnings("unchecked")
		Collection<String> collection = Utils.readXMLDocument( in,
        		new XMLCollection<String>( (Class<List<String>>) (Class<?>) ArrayList.class, new XMLString() ) );
        
        assertEquals( "Read collection is not correct.", EXPECTED, collection );
        
    }
    
	@Test
    @SuppressWarnings("unchecked")
    public void testWrite() throws XMLStreamException, IOException {
        
        XMLTestHelper.testReadWrite( EXPECTED, new XMLCollection<String>
        		( (Class<List<String>>) (Class<?>) ArrayList.class, new XMLString() ) );
        
    }

}
