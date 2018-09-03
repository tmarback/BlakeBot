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

package com.github.thiagotgm.blakebot.common.storage.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.translate.DataTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;

/**
 * Unit tests for {@link DynamoDBDatabase}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-30
 */
public class DynamoDBDatabaseTest {
	
	private static final Map<String,Data> TEST_DB_MAPPINGS = new HashMap<>();
	
	static {
		
		TEST_DB_MAPPINGS.put( "foobar", Data.listData( Data.numberData( 23 ), Data.numberData( 25 ) ) );
		TEST_DB_MAPPINGS.put( "tryOne", Data.numberData( 42 ) );
		TEST_DB_MAPPINGS.put( "Boop", Data.listData( Data.stringData( "magic" ), Data.numberData( 420 ),
				Data.listData( Data.stringData( "lol" ), Data.stringData( "nested" ), Data.stringData( "lists" ) )
				) );
		
		Map<String,Data> testMap = new HashMap<>();
		testMap.put( "lettuce", Data.numberData( 53.1997 ) );
		Map<String,Data> subMap = new HashMap<>();
		subMap.put( "sauce", Data.numberData( 177013 ) );
		testMap.put( "cucumbers", Data.mapData( subMap ) );
		testMap.put( "tomatoes", Data.stringData( "ketchup" ) );
		testMap.put( "onions", Data.nullData() );
		testMap.put( "carrots", Data.listData( Data.stringData( "a" ), Data.stringData( "list" ) ) );
		
		TEST_DB_MAPPINGS.put( "read_this", Data.mapData( testMap ) );
		
	}
	
	private DynamoDBDatabase db;
	private Map<String,Data> map;

	@Before
	public void setUp() {
		
		db = new DynamoDBDatabase();
		assertTrue( db.load( Arrays.asList( "yes", "8000", "", "" ) ) );
		map = db.newMap( "BlakeBotTest", new StringTranslator(), new DataTranslator() );
		
	}
	
	@After
	public void tearDown() throws Exception {
		
		if ( hasTempTable ) {
			deleteTempTable();
		}
		
		map = null;
		try {
			db.close();
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		db = null;
		
	}
	
	private static final String TEMP_TABLE = "temp";
	
	private boolean hasTempTable = false;
	
	/**
	 * Creates a temporary table.
	 * 
	 * @return The temporary table.
	 */
	private Map<String,Data> getTempTable() {
		
		hasTempTable = true;
		return db.newMap( TEMP_TABLE, new StringTranslator(), new DataTranslator() );
		
	}
	
	/**
	 * Deletes the temporary table.
	 * 
	 * @throws Exception
	 */
	private void deleteTempTable() throws Exception {
		
		Table table = db.dynamoDB.getTable( TEMP_TABLE ); // Delete temp table.
		table.delete();
		table.waitForDelete();
		hasTempTable = false;
		
	}
	
	@Test
	public void testSize() {
		
		assertEquals( TEST_DB_MAPPINGS.size(), map.size() );
		assertEquals( 0, getTempTable().size() );
		
	}
	
	@Test
	public void testIsEmpty() {
		
		assertFalse( map.isEmpty() );
		assertTrue( getTempTable().isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testContainsKey() {
		
		for ( String key : TEST_DB_MAPPINGS.keySet() ) {
			
			assertTrue( map.containsKey( key ) ); // Check existing keys.
			
		}
		
		/* Test non-existing keys */
		
		assertFalse( map.containsKey( null ) );
		assertFalse( map.containsKey( new Integer( 10 ) ) );
		assertFalse( map.containsKey( "not here" ) );
		assertFalse( map.containsKey( "bazinga" ) );
		assertFalse( map.containsKey( "ayyy" ) );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testContainsValue() {
		
		for ( Data value : TEST_DB_MAPPINGS.values() ) {
			
			assertTrue( map.containsValue( value ) ); // Check existing keys.
			
		}
		
		/* Test non-existing keys */
		
		assertFalse( map.containsKey( null ) );
		assertFalse( map.containsKey( new Integer( 10 ) ) );
		assertFalse( map.containsKey( Data.numberData( 43 ) ) );
		assertFalse( map.containsKey( Data.nullData() ) );
		assertFalse( map.containsKey( Data.listData( Data.nullData(), Data.nullData() ) ) );
		
	}

	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testGet() {
		
		/* Check for test values */
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
		/* Check inexistent keys */
		
		assertNull( map.get( "not here" ) );
		assertNull( map.get( "" ) );
		assertNull( map.get( new Integer( 5 ) ) );

	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testPutAndRemove() {
		
		Map<String,Data> map = getTempTable(); // Use a separate table.
		
		// Add default test values
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertNull( map.put( mapping.getKey(), mapping.getValue() ) );
			
		}
		
		// Check that they were inserted.
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
		String listKey = "mail";
		String mapKey = "look a map";
		
		/* Ensure keys don't exist */
		
		assertNull( map.get( listKey ) );
		assertNull( map.get( mapKey ) );
		assertNull( map.get( "" ) );
		
		/* Try putting inexistent values */
		
		Data listData = Data.listData( Data.stringData( "you" ), Data.stringData( "got" ) );
		assertNull( map.put( listKey, listData ) );
		
		Map<String,Data> testMap = new HashMap<>();
		testMap.put( "the key", Data.stringData( "sword" ) );
		testMap.put( "dev/null", Data.nullData() );
		testMap.put( "master key", Data.listData( Data.stringData( "master sword" ), Data.numberData( 42.24 ) ) );
		Data mapData = Data.mapData( testMap );
		assertNull( map.put( mapKey , mapData ) );
		
		assertNull( map.put( "", Data.stringData( "empty" ) ) );
		
		assertEquals( listData, map.get( listKey ) ); // Check placed values.
		assertEquals( mapData, map.get( mapKey ) );
		assertEquals( Data.stringData( "empty" ), map.get( "" ) );
		
		// Check that unrelated values are untouched.
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
		/* Try replacing existing values */
		
		Data newListData = Data.nullData();
		Data newMapData = Data.booleanData( false );
		
		assertEquals( listData, map.put( listKey, newListData ) );
		assertEquals( mapData, map.put( mapKey, newMapData ) );
		assertEquals( Data.stringData( "empty" ), map.put( "", Data.stringData( "" ) ) );
		
		assertEquals( newListData, map.get( listKey ) ); // Check new values.;
		assertEquals( newMapData, map.get( mapKey ) );
		assertEquals( Data.stringData( "" ), map.get( "" ) );
		
		// Check that unrelated values are untouched.
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
		/* Try removing non-existant values */
		
		assertNull( map.remove( "none" )  );
		assertNull( map.remove( "I do not exist" ) );
		assertNull( map.remove( new Integer( 0 ) ) );
		
		assertEquals( newListData, map.get( listKey ) ); // Check placed values.
		assertEquals( newMapData, map.get( mapKey ) );
		
		// Check that unrelated values are untouched.
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
		/* Try removing existing values */
		
		assertEquals( newListData, map.remove( listKey ) );
		assertEquals( newMapData, map.remove( mapKey ) );
		assertEquals( Data.stringData( "" ), map.remove( "" ) );
		
		assertNull( map.get( listKey ) ); // Check removed.
		assertNull( map.get( mapKey ) );
		assertNull( map.get( "" ) );
		
		// Check that unrelated values are untouched.
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), map.get( mapping.getKey() ) );
			
		}
		
	}
	
	@Test
	public void testPutAll() {
		
		Map<String,Data> tempMap = getTempTable();
		
		assertTrue( tempMap.isEmpty() ); // Ensure temp is empty.
		
		tempMap.putAll( TEST_DB_MAPPINGS ); // Put all mappings.
		
		/* Check for inserted values */
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), tempMap.get( mapping.getKey() ) );
			
		}
		
	}
	
	@Test
	public void testClear() {
		
		Map<String,Data> tempMap = getTempTable();
		
		assertTrue( tempMap.isEmpty() ); // Ensure temp is empty.
		
		tempMap.putAll( TEST_DB_MAPPINGS ); // Insert mappings.
		
		assertFalse( tempMap.isEmpty() ); // Check if temp is now not empty.
		
		tempMap.clear(); // Clear mappings.
		
		assertTrue( tempMap.isEmpty() ); // Check if temp is empty again.
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testEquals() {
		
		// Check correct map.
		assertTrue( map.equals( TEST_DB_MAPPINGS ) );
		assertTrue( TEST_DB_MAPPINGS.equals( map ) );
		
		// Check non-map.
		assertFalse( map.equals( "str" ) );
		assertFalse( "str".equals( map ) );
		
		// Check empty map.
		Map<String,Data> emptyMap = new HashMap<>();
		
		assertFalse( map.equals( emptyMap ) );
		assertFalse( emptyMap.equals( map ) );
		
		// Check other map.
		Map<String,Data> otherMap = new HashMap<>();
		otherMap.put( "one", Data.stringData( "haha" ) );
		
		assertFalse( map.equals( otherMap ) );
		assertFalse( otherMap.equals( map ) );
		
		/* Test with empty map */
		
		Map<String,Data> temp = getTempTable();
		
		// Check correct map.
		assertTrue( temp.equals( emptyMap ) );
		assertTrue( emptyMap.equals( temp ) );
		
		// Check non-map.
		assertFalse( temp.equals( "str" ) );
		assertFalse( "str".equals( temp ) );
		
		// Check wrong map.
		assertFalse( temp.equals( TEST_DB_MAPPINGS ) );
		assertFalse( TEST_DB_MAPPINGS.equals( temp ) );
		
		// Check other map.
		assertFalse( temp.equals( otherMap ) );
		assertFalse( otherMap.equals( temp ) );
		
	}
	
	@Test
	public void testHashCode() {
		
		assertEquals( TEST_DB_MAPPINGS.hashCode(), map.hashCode() );
		assertEquals( new HashMap<>().hashCode(), getTempTable().hashCode() );
		
	}
	
	/* Tests for key set view */
	
	@Test
	public void testKeySetSize() {
		
		assertEquals( TEST_DB_MAPPINGS.size(), map.keySet().size() );
		assertEquals( 0, getTempTable().keySet().size() );
		
	}

	@Test
	public void testKeySetIsEmpty() {
		
		assertFalse( map.keySet().isEmpty() );
		assertTrue( getTempTable().keySet().isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testKeySetContains() {
		
		Set<String> keys = map.keySet();
		
		for ( String key : TEST_DB_MAPPINGS.keySet() ) {
			
			assertTrue( keys.contains( key ) );
			
		}
		
		assertFalse( keys.contains( null ) );
		assertFalse( keys.contains( new Integer( 42 ) ) );
		assertFalse( keys.contains( "lololol" ) );
		assertFalse( keys.contains( "wroooooooong" ) );
		
	}
	
	@Test
	public void testKeySetIterator() {
		
		Iterator<String> iter = map.keySet().iterator();
		Set<String> expected = new HashSet<>( TEST_DB_MAPPINGS.keySet() );
		
		while ( iter.hasNext() ) { // Check if iterator only returns expected values.
			
			assertTrue( expected.remove( iter.next() ) );
			
		}
		
		assertTrue( expected.isEmpty() ); // Ensure all expected values were returned.
		
		assertFalse( getTempTable().keySet().iterator().hasNext() );
		
	}
	
	@Test
	public void testKeySetIteratorRemove() {
		
		Map<String,Data> map = getTempTable();
		
		map.put( "one", Data.numberData( 1 ) );
		map.put( "two", Data.numberData( 2 ) );
		map.put( "three", Data.numberData( 3 ) );
		
		Set<String> toFind = new HashSet<>( map.keySet() );
		
		Iterator<String> iter  = map.keySet().iterator();
		
		try { // Try removing before calling next.
			iter.remove();
			fail( "Should have thrown an exception." );
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		
		while ( iter.hasNext() ) {
			
			String next = iter.next();
			toFind.remove( next );
			if ( next.equals( "two" ) ) {
				iter.remove(); // Remove one element.
				
				try { // Try removing twice.
					iter.remove();
					fail( "Should have thrown an exception." );
				} catch ( IllegalStateException e ) {
					// Normal.
				}
			}
			
		}
		
		assertTrue( toFind.isEmpty() ); // Ensure iterated over everything.
		
		assertTrue( map.containsKey( "one" ) );  // Check that only the right one
		assertFalse( map.containsKey( "two" ) ); // got removed.
		assertTrue( map.containsKey( "three" ) );
		
	}
	
	@Test
	public void testKeySetToArrayObj() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.keySet().toArray() );
		List<Object> actual = Arrays.asList( map.keySet().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().keySet().toArray() );
		actual = Arrays.asList( getTempTable().keySet().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		assertTrue( Arrays.deepEquals( getTempTable().keySet().toArray(),
				new HashMap<>().keySet().toArray() ) );
		
	}
	
	@Test
	public void testKeySetToArray() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.keySet().toArray( new Object[15] ) );
		List<Object> actual = Arrays.asList( map.keySet().toArray( new Object[15] ) );
		int size = TEST_DB_MAPPINGS.size();
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().keySet().toArray( new Object[15] ) );
		actual = Arrays.asList( getTempTable().keySet().toArray( new Object[15] ) );
		size = 0;
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testKeySetAdd() {
		
		getTempTable().keySet().add( "fail" );
		
	}
	
	@Test
	public void testKeySetRemove() {
		
		Map<String,Data> map = getTempTable();
		
		map.put( "testing 1", Data.stringData( "toRemove" ) );
		map.put( "testing 2", Data.nullData() );
		map.put( "testing 3", Data.numberData( 5 ) );
		
		assertTrue( map.keySet().remove( "testing 2" ) );
		
		assertEquals( Data.stringData( "toRemove" ), map.get( "testing 1" ) );
		assertNull( map.get( "testing 2" ) );
		assertEquals( Data.numberData( 5 ), map.get( "testing 3" ) );
		
	}
	
	@Test
	public void testKeySetContainsAll() {
		
		assertTrue( map.keySet().containsAll( TEST_DB_MAPPINGS.keySet() ) );
		
		Map<String,Data> otherMap = new HashMap<>( TEST_DB_MAPPINGS );
		otherMap.put( "plane", Data.booleanData( false ) );
		
		assertFalse( map.keySet().containsAll( otherMap.keySet() ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testKeySetAddAll() {
		
		getTempTable().keySet().addAll( TEST_DB_MAPPINGS.keySet() );
		
	}
	
	@Test
	public void testKeySetRemoveAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Set<String> keys = map.keySet();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( keys.removeAll( toRemove.keySet() ) ); // Remove.
		
		for ( String key : toRemove.keySet() ) { // Check removed.
			
			assertFalse( keys.contains( key ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( keys.containsAll( toRetain.keySet() ) );
		
		assertFalse( keys.removeAll( toRemove.keySet() ) ); // Try removing again.
		
	}
	
	@Test
	public void testKeySetRetainAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Set<String> keys = map.keySet();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( keys.retainAll( toRetain.keySet() ) ); // Remove.
		
		for ( String key : toRemove.keySet() ) { // Check removed.
			
			assertFalse( keys.contains( key ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( keys.containsAll( toRetain.keySet() ) );
		
		assertFalse( keys.removeAll( toRemove.keySet() ) ); // Try removing again.
		
	}
	
	@Test
	public void testKeySetClear() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		
		assertFalse( map.isEmpty() );
		
		map.keySet().clear();
		
		assertTrue( map.isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testKeySetEquals() {
		
		Set<String> keys = map.keySet();
		
		// Check correct map.
		assertTrue( keys.equals( TEST_DB_MAPPINGS.keySet() ) );
		assertTrue( TEST_DB_MAPPINGS.keySet().equals( keys ) );
		
		// Check correct map but the map itself.
		assertFalse( keys.equals( TEST_DB_MAPPINGS ) );
		assertFalse( TEST_DB_MAPPINGS.equals( keys ) );
		
		// Check non-map.
		assertFalse( keys.equals( "str" ) );
		assertFalse( "str".equals( keys ) );
		
		// Check empty map.
		Map<String,Data> emptyMap = new HashMap<>();
		
		assertFalse( keys.equals( emptyMap.keySet() ) );
		assertFalse( emptyMap.keySet().equals( keys ) );
		
		// Check other map.
		Map<String,Data> otherMap = new HashMap<>();
		otherMap.put( "one", Data.stringData( "haha" ) );
		
		assertFalse( keys.equals( otherMap.keySet() ) );
		assertFalse( otherMap.keySet().equals( keys ) );
		
		/* Test with empty map */
		
		keys = getTempTable().keySet();
		
		// Check correct map.
		assertTrue( keys.equals( emptyMap.keySet() ) );
		assertTrue( emptyMap.keySet().equals( keys ) );
		
		// Check correct map but the map itself.
		assertFalse( keys.equals( emptyMap ) );
		assertFalse( emptyMap.equals( keys ) );
		
		// Check non-map.
		assertFalse( keys.equals( "str" ) );
		assertFalse( "str".equals( keys ) );
		
		// Check wrong map.
		assertFalse( keys.equals( TEST_DB_MAPPINGS.keySet() ) );
		assertFalse( TEST_DB_MAPPINGS.keySet().equals( keys ) );
		
		// Check other map.
		assertFalse( keys.equals( otherMap.keySet() ) );
		assertFalse( otherMap.keySet().equals( keys ) );
		
	}
	
	@Test
	public void testKeySetHashCode() {
		
		assertEquals( TEST_DB_MAPPINGS.keySet().hashCode(), map.keySet().hashCode() );
		assertEquals( new HashMap<>().keySet().hashCode(), getTempTable().keySet().hashCode() );
		
	}
	
	/* Tests for value collection view */
	
	@Test
	public void testValueCollectionSize() {
		
		assertEquals( TEST_DB_MAPPINGS.size(), map.values().size() );
		assertEquals( 0, getTempTable().values().size() );
		
	}

	@Test
	public void testValueCollectionIsEmpty() {
		
		assertFalse( map.values().isEmpty() );
		assertTrue( getTempTable().values().isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testValueCollectionContains() {
		
		Collection<Data> values = map.values();
		
		for ( Data value : TEST_DB_MAPPINGS.values() ) {
			
			assertTrue( values.contains( value ) );
			
		}
		
		assertFalse( values.contains( null ) );
		assertFalse( values.contains( new Integer( 42 ) ) );
		assertFalse( values.contains( Data.nullData() ) );
		assertFalse( values.contains( Data.stringData( "thinkTak" ) ) );
		
	}
	
	@Test
	public void testValueCollectionIterator() {
		
		Iterator<Data> iter = map.values().iterator();
		Collection<Data> expected = new ArrayList<>( TEST_DB_MAPPINGS.values() );
		
		while ( iter.hasNext() ) { // Check if iterator only returns expected values.
			
			assertTrue( expected.remove( iter.next() ) );
			
		}
		
		assertTrue( expected.isEmpty() ); // Ensure all expected values were returned.
		
		assertFalse( getTempTable().values().iterator().hasNext() );
		
	}
	
	@Test
	public void testValueCollectionIteratorRemove() {
		
		Map<String,Data> map = getTempTable();
		
		map.put( "one", Data.numberData( 1 ) );
		map.put( "two", Data.numberData( 2 ) );
		map.put( "two.2", Data.numberData( 2 ) );
		map.put( "three", Data.numberData( 3 ) );
		
		Collection<Data> toFind = new ArrayList<>( map.values() );
		
		boolean shouldDelete = true;
		Iterator<Data> iter = map.values().iterator();
		
		try { // Try removing before calling next.
			iter.remove();
			fail( "Should have thrown an exception." );
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		
		while ( iter.hasNext() ) {
			
			Data next = iter.next();
			toFind.remove( next );
			if ( shouldDelete && next.equals( Data.numberData( 2 ) ) ) {
				iter.remove(); // Remove one of 2 possible elements.
				shouldDelete = false;
				
				try { // Try removing twice.
					iter.remove();
					fail( "Should have thrown an exception." );
				} catch ( IllegalStateException e ) {
					// Normal.
				}
			}
			
		}
		
		assertTrue( toFind.isEmpty() ); // Ensure iterated over everything.
		
		assertTrue( map.containsKey( "one" ) ); // Check that exactly one of the possible elements
		assertFalse( map.containsKey( "two" ) && map.containsKey( "two.2" ) ); // got removed, and
		assertTrue( map.containsKey( "two" ) || map.containsKey( "two.2" ) );  // nothing else.
		assertTrue( map.containsKey( "three" ) );
		
	}
	
	@Test
	public void testValueCollectionToArrayObj() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.values().toArray() );
		List<Object> actual = Arrays.asList( map.values().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().values().toArray() );
		actual = Arrays.asList( getTempTable().values().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		assertTrue( Arrays.deepEquals( getTempTable().values().toArray(),
				new HashMap<>().values().toArray() ) );
		
	}
	
	@Test
	public void testValueCollectionToArray() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.values().toArray( new Object[15] ) );
		List<Object> actual = Arrays.asList( map.values().toArray( new Object[15] ) );
		int size = TEST_DB_MAPPINGS.size();
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().values().toArray( new Object[15] ) );
		actual = Arrays.asList( getTempTable().values().toArray( new Object[15] ) );
		size = 0;
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testValueCollectionAdd() {
		
		getTempTable().keySet().add( "fail" );
		
	}
	
	@Test
	public void testValueCollectionRemove() {
		
		Map<String,Data> map = getTempTable();
		
		map.put( "testing 1", Data.stringData( "toRemove" ) );
		map.put( "testing 2", Data.nullData() );
		map.put( "testing 2.5", Data.nullData() );
		map.put( "testing 3", Data.numberData( 5 ) );
		
		assertTrue( map.values().remove( Data.nullData() ) );
		
		assertEquals( Data.stringData( "toRemove" ), map.get( "testing 1" ) );
		assertTrue( ( map.get( "testing 2" ) == null ) || // Ensure exactly one got removed.
				    ( map.get( "testing 2.5" ) == null ) );
		assertTrue( Data.nullData().equals( map.get( "testing 2" ) ) ||
				    Data.nullData().equals( map.get( "testing 2.5" ) ) );
		assertEquals( Data.numberData( 5 ), map.get( "testing 3" ) );
		
	}
	
	@Test
	public void testValueCollectionContainsAll() {
		
		assertTrue( map.values().containsAll( TEST_DB_MAPPINGS.values() ) );
		
		Map<String,Data> otherMap = new HashMap<>( TEST_DB_MAPPINGS );
		otherMap.put( "plane", Data.booleanData( false ) );
		
		assertFalse( map.values().containsAll( otherMap.values() ) );
		
		otherMap = new HashMap<>( TEST_DB_MAPPINGS );
		otherMap.put( "plane", map.values().iterator().next() );
		
		assertTrue( map.values().containsAll( otherMap.values() ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testValueCollectionAddAll() {
		
		getTempTable().values().addAll( TEST_DB_MAPPINGS.values() );
		
	}
	
	@Test
	public void testValueCollectionRemoveAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Collection<Data> values = map.values();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( values.removeAll( toRemove.values() ) ); // Remove.
		
		for ( Data value : toRemove.values() ) { // Check removed.
			
			assertFalse( values.contains( value ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( values.containsAll( toRetain.values() ) );
		
		assertFalse( values.removeAll( toRemove.values() ) ); // Try removing again.
		
	}
	
	@Test
	public void testValueCollectionRetainAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Collection<Data> values = map.values();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( values.retainAll( toRetain.values() ) ); // Remove.
		
		for ( Data value : toRemove.values() ) { // Check removed.
			
			assertFalse( values.contains( value ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( values.containsAll( toRetain.values() ) );
		
		assertFalse( values.removeAll( toRemove.values() ) ); // Try removing again.
		
	}
	
	@Test
	public void testValueCollectionClear() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		
		assertFalse( map.isEmpty() );
		
		map.values().clear();
		
		assertTrue( map.isEmpty() );
		
	}
	
	/* Tests for entry set view */
	
	@Test
	public void testEntrySetSize() {
		
		assertEquals( TEST_DB_MAPPINGS.size(), map.entrySet().size() );
		assertEquals( 0, getTempTable().entrySet().size() );
		
	}

	@Test
	public void testEntrySetIsEmpty() {
		
		assertFalse( map.entrySet().isEmpty() );
		assertTrue( getTempTable().entrySet().isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testEntrySetContains() {
		
		Set<Map.Entry<String,Data>> entries = map.entrySet();
		
		for ( Map.Entry<String,Data> entry : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertTrue( entries.contains( entry ) );
			
		}
		
		assertFalse( entries.contains( null ) );
		assertFalse( entries.contains( new Integer( 42 ) ) );
		assertFalse( entries.contains( "lololol" ) );
		
		Map<String,Data> otherMap = new HashMap<>();
		otherMap.put( "wolololo", Data.nullData() );
		otherMap.put( "trollface", Data.numberData( "4.999" ) );
		
		for ( Map.Entry<String,Data> entry : otherMap.entrySet() ) {
			
			assertFalse( entries.contains( entry ) );
			
		}
		
	}
	
	@Test
	public void testEntrySetIterator() {
		
		Iterator<Map.Entry<String,Data>> iter = map.entrySet().iterator();
		Set<Map.Entry<String,Data>> expected = new HashSet<>( TEST_DB_MAPPINGS.entrySet() );
		
		while ( iter.hasNext() ) { // Check if iterator only returns expected values.
			
			assertTrue( expected.remove( iter.next() ) );
			
		}
		
		assertTrue( expected.isEmpty() ); // Ensure all expected values were returned.
		
		assertFalse( getTempTable().entrySet().iterator().hasNext() );
		
	}
	
	@Test
	public void testEntrySetIteratorRemove() {
		
		Map<String,Data> map = getTempTable();
		
		map.put( "one", Data.numberData( 1 ) );
		map.put( "two", Data.numberData( 2 ) );
		map.put( "three", Data.numberData( 3 ) );
		
		Set<Map.Entry<String,Data>> toFind = new HashSet<>( map.entrySet() );
		Iterator<Map.Entry<String,Data>> iter = map.entrySet().iterator();
		
		try { // Try removing before calling next.
			iter.remove();
			fail( "Should have thrown an exception." );
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toFind.remove( next );
			if ( next.getKey().equals( "two" ) ) {
				iter.remove(); // Remove one element.
				
				try { // Try removing twice.
					iter.remove();
					fail( "Should have thrown an exception." );
				} catch ( IllegalStateException e ) {
					// Normal.
				}
			}
			
		}
		
		assertTrue( toFind.isEmpty() ); // Ensure iterated over everything.
		
		assertTrue( map.containsKey( "one" ) );  // Check that only the right one
		assertFalse( map.containsKey( "two" ) ); // got removed.
		assertTrue( map.containsKey( "three" ) );
		
	}
	
	@Test
	public void testEntry() {
		
		Map<String,Data> map = getTempTable();
		map.put( "one", Data.numberData( 1 ) );
		map.put( "two", Data.numberData( 2 ) );
		map.put( "three", Data.numberData( 3 ) );
		
		for ( Map.Entry<String,Data> entry : map.entrySet() ) {
			
			switch ( entry.getKey() ) {
			
				case "one":
					assertEquals( Data.numberData( 1 ), entry.getValue() );
					break;
					
				case "two":
					assertEquals( Data.numberData( 2 ), entry.getValue() );
					assertEquals( Data.numberData( 2 ),
							entry.setValue( Data.numberData( 4 ) ) ); // Try setting value.
					assertEquals( Data.numberData( 4 ), entry.getValue() );
					break;
					
				case "three":
					assertEquals( Data.numberData( 3 ), entry.getValue() );
					break;
					
				default:
					fail( "Unexpected key returned" );
			
			}
			
		}
		
		assertEquals( 3, map.size() );
		assertEquals( Data.numberData( 1 ), map.get( "one" ) );
		assertEquals( Data.numberData( 4 ), map.get( "two" ) );
		assertEquals( Data.numberData( 3 ), map.get( "three" ) );
		
	}
	
	@Test
	public void testEntrySetToArrayObj() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.entrySet().toArray() );
		List<Object> actual = Arrays.asList( map.entrySet().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().entrySet().toArray() );
		actual = Arrays.asList( getTempTable().entrySet().toArray() );
		
		assertEquals( expected.size(), actual.size() );
		assertFalse( expected.retainAll( actual ) );
		
		assertTrue( Arrays.deepEquals( getTempTable().entrySet().toArray(),
				new HashMap<>().entrySet().toArray() ) );
		
	}
	
	@Test
	public void testEntrySetToArray() {
		
		List<Object> expected = Arrays.asList( TEST_DB_MAPPINGS.entrySet().toArray( new Object[15] ) );
		List<Object> actual = Arrays.asList( map.entrySet().toArray( new Object[15] ) );
		int size = TEST_DB_MAPPINGS.size();
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
		/* Test with empty map */
		
		expected = Arrays.asList( new HashMap<>().entrySet().toArray( new Object[15] ) );
		actual = Arrays.asList( getTempTable().entrySet().toArray( new Object[15] ) );
		size = 0;
		
		assertEquals( expected.size(), actual.size() );
		assertEquals( expected.subList( size, expected.size() ), actual.subList( size, expected.size() )  );
		assertFalse( expected.retainAll( actual ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testEntrySetAdd() {
		
		getTempTable().keySet().add( "fail" );
		
	}
	
	@Test
	public void testEntrySetRemove() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		Map.Entry<String,Data> toDelete = iter.next(); // Get a mapping to delete.
		
		assertTrue( map.entrySet().remove( toDelete ) ); // Delete the mapping.
		
		assertFalse( map.containsKey( toDelete.getKey() ) );
		
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			assertEquals( next.getValue(), map.get( next.getKey() ) );
			
		}
		
		assertFalse( map.entrySet().remove( toDelete ) ); // Try deleting twice.
		
		map.put( "aTest", Data.numberData( -54 ) );
		
		Map<String,Data> otherMap = new HashMap<>();
		otherMap.put( "aTest", Data.nullData() );
		
		// Try entry with the right key but wrong value.
		assertFalse( map.entrySet().remove( otherMap.entrySet().iterator().next() ) );
		
		// Now with right value.
		otherMap.put( "aTest", Data.numberData( -54 ) );
		assertTrue( map.entrySet().remove( otherMap.entrySet().iterator().next() ) );
		
		assertFalse( map.containsKey( "aTest" ) );
		
		assertEquals( TEST_DB_MAPPINGS.size() - 1, map.size() );
		
	}
	
	@Test
	public void testEntrySetContainsAll() {
		
		assertTrue( map.entrySet().containsAll( TEST_DB_MAPPINGS.entrySet() ) );
		
		Map<String,Data> otherMap = new HashMap<>( TEST_DB_MAPPINGS );
		otherMap.put( "plane", Data.booleanData( false ) );
		
		assertFalse( map.entrySet().containsAll( otherMap.entrySet() ) );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testEntrySetAddAll() {
		
		getTempTable().entrySet().addAll( TEST_DB_MAPPINGS.entrySet() );
		
	}
	
	@Test
	public void testEntrySetRemoveAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Set<Map.Entry<String,Data>> entries = map.entrySet();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( entries.removeAll( toRemove.entrySet() ) ); // Remove.
		
		for ( Map.Entry<String,Data> entry : toRemove.entrySet() ) { // Check removed.
			
			assertFalse( entries.contains( entry ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( entries.containsAll( toRetain.entrySet() ) );
		
		assertFalse( entries.removeAll( toRemove.entrySet() ) ); // Try removing again.
		
	}
	
	@Test
	public void testEntrySetRetainAll() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		Set<Map.Entry<String,Data>> entries = map.entrySet();
		
		Iterator<Map.Entry<String,Data>> iter = TEST_DB_MAPPINGS.entrySet().iterator();
		
		Map<String,Data> toRemove = new HashMap<>();
		for ( int i = 0; i <= TEST_DB_MAPPINGS.size() / 2; i++ ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRemove.put( next.getKey(), next.getValue() );
			
		}
		
		Map<String,Data> toRetain = new HashMap<>();
		while ( iter.hasNext() ) {
			
			Map.Entry<String,Data> next = iter.next();
			toRetain.put( next.getKey(), next.getValue() );
			
		}
		
		assertTrue( entries.retainAll( toRetain.entrySet() ) ); // Remove.
		
		for ( Map.Entry<String,Data> entry : toRemove.entrySet() ) { // Check removed.
			
			assertFalse( entries.contains( entry ) );
			
		}
		
		// Check nothing else was removed.
		assertTrue( entries.containsAll( toRetain.entrySet() ) );
		
		assertFalse( entries.removeAll( toRemove.entrySet() ) ); // Try removing again.
		
	}
	
	@Test
	public void testEntrySetClear() {
		
		Map<String,Data> map = getTempTable();
		map.putAll( TEST_DB_MAPPINGS );
		
		assertFalse( map.isEmpty() );
		
		map.entrySet().clear();
		
		assertTrue( map.isEmpty() );
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testEntrySetEquals() {
		
		Set<Map.Entry<String,Data>> entries = map.entrySet();
		
		// Check correct map.
		assertTrue( entries.equals( TEST_DB_MAPPINGS.entrySet() ) );
		assertTrue( TEST_DB_MAPPINGS.entrySet().equals( entries ) );
		
		// Check correct map but the map itself.
		assertFalse( entries.equals( TEST_DB_MAPPINGS ) );
		assertFalse( TEST_DB_MAPPINGS.equals( entries ) );
		
		// Check non-map.
		assertFalse( entries.equals( "str" ) );
		assertFalse( "str".equals( entries ) );
		
		// Check empty map.
		Map<String,Data> emptyMap = new HashMap<>();
		
		assertFalse( entries.equals( emptyMap.entrySet() ) );
		assertFalse( emptyMap.entrySet().equals( entries ) );
		
		// Check other map.
		Map<String,Data> otherMap = new HashMap<>();
		otherMap.put( "one", Data.stringData( "haha" ) );
		
		assertFalse( entries.equals( otherMap.entrySet() ) );
		assertFalse( otherMap.entrySet().equals( entries ) );
		
		/* Test with empty map */
		
		entries = getTempTable().entrySet();
		
		// Check correct map.
		assertTrue( entries.equals( emptyMap.entrySet() ) );
		assertTrue( emptyMap.entrySet().equals( entries ) );
		
		// Check correct map but the map itself.
		assertFalse( entries.equals( emptyMap ) );
		assertFalse( emptyMap.equals( entries ) );
		
		// Check non-map.
		assertFalse( entries.equals( "str" ) );
		assertFalse( "str".equals( entries ) );
		
		// Check wrong map.
		assertFalse( entries.equals( TEST_DB_MAPPINGS.entrySet() ) );
		assertFalse( TEST_DB_MAPPINGS.entrySet().equals( entries ) );
		
		// Check other map.
		assertFalse( entries.equals( otherMap.entrySet() ) );
		assertFalse( otherMap.entrySet().equals( entries ) );
		
	}
	
	@Test
	public void testEntrySetHashCode() {
		
		assertEquals( TEST_DB_MAPPINGS.entrySet().hashCode(), map.entrySet().hashCode() );
		assertEquals( new HashMap<>().entrySet().hashCode(), getTempTable().entrySet().hashCode() );
		
	}

}
