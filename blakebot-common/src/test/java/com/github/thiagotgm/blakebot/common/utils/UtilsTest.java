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

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Tester class for {@link Utils}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-23
 */
public class UtilsTest {

    /**
     * Tests the methods for serializing and deserializing Serializables to strings.
     */
    @Test
    public void testSerialize() {

        testSerialize( new String( "hi" ) );
        testSerialize( new Integer( 20 ) );
        testSerialize( new Double( 23.55 ) );
        
        HashMap<String,Float> map = new HashMap<>();
        map.put( "one", 1f );
        map.put( "two", 2f );
        map.put( "three", 3f );
        testSerialize( map );
        
        TreeSet<String> set = new TreeSet<>();
        set.add( "afirst" );
        set.add( "bsecond" );
        set.add( "cthird" );
        testSerialize( set );
        
    }
    
    /**
     * Attempts to serialize then deserialize an object to a string, failing if the
     * deserialized object is not equal to the original.
     *
     * @param obj The object to (de)serialize.
     */
    private <T extends Serializable> void testSerialize( T obj ) {
        
        String encoded = Utils.serializableToString( obj );
        T decoded = Utils.stringToSerializable( encoded );
        assertEquals( "Deserialized object not equal to original.", obj, decoded );
        
    }
    
    /**
     * Tests the methods for serializing and deserializing objects to strings.
     * 
     * @throws NotSerializableException if an exception happens.
     */
    @Test
    public void testEncode() throws NotSerializableException {
        
        testEncode( new String( "hi" ) );
        testEncode( new Integer( 20 ) );
        testEncode( new Double( 23.55 ) );
        
        Map<String,Float> map = new HashMap<>();
        map.put( "one", 1f );
        map.put( "two", 2f );
        map.put( "three", 3f );
        testEncode( map );
        
        Set<String> set = new TreeSet<>();
        set.add( "afirst" );
        set.add( "bsecond" );
        set.add( "cthird" );
        testEncode( set );
        
    }
    
    /**
     * Tests encoding an object that is not serializable.
     * 
     * @throws NotSerializableException if an exception happens (expected).
     */
    @Test( expected = NotSerializableException.class )
    public void testEncodeException() throws NotSerializableException {
        
        testEncode( new Thread() );
        
    }
    
    /**
     * Attempts to encode then decode an object to a string, failing if the
     * decoded object is not equal to the original.
     *
     * @param obj The object to en/decode.
     * @throws NotSerializableException if the object is not serializable.
     */
    private <T> void testEncode( T obj ) throws NotSerializableException {
        
        String encoded = Utils.encode( obj );
        T decoded = Utils.decode( encoded );
        assertEquals( "Deserialized object not equal to original.", obj, decoded );
        
    }

}
