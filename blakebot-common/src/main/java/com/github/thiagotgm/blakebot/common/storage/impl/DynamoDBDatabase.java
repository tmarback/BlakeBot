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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;

/**
 * Database that uses a DynamoDB backed, either locally or using the
 * AWS service.
 * <p>
 * When a map is requested, the table of matching name will be used, and if
 * one does not exist, a new one is created. In the latter case, the read and
 * write capacity units that the created table is set to are {@value #DEFAULT_READ_UNITS}
 * and {@value #DEFAULT_WRITE_UNITS}, respectively. They may be changed later using the
 * console for the backed DynamoDB database (the AWS console, for example).
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-28
 */
public class DynamoDBDatabase extends TableDatabase {
	
	private static final Logger LOG = LoggerFactory.getLogger( DynamoDBDatabase.class );
	
	private static final List<String> REGIONS;
	
	static {
		
		Regions[] regions = Regions.values();
		REGIONS = new ArrayList<>( regions.length );
		for ( Regions region : regions ) {
			
			REGIONS.add( region.toString() );
			
		}
		
	}
	
	private static final List<Parameter> PARAMS = Arrays.asList(
			new Parameter( "Local", true ),
            new Parameter( Arrays.asList( "Port", "Access key" ) ),
            new Parameter( Arrays.asList( "(ignored)", "Secret Key" ) ),
            new Parameter( Arrays.asList( "(ignored)", "Region" ), REGIONS ) );
	
	private static final String KEY_ATTRIBUTE = "key";
	private static final String VALUE_ATTRIBUTE = "value";
	
	private static final List<KeySchemaElement> KEY_SCHEMA = Collections.unmodifiableList(
			Arrays.asList( new KeySchemaElement( KEY_ATTRIBUTE, KeyType.HASH ) ) );
	private static final List<AttributeDefinition> ATTRIBUTE_DEFINITIONS = Collections.unmodifiableList( 
			Arrays.asList( new AttributeDefinition( KEY_ATTRIBUTE, ScalarAttributeType.S ) ) );
	
	/**
	 * Default read capacity units provided for a <b>new</b> DynamoDB table created by this database.
	 */
	public static final long DEFAULT_READ_UNITS = 1L;
	/**
	 * Default write capacity units provided for a <b>new</b> DynamoDB table created by this database.
	 */
	public static final long DEFAULT_WRITE_UNITS = 1L;
	private static final ProvisionedThroughput DEFAULT_THROUGHPUT =
			new ProvisionedThroughput( DEFAULT_READ_UNITS, DEFAULT_WRITE_UNITS );
	
	protected AmazonDynamoDB client;
	protected DynamoDB dynamoDB;

	@Override
	public List<Parameter> getLoadParams() {

		return new ArrayList<>( PARAMS );
		
	}

	@Override
	public synchronized boolean load( List<String> args ) throws IllegalStateException, IllegalArgumentException {
		
		if ( args.size() != getLoadParams().size() ) {
			throw new IllegalArgumentException( "Incorrect amount of arguments provided." );
		}
		
		AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
		AWSCredentials credentials = new BasicAWSCredentials( args.get( 1 ), args.get( 2 ) );
		builder.withCredentials( new AWSStaticCredentialsProvider( credentials ) );
		
		if ( args.get( 0 ).equals( "no" ) ) { // Use web service.
			LOG.info( "Starting DynamoDB AWS service, region {}.", args.get( 3 ) );
			builder.withRegion( Regions.valueOf( args.get( 3 ) ) );
		} else { // Use local database.
			LOG.info( "Starting DynamoDB local.", args.get( 3 ) );
			builder.withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration(
					"http://localhost:" + args.get( 1 ), "us-west-2" ) );
		}
		client = builder.build();
		
		try {
			LOG.info( "Checking connection to database." );
			client.listTables( 1 );
		} catch ( AmazonDynamoDBException e ) {
			LOG.error( "Failed to connect to database.", e );
			return false;
		}
		
		dynamoDB = new DynamoDB( client );
		
		loaded = true;
		return true;

	}

	@Override
	public synchronized void close() throws IllegalStateException {
		
		if ( !loaded ) {
			throw new IllegalStateException( "Database not loaded yet." );
		}

		closed = true;

		dynamoDB.shutdown();
		client.shutdown();

	}
	
	/**
	 * Retrieves a table with the given name, creating it if necessary.
	 * 
	 * @param tableName The name of the table.
	 * @return The table.
	 */
	protected Table getTable( String tableName ) {
		
		Table table;
		try {
			LOG.trace( "Attempting to create table of name '{}'.", tableName );
			table = dynamoDB.createTable( tableName, KEY_SCHEMA, ATTRIBUTE_DEFINITIONS,
					DEFAULT_THROUGHPUT );
			LOG.info( "Created table of name '{}'.", tableName );
		} catch ( ResourceInUseException e ) {
			LOG.trace( "Table already exists. Retrieving table of name '{}'.", tableName );
			table = dynamoDB.getTable( tableName );
			LOG.info( "Retrieved table of name '{}'.", tableName );
		}
		return table;
		
	}

	@Override
	protected <K,V> Map<K,V> newMap( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {

		LOG.debug( "Retrieving table of name '{}'.", dataName );
		
		return new TableMap<>( getTable( dataName ), keyTranslator, valueTranslator );
		
	}
	
	/**
	 * Map that is backed by a DynamoDB table.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-29
	 * @param <K> The type of the keys in the map.
	 * @param <V> The type of the values in the map.
	 */
	private class TableMap<K,V> implements Map<K,V> {
		
		private Table table;
		private final Translator<K> keyTranslator;
		private final Translator<V> valueTranslator;
		
		/**
		 * Initializes a map backed by the given table that uses the
		 * given translators.
		 * 
		 * @param backing The table that should back this map.
		 * @param keyTranslator The translator to be used for keys.
		 * @param valueTranslator The translator to be used for values.
		 */
		public TableMap( Table backing, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) {
			
			this.table = backing;
			this.keyTranslator = keyTranslator;
			this.valueTranslator = valueTranslator;
			
		}

		@Override
		public int size() {

			return table.describe().getItemCount().intValue();
			
		}

		@Override
		public boolean isEmpty() {

			return size() == 0;
			
		}
		
		/**
		 * Encodes a key.
		 * Errors encountered during encoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param key The key to encode.
		 * @return The encoded key, or <tt>null</tt> if the given object
		 *         is not of the type supported by the key encoder.
		 * @throws DatabaseException if an error occurred while encoding.
		 */
		private String encodeKey( Object key ) throws DatabaseException {
			
			try {
				return keyTranslator.encodeObj( key );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to encode key.", e );
			}
			
		}
		
		/**
		 * Decodes a key.
		 * Errors encountered during decoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param encoded The string to decode.
		 * @return The decoded key.
		 * @throws DatabaseException if an error occurred while decoding.
		 */
		private K decodeKey( String encoded ) throws DatabaseException {
			
			try {
				return keyTranslator.decode( encoded );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to decode key.", e );
			}
			
		}
		
		/**
		 * Encodes a value.
		 * Errors encountered during encoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param value The value to encode.
		 * @return The encoded value, or <tt>null</tt> if the given object
		 *         is not of the type supported by the value encoder.
		 * @throws DatabaseException if an error occurred while encoding.
		 */
		private String encodeValue( Object value ) throws DatabaseException {
			
			try {
				return valueTranslator.encodeObj( value );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to encode value.", e );
			}
			
		}
		
		/**
		 * Decodes a value.
		 * Errors encountered during decoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param encoded The string to decode.
		 * @return The decoded value.
		 * @throws DatabaseException if an error occurred while decoding.
		 */
		private V decodeValue( String encoded ) throws DatabaseException {
			
			try {
				return valueTranslator.decode( encoded );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to decode value.", e );
			}
			
		}

		@Override
		public boolean containsKey( Object key ) {
			
			return get( key ) != null;
			
		}

		@Override
		public boolean containsValue( Object value ) {
			
			String translated = encodeValue( value );
			if ( translated == null ) {
				return false; // Incorrect type.
			}
			
			ScanSpec scanSpec = new ScanSpec().withProjectionExpression( KEY_ATTRIBUTE ) // Construct scan request.
					                          .withFilterExpression( "#value = :value" )
					                          .withNameMap( new NameMap().with( "#value", VALUE_ATTRIBUTE ) )
					                          .withValueMap( new ValueMap().withString( ":value", translated ) );
			
			try {
	            ItemCollection<ScanOutcome> items = table.scan( scanSpec ); // Run scan.
	            return items.getAccumulatedItemCount() != 0; // Check if at least one match.
	        } catch ( Exception e ) {
	        	LOG.warn( "Failed to scan for value '" + translated + "'.", e );
				return false;
	        }
			
		}

		@Override
		public V get( Object key ) {
			
			String translated = encodeKey( key );
			if ( translated == null ) {
				return null; // Incorrect type.
			}
			
			Item result;
			try {
				result = table.getItem( KEY_ATTRIBUTE, translated );
			} catch ( Exception e ) {
				LOG.warn( "Failed to retrieve item of key '" + translated + "'.", e );
				return null;
			}
			
			String value = result.getString( VALUE_ATTRIBUTE );
			return value == null ? null : decodeValue( value );
			
		}

		@Override
		public V put( K key, V value ) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public V remove( Object key ) {
			
			String translated = encodeKey( key );
			if ( translated == null ) {
				return null; // Incorrect type.
			}
			
			AttributeValue result = table.deleteItem( 
					new DeleteItemSpec().withPrimaryKey( KEY_ATTRIBUTE, translated )
					                    .withReturnValues( ReturnValue.ALL_OLD ) )
					.getDeleteItemResult().getAttributes().get( VALUE_ATTRIBUTE );
			
			return result == null ? null : decodeValue( result.getS() );
			
		}

		@Override
		public void putAll( Map<? extends K,? extends V> m ) {

			for ( Map.Entry<? extends K,? extends V> entry : m.entrySet() ) {
				
				put( entry.getKey(), entry.getValue() );
				
			}
			
		}

		@Override
		public void clear() {

			String tableName = table.getTableName();
			
			LOG.debug( "Deleting table '{}'.", tableName );
			table.delete(); // Delete table.
			try {
				table.waitForDelete();
			} catch ( InterruptedException e ) {
				LOG.warn( "Interrupted while waiting for table deletion.", e );
			}
			LOG.info( "Table cleared." );
			
			table = getTable( tableName ); // Recreate table.
			
		}

		@Override
		public Set<K> keySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<V> values() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
