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
import java.util.HashMap;
import java.util.Map;

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
	
	{
		
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
	public void setUp() throws Exception {
		
		db = new DynamoDBDatabase();
		assertTrue( db.load( Arrays.asList( "yes", "8000", "", "" ) ) );
		map = db.newMap( "BlakeBotTest", new StringTranslator(), new DataTranslator() );
		
	}

	@After
	public void tearDown() throws Exception {
		
		map = null;
		try {
			db.close();
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		db = null;
		
	}
	
	private static final String TEMP_TABLE = "temp";
	
	/**
	 * Creates a temporary table.
	 * 
	 * @return The temporary table.
	 */
	private Map<String,Data> getTempTable() {
		
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
		
	}
	
	@Test
	public void testSize() throws Exception {
		
		assertEquals( TEST_DB_MAPPINGS.size(), map.size() );
		assertEquals( 0, db.newMap( TEMP_TABLE, new StringTranslator(), new DataTranslator() ).size() );
		
		deleteTempTable();
		
	}
	
	@Test
	public void testIsEmpty() throws Exception {
		
		assertFalse( map.isEmpty() );
		assertTrue( getTempTable().isEmpty() );

		deleteTempTable();
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testContainsKey() throws Exception {
		
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
	public void testContainsValue() throws Exception {
		
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
	public void testGet() throws Exception {
		
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
	public void testPutAndRemove() throws Exception {
		
		String listKey = "mail";
		String mapKey = "look a map";
		
		/* Ensure keys don't exist */
		
		assertNull( map.get( listKey ) );
		assertNull( map.get( mapKey ) );
		
		/* Try putting inexistent values */
		
		Data listData = Data.listData( Data.stringData( "you" ), Data.stringData( "got" ) );
		assertNull( map.put( listKey, listData ) );
		
		Map<String,Data> testMap = new HashMap<>();
		testMap.put( "the key", Data.stringData( "sword" ) );
		testMap.put( "dev/null", Data.nullData() );
		testMap.put( "master key", Data.listData( Data.stringData( "master sword" ), Data.numberData( 42.24 ) ) );
		Data mapData = Data.mapData( testMap );
		assertNull( map.put( mapKey , mapData ) );
		
		assertEquals( listData, map.get( listKey ) ); // Check placed values.
		assertEquals( mapData, map.get( mapKey ) );
		
		testGet(); // Check that unrelated values are untouched.
		
		/* Try replacing existing values */
		
		Data newListData = Data.nullData();
		Data newMapData = Data.booleanData( false );
		
		assertEquals( listData, map.put( listKey, newListData ) );
		assertEquals( mapData, map.put( mapKey, newMapData ) );
		
		assertEquals( newListData, map.get( listKey ) ); // Check new values.
		assertEquals( newMapData, map.get( mapKey ) );
		
		testGet(); // Check that unrelated values are untouched.
		
		/* Try removing non-existant values */
		
		assertNull( map.remove( "none" )  );
		assertNull( map.remove( "I do not exist" ) );
		assertNull( map.remove( new Integer( 0 ) ) );
		
		assertEquals( newListData, map.get( listKey ) ); // Check placed values.
		assertEquals( newMapData, map.get( mapKey ) );
		
		testGet(); // Check that unrelated values are untouched.
		
		/* Try removing existing values */
		
		assertEquals( newListData, map.remove( listKey ) );
		assertEquals( newMapData, map.remove( mapKey ) );
		
		assertNull( map.get( listKey ) ); // Check removed.
		assertNull( map.get( mapKey ) );
		
		testGet(); // Check that unrelated values are untouched.
		
	}
	
	@Test
	public void testPutAll() throws Exception {
		
		Map<String,Data> tempMap = getTempTable();
		
		assertTrue( tempMap.isEmpty() ); // Ensure temp is empty.
		
		tempMap.putAll( TEST_DB_MAPPINGS ); // Put all mappings.
		
		/* Check for inserted values */
		for ( Map.Entry<String,Data> mapping : TEST_DB_MAPPINGS.entrySet() ) {
			
			assertEquals( mapping.getValue(), tempMap.get( mapping.getKey() ) );
			
		}
		
		deleteTempTable();
		
	}
	
	@Test
	public void testClear() throws Exception {
		
		Map<String,Data> tempMap = getTempTable();
		
		assertTrue( tempMap.isEmpty() ); // Ensure temp is empty.
		
		tempMap.putAll( TEST_DB_MAPPINGS ); // Insert mappings.
		
		assertFalse( tempMap.isEmpty() ); // Check if temp is now not empty.
		
		tempMap.clear(); // Clear mappings.
		
		assertTrue( tempMap.isEmpty() ); // Check if temp is empty again.
		
		deleteTempTable();
		
	}
	
	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testEquals() throws Exception {
		
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
		
		deleteTempTable();
		
	}
	
	@Test
	public void testHashCode() throws Exception {
		
		assertEquals( TEST_DB_MAPPINGS, map.hashCode() );
		assertEquals( new HashMap<>().hashCode(), getTempTable().hashCode() );
		deleteTempTable();
		
	}
	
	/* Tests for key set view */
	
	@Test
	public void testKeySetSize() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}

	@Test
	public void testKeySetIsEmpty() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetContains() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetIterator() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetIteratorRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetToArrayObj() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetToArray() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testKeySetAdd() {
		
		map.keySet().add( "fail" );
		
	}
	
	@Test
	public void testKeySetRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetContainsAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testKeySetAddAll() {
		
		map.keySet().addAll( new ArrayList<>( 1 ) );
		
	}
	
	@Test
	public void testKeySetRemoveAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetRetainAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetClear() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetEquals() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testKeySetHashCode() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	/* Tests for value collection view */
	
	@Test
	public void testValueCollectionSize() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}

	@Test
	public void testValueCollectionIsEmpty() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionContains() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionIterator() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionIteratorRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionToArrayObj() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionToArray() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testValueCollectionAdd() {
		
		map.keySet().add( "fail" );
		
	}
	
	@Test
	public void testValueCollectionRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionContainsAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testValueCollectionAddAll() {
		
		map.keySet().addAll( new ArrayList<>( 1 ) );
		
	}
	
	@Test
	public void testValueCollectionRemoveAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionRetainAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionClear() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionEquals() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testValueCollectionHashCode() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	/* Tests for entry set view */
	
	@Test
	public void testEntrySetSize() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}

	@Test
	public void testEntrySetIsEmpty() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetContains() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetIterator() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetIteratorRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetToArrayObj() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetToArray() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testEntrySetAdd() {
		
		map.keySet().add( "fail" );
		
	}
	
	@Test
	public void testEntrySetRemove() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetContainsAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test( expected = UnsupportedOperationException.class )
	public void testEntrySetAddAll() {
		
		map.keySet().addAll( new ArrayList<>( 1 ) );
		
	}
	
	@Test
	public void testEntrySetRemoveAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetRetainAll() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetClear() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetEquals() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}
	
	@Test
	public void testEntrySetHashCode() {
		
		// TODO
		fail( "Not implemented yet" );
		
	}

}
