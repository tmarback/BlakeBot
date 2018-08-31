package com.github.thiagotgm.blakebot.common.storage.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.translate.DataTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;

public class DynamoDBDatabaseTest {
	
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

	@Test
	public void testGet() throws Exception {
		
		assertEquals( Data.listData( Data.numberData( 23 ), Data.numberData( 25 ) ), map.get( "foobar" ) );
		assertEquals( Data.numberData( 42 ), map.get( "tryOne" ) );
		assertEquals( Data.listData( Data.stringData( "magic" ), Data.numberData( 420 ), Data.listData(
				Data.stringData( "lol" ), Data.stringData( "nested" ), Data.stringData( "lists" ) ) ),
				map.get( "Boop" ) );
		
		Map<String,Data> testMap = new HashMap<>();
		testMap.put( "lettuce", Data.numberData( 53.1997 ) );
		Map<String,Data> subMap = new HashMap<>();
		subMap.put( "sauce", Data.numberData( 177013 ) );
		testMap.put( "cucumbers", Data.mapData( subMap ) );
		testMap.put( "tomatoes", Data.stringData( "ketchup" ) );
		testMap.put( "onions", Data.nullData() );
		testMap.put( "carrots", Data.listData( Data.stringData( "a" ), Data.stringData( "list" ) ) );
		
		assertEquals( Data.mapData( testMap ), map.get( "read_this" ) );

	}
	
	@Test
	public void testPutAndRemove() throws Exception {
		
		map.put( "mail", Data.listData( Data.stringData( "you" ), Data.stringData( "got" ) ) );
		
		Map<String,Data> testMap = new HashMap<>();
		testMap.put( "the key", Data.stringData( "sword" ) );
		testMap.put( "dev/null", Data.nullData() );
		testMap.put( "master key", Data.listData( Data.stringData( "master sword" ), Data.numberData( 42.24 ) ) );
		map.put( "look a map" , Data.mapData( testMap ) );
		
	}

}
