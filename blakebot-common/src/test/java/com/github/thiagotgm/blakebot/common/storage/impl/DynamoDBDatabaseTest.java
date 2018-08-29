package com.github.thiagotgm.blakebot.common.storage.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class DynamoDBDatabaseTest {
	
	private DynamoDBDatabase db;

	@Before
	public void setUp() throws Exception {
		
		db = new DynamoDBDatabase();
		assertTrue( db.load( Arrays.asList( "yes", "8000", "", "" ) ) );
		
	}

	@After
	public void tearDown() throws Exception {
		
		try {
			db.close();
		} catch ( IllegalStateException e ) {
			// Normal.
		}
		
	}

	@Test
	public void test() throws Exception {
		
		String tableName = "Movies";

		System.out.println("Attempting to create table; please wait...");
        Table table;
        try {
        	table = db.dynamoDB.createTable(tableName,
            Arrays.asList(new KeySchemaElement("year", KeyType.HASH), // Partition
                                                                      // key
                new KeySchemaElement("title", KeyType.RANGE)), // Sort key
            Arrays.asList(new AttributeDefinition("year", ScalarAttributeType.N),
                new AttributeDefinition("title", ScalarAttributeType.S)),
            new ProvisionedThroughput(10L, 10L));
        } catch ( ResourceInUseException e ) {
        	table = db.dynamoDB.getTable( tableName );
        }
        table.waitForActive();
        System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());
        
        int year = 2015;
        String title = "The Big New Movie";

        final Map<String, Object> infoMap = new HashMap<String, Object>();
        infoMap.put("plot", "Nothing happens at all.");
        infoMap.put("rating", 0);

        System.out.println("Adding a new item...");
        PutItemOutcome outcome = table
            .putItem(new Item().withPrimaryKey("year", year, "title", title).withMap("info", infoMap));

        System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

	}

}
