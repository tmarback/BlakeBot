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

package com.github.thiagotgm.blakebot.common.storage;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link Data}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-30
 */
public class DataTest {
	
	@Test
	public void testEquals() {
		
		assertTrue( Data.nullData().equals( Data.nullData() ) );
		assertFalse( Data.nullData().equals( Data.booleanData( true ) ) );
		
		assertTrue( Data.stringData( "one" ).equals( Data.stringData( "one" ) ) );
		assertFalse( Data.stringData( "one" ).equals( Data.stringData( "two" ) ) );
		assertFalse( Data.nullData().equals( Data.booleanData( true ) ) );
		
		assertTrue( Data.numberData( "42" ).equals( Data.numberData( 42 ) ) );
		assertFalse( Data.numberData( "42" ).equals( Data.numberData( 42.0 ) ) );
		assertFalse( Data.numberData( "42.0" ).equals( Data.numberData( 42 ) ) );
		assertTrue( Data.numberData( "42.0" ).equals( Data.numberData( 42.0 ) ) );
		
		
		List<Data> list1 = new ArrayList<>();
		list1.add( Data.stringData( "Hi" ) );
		List<Data> list2 = new ArrayList<>();
		list2.add( Data.nullData() );
		list2.add( Data.booleanData( true ) );
		list2.add( Data.stringData( "Hi" ) );
		list2.add( Data.numberData( 11 ) );
		
		assertTrue( Data.listData( list1 ).equals( Data.listData( list1 ) ) );
		assertTrue( Data.listData( list2 ).equals( Data.listData( list2 ) ) );
		assertFalse( Data.listData( list1 ).equals( Data.listData( list2 ) ) );
		assertFalse( Data.listData( list2 ).equals( Data.listData( list1 ) ) );
		
		Map<String,Data> map1 = new HashMap<>();
		map1.put( "hello", Data.numberData( 94 ) );
		Map<String,Data> map2 = new HashMap<>();
		map2.put( "hello", Data.numberData( 93 ) );
		Map<String,Data> map3 = new HashMap<>();
		map3.put( "hello", Data.numberData( 94 ) );
		map3.put( "key", Data.listData( list1 ) );
		
		assertTrue( Data.mapData( map1 ).equals( Data.mapData( map1 ) ) );
		assertTrue( Data.mapData( map2 ).equals( Data.mapData( map2 ) ) );
		assertTrue( Data.mapData( map3 ).equals( Data.mapData( map3 ) ) );
		assertFalse( Data.mapData( map1 ).equals( Data.mapData( map2 ) ) );
		assertFalse( Data.mapData( map1 ).equals( Data.mapData( map3 ) ) );
		assertFalse( Data.mapData( map2 ).equals( Data.mapData( map3 ) ) );
		
	}
	
	@Test
	public void testHashCode() {
		
		assertEquals( "i am here".hashCode(), Data.stringData( "i am here" ).hashCode() );
		assertEquals( "420".hashCode(), Data.stringData( "420" ).hashCode() );
		assertEquals( 0, Data.booleanData( false ).hashCode() );
		assertEquals( 1, Data.booleanData( true ).hashCode() );
		assertEquals( 0, Data.nullData().hashCode() );
		
		List<Data> list = new ArrayList<>();
		list.add( Data.nullData() );
		list.add( Data.booleanData( true ) );
		list.add( Data.stringData( "Hi" ) );
		list.add( Data.numberData( 11 ) );
		assertEquals( list.hashCode(), Data.listData( list ).hashCode() );
		
		Map<String,Data> map = new HashMap<>();
		map.put( "hello", Data.numberData( 94 ) );
		map.put( "key", Data.listData( list ) );
		assertEquals( map.hashCode(), Data.mapData( map ).hashCode() );
		
	}
	
	@Test
	public void testString() {
		
		/* Test null string */
		
		Data data = Data.stringData( null );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NULL, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertTrue( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test empty string */
		
		String str = "";
		data = Data.stringData( new String( str ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.STRING, data.getType() );
		
		// Check test methods.
		assertTrue( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertEquals( str, data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test random string */
		
		str = "hello world!";
		data = Data.stringData( new String( str ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.STRING, data.getType() );
		
		// Check test methods.
		assertTrue( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertEquals( str, data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
	}
	
	@Test
	public void testNumber() {
		
		/* Test null string */
		
		Data data = Data.numberData( null );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NULL, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertTrue( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test integer number string. */
		
		long integer = 24;
		String str = String.valueOf( integer );
		data = Data.numberData( str );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NUMBER, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertTrue( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( str, data.getString() );
		assertEquals( str, data.getNumber() );
		assertEquals( integer, data.getNumberInteger() );
		assertEquals( integer, data.getNumberFloat(), 0.000001 );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test integer number. */
		
		integer = -345;
		str = String.valueOf( integer );
		data = Data.numberData( integer );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NUMBER, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertTrue( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( str, data.getString() );
		assertEquals( str, data.getNumber() );
		assertEquals( integer, data.getNumberInteger() );
		assertEquals( integer, data.getNumberFloat(), 0.000001 );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test floating-point number string. */
		
		double fp = -420.245;
		str = String.valueOf( fp );
		data = Data.numberData( str );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NUMBER, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertTrue( data.isNumber() );
		assertTrue( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( str, data.getString() );
		assertEquals( str, data.getNumber() );
		assertEquals( (long) fp, data.getNumberInteger() );
		assertEquals( fp, data.getNumberFloat(), 0.000001 );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test floating-point number string. */
		
		fp = 1337.995;
		str = String.valueOf( fp );
		data = Data.numberData( fp );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NUMBER, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertTrue( data.isNumber() );
		assertTrue( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( str, data.getString() );
		assertEquals( str, data.getNumber() );
		assertEquals( (long) fp, data.getNumberInteger() );
		assertEquals( fp, data.getNumberFloat(), 0.000001 );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test invalid string */
		
		try {
			Data.numberData( "wrong" );
			fail( "Should have thrown an exception on invalid number string." );
		} catch ( NumberFormatException e ) {
			// Expected.
		}
		
	}
	
	@Test
	public void testBoolean() {
		
		/* Test true */
		
		Data data = Data.booleanData( true );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.BOOLEAN, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertTrue( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getBoolean() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test false */
		
		data = Data.booleanData( false );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.BOOLEAN, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertTrue( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getBoolean() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
	}

	@Test
	public void testNull() {
		
		Data data = Data.nullData();
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NULL, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertTrue( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
	}
	
	@Test
	public void testList() {
		
		/* Test null list */
		
		Data data = Data.listData( (List<Data>) null );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NULL, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertTrue( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test empty list */
		
		List<Data> list = new ArrayList<>();
		data = Data.listData( new ArrayList<>( list ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.LIST, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertTrue( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertEquals( list, data.getList() );
		assertNull( data.getMap() );
		
		/* Test random list */
		
		list = new ArrayList<>();
		list.add( Data.nullData() );
		list.add( Data.booleanData( true ) );
		list.add( Data.stringData( "Hi" ) );
		list.add( Data.numberData( 11 ) );
		data = Data.listData( new ArrayList<>( list ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.LIST, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertTrue( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertEquals( list, data.getList() );
		assertNull( data.getMap() );
		
	}
	
	@Test
	public void testMap() {
		
		/* Test null map */
		
		Data data = Data.mapData( null );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.NULL, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertTrue( data.isNull() );
		assertFalse( data.isList() );
		assertFalse( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertTrue( data.getNull() );
		assertNull( data.getList() );
		assertNull( data.getMap() );
		
		/* Test empty map */
		
		Map<String,Data> map = new HashMap<>();
		data = Data.mapData( new HashMap<>( map ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.MAP, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertTrue( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertEquals( map, data.getMap() );
		
		/* Test random map */
		
		List<Data> list = new ArrayList<>();
		list.add( Data.nullData() );
		list.add( Data.booleanData( true ) );
		list.add( Data.stringData( "Hi" ) );
		list.add( Data.numberData( 11 ) );
		
		map = new HashMap<>();
		map.put( "A value", Data.stringData( "foobar" ) );
		map.put( "A key", Data.listData( list ) );
		map.put( "stuff", Data.nullData() );
		data = Data.mapData( new HashMap<>( map ) );
		
		// Check type.
		assertEquals( "Incorrect data type.", Data.Type.MAP, data.getType() );
		
		// Check test methods.
		assertFalse( data.isString() );
		assertFalse( data.isNumber() );
		assertFalse( data.isFloat() );
		assertFalse( data.isBoolean() );
		assertFalse( data.isNull() );
		assertFalse( data.isList() );
		assertTrue( data.isMap() );
		
		// Check data.
		assertNull( data.getString() );
		assertNull( data.getNumber() );
		assertFalse( data.getNull() );
		assertNull( data.getList() );
		assertEquals( map, data.getMap() );
		
	}

}
