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
import java.util.List;
import java.util.Map;

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
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.github.thiagotgm.blakebot.common.storage.Translator;

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
	
	protected AmazonDynamoDB client;
	protected DynamoDB dynamoDB;

	@Override
	public List<Parameter> getLoadParams() {

		return new ArrayList<>( PARAMS );
		
	}

	@Override
	public boolean load( List<String> args ) throws IllegalStateException, IllegalArgumentException {
		
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
	public void close() throws IllegalStateException {
		
		if ( !loaded ) {
			throw new IllegalStateException( "Database not loaded yet." );
		}

		closed = true;

		dynamoDB.shutdown();
		client.shutdown();

	}

	@Override
	protected <K,V> Map<K,V> newMap( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

}
