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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;
import com.github.thiagotgm.blakebot.common.utils.Utils;

/**
 * Database that uses a DynamoDB backend, either locally or using the
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
	
	/**
	 * Attribute that stores the key of a database entry (hash key).
	 */
	protected static final String KEY_ATTRIBUTE = "key";
	/**
	 * Attribute that stores the value of a database entry.
	 */
	protected static final String VALUE_ATTRIBUTE = "value";
	/**
	 * Attribute that is (expected to be) not present in a database
	 * item. Used in projection expressions in get or scan operations
	 * in order to prevent actual item data from being loaded,
	 * when only existence checking is needed.
	 */
	protected static final String INVALID_ATTRIBUTE = "invalid";
	
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
		if ( args.get( 0 ).equals( "no" ) ) { // Use web service.
			LOG.info( "Starting DynamoDB AWS service, region {}.", args.get( 3 ) );
			AWSCredentials credentials = new BasicAWSCredentials( args.get( 1 ), args.get( 2 ) );
			builder.withRegion( Regions.valueOf( args.get( 3 ) ) )
			       .withCredentials( new AWSStaticCredentialsProvider( credentials ) );
		} else { // Use local database.
			LOG.info( "Starting DynamoDB local.", args.get( 3 ) );
			builder.withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration(
					"http://localhost:" + args.get( 1 ), "local" ) );
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
		
		LOG.info( "Closing DynamoDB." );

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
			table.waitForActive(); // Wait until table is active.
			LOG.info( "Created table of name '{}'.", tableName );
		} catch ( ResourceInUseException e ) {
			LOG.trace( "Table already exists. Retrieving table of name '{}'.", tableName );
			table = dynamoDB.getTable( tableName );
			LOG.info( "Retrieved table of name '{}'.", tableName );
		} catch ( InterruptedException e ) {
			throw new DatabaseException( "Interrupted while creating table.", e );
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
			
			// Construct scan request.
			ScanSpec scanSpec = new ScanSpec().withProjectionExpression( INVALID_ATTRIBUTE );

			int count = 0;
			try {
				ItemCollection<ScanOutcome> items = table.scan( scanSpec ); // Run scan.
				Iterator<Item> iter = items.iterator();
				while ( iter.hasNext() ) {
					iter.next();
					count++;
				}
			} catch ( Exception e ) {
				LOG.warn( "Failed to scan database.", e );
				return 0;
			}

			return count;
			
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
				return Utils.sanitize( keyTranslator.encodeObj( key ) );
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
				return keyTranslator.decode( Utils.desanitize( encoded ) );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to decode key.", e );
			}
			
		}
		
		/**
		 * Extracts a Data value into an object that is able to be used as a
		 * value in DynamoDB.
		 * 
		 * @param data The data to extract.
		 * @return The DynamoDB-compatible object.
		 */
		private Object extractData( Data data ) {
			
			switch ( data.getType() ) {
			
			case STRING:
				return Utils.sanitize( data.getString() );
				
			case NUMBER:
				if ( data.isFloat() ) {
					return data.getNumberFloat();
				} else {
					return data.getNumberInteger();
				}
				
			case BOOLEAN:
				return data.getBoolean();
				
			default:
			case NULL:
				return null;
				
			case LIST:
				List<Object> list = new ArrayList<>( data.getList().size() );
				for ( Data elem : data.getList() ) { // Extract each element.
					
					list.add( extractData( elem ) );
					
				}
				return list;
				
			case MAP:
				Map<String,Object> map = new HashMap<>( data.getMap().size() );
				for ( Map.Entry<String,Data> entry : data.getMap().entrySet() ) {
					// Extract each mapping.
					map.put( entry.getKey(), extractData( entry.getValue() ) );
					
				}
				return map;
				
			}
		
		}
		
		/**
		 * Encodes a value into an object that can be stored into DynamoDB.
		 * Errors encountered during encoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param value The value to encode.
		 * @return The encoded value.
		 * @throws DatabaseException if an error occurred while encoding.
		 */
		private Object encodeValue( V value ) throws DatabaseException {
			
			try {
				return extractData( valueTranslator.toData( value ) );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to encode value.", e );
			}
			
		}
		
		/**
		 * Stores data received from DynamoDB into a Data instance.
		 * <p>
		 * Inverse of {@link #extractData(Data)}.
		 * 
		 * @param obj The DynamoDB-supported object.
		 * @return The Data representation.
		 */
		private Data storeData( Object obj ) {
			
			if ( obj instanceof String ) {
				return Data.stringData( Utils.desanitize( (String) obj ) );
			} else if ( obj instanceof Number ) {
				return Data.numberData( obj.toString() );
			} else if ( obj instanceof Boolean ) {
				return Data.booleanData( (Boolean) obj );
			} else if ( obj == null ) {
				return Data.nullData();
			} else if ( obj instanceof List ) {
				List<Data> list = new LinkedList<>();
				for ( Object elem : (List<?>) obj ) { // Store each element.
					
					list.add( storeData( elem ) );
					
				}
				return Data.listData( list );
			} else if ( obj instanceof Map ) {
				Map<String,Data> map = new HashMap<>();
				@SuppressWarnings("unchecked")
				Map<String,?> extracted = (Map<String,?>) obj;
				for ( Map.Entry<String,?> entry : extracted.entrySet() ) {
					// Store each mapping.
					map.put( entry.getKey(), storeData( entry.getValue() ) );
					
				}
				return Data.mapData( map );
			} else {
				throw new DatabaseException( "Unsupported data type received from database." );
			}
			
		}
		
		/**
		 * Decodes a value.
		 * Errors encountered during decoding are automatically
		 * converted to a DatabaseException.
		 * 
		 * @param encoded The data to decode.
		 * @return The decoded value.
		 * @throws DatabaseException if an error occurred while decoding.
		 */
		private V decodeValue( Object encoded ) throws DatabaseException {
			
			try {
				return valueTranslator.fromData( storeData( encoded ) );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to decode value.", e );
			}
			
		}
		
		/**
		 * Attempts to retrieve the Item that has the given key.
		 *
		 * @param key The key.
		 * @param loadData Whether the item data (attributes) should be loaded, if it
		 *                 is found. If this is <tt>false</tt>, then if the returned item
		 *                 (if any) will not contain any attributes, and is only good for
		 *                 checking if there exists an item with the given key.
		 * @return The item under the given key, or <tt>null</tt> if there is
		 *         no such item.
		 */
		protected Item getItem( Object key, boolean loadData ) {
			
			String translated = encodeKey( key );
			if ( translated == null ) {
				return null; // Incorrect type.
			}
		
			GetItemSpec getSpec = new GetItemSpec().withPrimaryKey( KEY_ATTRIBUTE, translated );
			if ( !loadData ) { // Should not load data.
				getSpec.withProjectionExpression( INVALID_ATTRIBUTE );
			}
			
			try {
				return table.getItem( getSpec );
			} catch ( Exception e ) {
				LOG.warn( "Failed to retrieve item of key '" + translated + "'.", e );
				return null;
			}
			
		}

		@Override
		public boolean containsKey( Object key ) {
			
			return getItem( key, false ) != null;
			
		}

		@Override
		public boolean containsValue( Object value ) {
			
			Data data = null;
			try {
				data = valueTranslator.objToData( value );
			} catch ( TranslationException e ) {
				throw new DatabaseException( "Failed to translate value." );
			}
			if ( data == null ) {
				return false; // Incorrect type.
			}
			
			Object translated = extractData( data );
			
			// Construct scan request.
			ScanSpec scanSpec = new ScanSpec().withProjectionExpression( INVALID_ATTRIBUTE )
					                          .withFilterExpression( "#value = :value" )
					                          .withNameMap( new NameMap().with( "#value", VALUE_ATTRIBUTE ) )
					                          .withValueMap( new ValueMap().with( ":value", translated ) );
			
			try {
	            ItemCollection<ScanOutcome> items = table.scan( scanSpec ); // Run scan.
	            return items.iterator().hasNext(); // Return if there is at least one match.
	        } catch ( Exception e ) {
	        	LOG.warn( "Failed to scan for value '" + translated + "'.", e );
				return false;
	        }
			
		}

		@Override
		public V get( Object key ) {
			
			Item result = getItem( key, true );
			
			if ( result == null ) {
				return null; // No item found.
			}
			
			if ( !result.hasAttribute( VALUE_ATTRIBUTE ) ) { // Missing value attribute.
				throw new DatabaseException( "Item is missing value attribute." );
			}
			
			return decodeValue( result.get( VALUE_ATTRIBUTE ) );
			
		}

		@Override
		public V put( K key, V value ) {
			
			String translatedKey = encodeKey( key );
			Object translatedValue = encodeValue( value );
			
			if ( translatedKey == null ) {
				throw new DatabaseException( "Failed to translate key." );
			}

			UpdateItemSpec updateItemSpec = new UpdateItemSpec()
					.withPrimaryKey( KEY_ATTRIBUTE, translatedKey )
					.withUpdateExpression( "set #value = :val" )
					.withNameMap( new NameMap().with( "#value", VALUE_ATTRIBUTE ) )
		            .withValueMap( new ValueMap().with(":val", translatedValue ) )
		            .withReturnValues( ReturnValue.UPDATED_OLD );
			
			Map<String,AttributeValue> result = table.updateItem( updateItemSpec ).getUpdateItemResult()
					                                 .getAttributes();
			
			if ( result == null ) {
				return null; // No old value.
			}
			
			if ( !result.containsKey( VALUE_ATTRIBUTE ) ) { // Missing value attribute.
				throw new DatabaseException( "Item was missing value attribute." );
			}
			
			return decodeValue( ItemUtils.toSimpleValue( result.get( VALUE_ATTRIBUTE ) ) );
			
		}

		@Override
		public V remove( Object key ) {
			
			String translated = encodeKey( key );
			if ( translated == null ) {
				return null; // Incorrect type.
			}
			
			Map<String,AttributeValue> result = table.deleteItem( 
					new DeleteItemSpec().withPrimaryKey( KEY_ATTRIBUTE, translated )
					                    .withReturnValues( ReturnValue.ALL_OLD ) )
					.getDeleteItemResult().getAttributes();
			
			if ( result == null ) {
				return null; // No old value.
			}
			
			if ( !result.containsKey( VALUE_ATTRIBUTE ) ) { // Missing value attribute.
				throw new DatabaseException( "Item was missing value attribute." );
			}
			
			return decodeValue( ItemUtils.toSimpleValue( result.get( VALUE_ATTRIBUTE ) ) );
			
		}

		@Override
		public void putAll( Map<? extends K,? extends V> m ) {

			// Put each entry.
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
		public boolean equals( Object o ) {
			
			return ( o instanceof Map ) && entrySet().equals( ( (Map<?,?>) o ).entrySet() );
			
		}
		
		@Override
		public int hashCode() {
			
			int sum = 0;
			
			for ( Map.Entry<K,V> entry : entrySet() ) {
				
				sum += entry.hashCode();
				
			}
			
			return sum;
			
		}

		@Override
		public Set<K> keySet() {

			final Map<K,V> thisMap = this;
			return new Set<K>() {

				@Override
				public int size() {

					return thisMap.size();
					
				}

				@Override
				public boolean isEmpty() {
					
					return thisMap.isEmpty();
							
				}

				@Override
				public boolean contains( Object o ) {

					return thisMap.containsKey( o );
					
				}

				@Override
				public Iterator<K> iterator() {

					// Construct scan request.
					ScanSpec scanSpec = new ScanSpec().withProjectionExpression( "#k" )
							                          .withNameMap( new NameMap().with( "#k", KEY_ATTRIBUTE ) );
					
					Iterator<Item> itemIter;
					try {
			            ItemCollection<ScanOutcome> items = table.scan( scanSpec ); // Run scan.
			            itemIter = items.iterator(); // Get iterator.
			        } catch ( Exception e ) {
			        	throw new DatabaseException( "Failed to scan table.", e );
			        }
					
					final Iterator<Item> backing = itemIter;
					return new Iterator<K>() {
						
						private K lastKey = null;

						@Override
						public boolean hasNext() {

							return backing.hasNext();
							
						}

						@Override
						public K next() {

							Item next = backing.next();
							if ( !next.hasAttribute( KEY_ATTRIBUTE ) ) {
								throw new DatabaseException( "Missing key attribute." );
							}
							
							K key = decodeKey( next.getString( KEY_ATTRIBUTE ) );
							
							lastKey = key; // Store last key.
							return key;
							
						}
						
						@Override
						public void remove() {
							
							if ( lastKey == null ) {
								throw new IllegalStateException( "Must have called next() beforehand." );
							}
							
							thisMap.remove( lastKey );
							lastKey = null; // Last key was removed.
							
						}
						
					};
					
				}

				@Override
				public Object[] toArray() {

					return toArray( new Object[0] );
					
				}

				@Override
				public <T> T[] toArray( T[] a ) {
					
					List<K> elems = new LinkedList<>(); 
					for ( K elem : this ) { // Get all elements.
						
						elems.add( elem );
						
					}
					return elems.toArray( a ); // Put into array.
					
				}

				@Override
				public boolean add( K e ) {

					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean remove( Object o ) {

					return thisMap.remove( o ) != null;
					
				}

				@Override
				public boolean containsAll( Collection<?> c ) {

					for ( Object elem : c ) {
						
						if ( !contains( elem ) ) {
							return false; // Found not contained.
						}
						
					}
					return true;
					
				}

				@Override
				public boolean addAll( Collection<? extends K> c ) {
					
					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean retainAll( Collection<?> c ) {
					
					boolean changed = false;
					
					for ( Iterator<K> iter = iterator(); iter.hasNext(); ) {
						
						if ( !c.contains( iter.next() ) ) {
							iter.remove();
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public boolean removeAll( Collection<?> c ) {
					
					boolean changed = false;
					
					for ( Object elem : c ) {
						
						if ( remove( elem ) ) {
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public void clear() {

					thisMap.clear();
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					if ( !( o instanceof Set ) ) {
						return false;
					}
					
					Set<?> other = (Set<?>) o;
					
					return ( size() == other.size() ) && containsAll( other );
					
				}
				
				@Override
				public int hashCode() {
					
					int sum = 0;
					
					for ( K elem : this ) {
						
						sum += elem == null ? 0 : elem.hashCode();
						
					}
					
					return sum;
					
				}
				
			};
			
		}

		@Override
		public Collection<V> values() {

			final Map<K,V> thisMap = this;
			return new Collection<V>() {

				@Override
				public int size() {

					return thisMap.size();
					
				}

				@Override
				public boolean isEmpty() {
					
					return thisMap.isEmpty();
							
				}

				@Override
				public boolean contains( Object o ) {

					return thisMap.containsValue( o );
					
				}

				@Override
				public Iterator<V> iterator() {

					final Iterator<Map.Entry<K,V>> backing = thisMap.entrySet().iterator();
					return new Iterator<V>() {

						@Override
						public boolean hasNext() {

							return backing.hasNext();
							
						}

						@Override
						public V next() {

							return backing.next().getValue();
							
						}

						@Override
						public void remove() {
							
							backing.remove(); // Delegate.
							
						}
						
					};
					
				}

				@Override
				public Object[] toArray() {
					
					return toArray( new Object[0] );
					
				}

				@Override
				public <T> T[] toArray( T[] a ) {

					List<V> elems = new LinkedList<>(); 
					for ( V elem : this ) { // Get all elements.
						
						elems.add( elem );
						
					}
					return elems.toArray( a ); // Put into array.
					
				}

				@Override
				public boolean add( V e ) {

					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean remove( Object o ) {

					for ( Iterator<V> iter = iterator(); iter.hasNext(); ) {
						// Search through values.
						V next = iter.next();
						if ( o == null ? next == null : o.equals( next ) ) {
							iter.remove();
							return true; // Found object.
						}
						
					}
					return false; // Didn't find object.
					
				}

				@Override
				public boolean containsAll( Collection<?> c ) {

					for ( Object elem : c ) {
						
						if ( !contains( elem ) ) {
							return false; // Found not contained.
						}
						
					}
					return true;
					
				}

				@Override
				public boolean addAll( Collection<? extends V> c ) {
					
					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean retainAll( Collection<?> c ) {

					boolean changed = false;
					
					for ( Iterator<V> iter = iterator(); iter.hasNext(); ) {
						
						if ( !c.contains( iter.next() ) ) {
							iter.remove();
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public boolean removeAll( Collection<?> c ) {

					boolean changed = false;
					
					for ( Iterator<V> iter = iterator(); iter.hasNext(); ) {
						
						if ( c.contains( iter.next() ) ) {
							iter.remove(); // Remove matching element.
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public void clear() {

					thisMap.clear();
					
				}
				
			};
			
		}

		@Override
		public Set<Entry<K,V>> entrySet() {

			final TableMap<K,V> thisMap = this;
			return new Set<Entry<K,V>>() {

				@Override
				public int size() {

					return thisMap.size();
					
				}

				@Override
				public boolean isEmpty() {
					
					return thisMap.isEmpty();
							
				}

				@Override
				public boolean contains( Object o ) {

					if ( !( o instanceof Map.Entry ) ) {
						return false; // Wrong type.
					}
					
					Map.Entry<?,?> other = (Map.Entry<?,?>) o;
					
					Item item = thisMap.getItem( other.getKey(), true );
					if ( item == null ) {
						return false;
					}
					
					if ( !item.hasAttribute( VALUE_ATTRIBUTE ) ) {
						throw new DatabaseException( "Missing value attribute." );
					}
					
					V value = decodeValue( item.get( VALUE_ATTRIBUTE ) );
					return value == null ? other.getValue() == null : value.equals( other.getValue() );
					
				}

				@Override
				public Iterator<Entry<K,V>> iterator() {

					// Construct scan request.
					ScanSpec scanSpec = new ScanSpec().withProjectionExpression( "#k,#val" )
							                          .withNameMap( new NameMap().with( "#k", KEY_ATTRIBUTE )
							                        		  .with( "#val", VALUE_ATTRIBUTE ) );
					
					Iterator<Item> itemIter;
					try {
			            ItemCollection<ScanOutcome> items = table.scan( scanSpec ); // Run scan.
			            itemIter = items.iterator(); // Get iterator.
			        } catch ( Exception e ) {
			        	throw new DatabaseException( "Failed to scan table.", e );
			        }
					
					final Iterator<Item> backing = itemIter;
					return new Iterator<Entry<K,V>>() {
						
						private K lastKey = null;

						@Override
						public boolean hasNext() {

							return backing.hasNext();
							
						}

						@Override
						public Entry<K,V> next() {

							Item next = backing.next();
							if ( !next.hasAttribute( KEY_ATTRIBUTE ) ) {
								throw new DatabaseException( "Missing key attribute." );
							}
							if ( !next.hasAttribute( VALUE_ATTRIBUTE ) ) {
								throw new DatabaseException( "Missing value attribute." );
							}
							
							final K key = decodeKey( next.getString( KEY_ATTRIBUTE ) );
							final V value = decodeValue( next.get( VALUE_ATTRIBUTE ) );
							
							lastKey = key; // Store last key.
							return new Map.Entry<K,V>() {
								
								private V entryValue = value; // Current value.

								@Override
								public K getKey() {

									return key;
									
								}

								@Override
								public V getValue() {

									return entryValue;
									
								}

								@Override
								public V setValue( V value ) {

									thisMap.put( key, value ); // Set value.
									
									V previousValue = entryValue;
									entryValue = value; // Update stored value.
									
									return previousValue;
									
								}
							
								@Override
								public boolean equals( Object o ) {
									
									if ( !( o instanceof Map.Entry ) ) {
										return false; // Wrong type.
									}
									
									Map.Entry<?,?> other = (Map.Entry<?,?>) o;
									
									K key = getKey();
									V value = getValue();
									
									return ( key == null ? other.getKey() == null :
										                   key.equals( other.getKey() ) ) &&
										   ( value == null ? other.getValue() == null :
											                 value.equals( other.getValue() ) );
									
								}
								
								@Override
								public int hashCode() {
									
									K key = getKey();
									V value = getValue();
									
									return ( key == null ? 0 : key.hashCode() ) ^
								         ( value == null ? 0 : value.hashCode() );

									
								}
								
							};
							
						}
						
						@Override
						public void remove() {
							
							if ( lastKey == null ) {
								throw new IllegalStateException( "Must have called next() beforehand." );
							}
							
							thisMap.remove( lastKey );
							lastKey = null; // Last key was removed.
							
						}
						
					};
					
				}

				@Override
				public Object[] toArray() {
					
					return toArray( new Object[0] );
					
				}

				@Override
				public <T> T[] toArray( T[] a ) {

					List<Map.Entry<K,V>> elems = new LinkedList<>(); 
					for ( Map.Entry<K,V> elem : this ) { // Get all elements.
						
						elems.add( elem );
						
					}
					return elems.toArray( a ); // Put into array.
					
				}

				@Override
				public boolean add( Entry<K,V> e ) {

					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean remove( Object o ) {
					
					if ( !( o instanceof Map.Entry ) ) {
						return false; // Not an entry.
					}
					
					Map.Entry<?,?> other = (Map.Entry<?,?>) o;
					
					final String key = encodeKey( other.getKey() ); // Translate key.
					if ( key == null ) {
						return false; // Wrong entry key type.
					}
					
					Data data = null;
					try {
						data = valueTranslator.objToData( other.getValue() );
					} catch ( TranslationException e ) {
						throw new DatabaseException( "Failed to translate value." );
					}
					if ( data == null ) {
						return false; // Wrong entry value type.
					}
					final Object value = extractData( data );
					
					DeleteItemSpec deleteSpec = new DeleteItemSpec().withPrimaryKey( KEY_ATTRIBUTE, key )
							.withConditionExpression( "#val = :val" )
							.withNameMap( new NameMap().with( "#val", VALUE_ATTRIBUTE ) )
							.withValueMap( new ValueMap().with( ":val", value ) )
							.withReturnValues( ReturnValue.ALL_OLD );
					
					try {
						return table.deleteItem( deleteSpec ).getDeleteItemResult()
								.getAttributes() != null; // Check if found item and deleted.
					} catch ( ConditionalCheckFailedException e ) {
						return false; // Value didn't match.
					}

				}

				@Override
				public boolean containsAll( Collection<?> c ) {

					for ( Object elem : c ) {
						
						if ( !contains( elem ) ) {
							return false; // Found not contained.
						}
						
					}
					return true;
					
				}

				@Override
				public boolean addAll( Collection<? extends Entry<K,V>> c ) {
					
					throw new UnsupportedOperationException();
					
				}

				@Override
				public boolean retainAll( Collection<?> c ) {
					
					boolean changed = false;
					
					for ( Iterator<Map.Entry<K,V>> iter = iterator(); iter.hasNext(); ) {
						
						if ( !c.contains( iter.next() ) ) {
							iter.remove();
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public boolean removeAll( Collection<?> c ) {

					boolean changed = false;
					
					for ( Object elem : c ) {
						
						if ( remove( elem ) ) {
							changed = true;
						}
						
					}
					
					return changed;
					
				}

				@Override
				public void clear() {

					thisMap.clear();
					
				}
				
				@Override
				public boolean equals( Object o ) {
					
					if ( !( o instanceof Set ) ) {
						return false;
					}
					
					Set<?> other = (Set<?>) o;
					
					return ( size() == other.size() ) && containsAll( other );
					
				}
				
				@Override
				public int hashCode() {
					
					int sum = 0;
					
					for ( Entry<K,V> elem : this ) {
						
						sum += elem == null ? 0 : elem.hashCode();
						
					}
					
					return sum;
					
				}
				
			};
			
		}
		
	}

}
