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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.SaveManager;
import com.github.thiagotgm.blakebot.common.SaveManager.Saveable;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.xml.XMLElement;
import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;
import com.github.thiagotgm.blakebot.common.storage.xml.XMLTreeGraph;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLMap;
import com.github.thiagotgm.blakebot.common.storage.xml.translate.XMLString;
import com.github.thiagotgm.blakebot.common.utils.Tree;
import com.github.thiagotgm.blakebot.common.utils.Utils;

/**
 * Database that saves data in local XML files.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-16
 */
public class XMLDatabase extends AbstractDatabase implements Saveable {
	
	@SuppressWarnings("rawtypes")
	private static final Class<? extends Map> MAP_CLASS = HashMap.class;
	
	private static final List<String> loadParams = Collections.unmodifiableList(
			Arrays.asList( new String[]{ "Directory path" } ) );
	private static final Logger LOG = LoggerFactory.getLogger( XMLDatabase.class );
	
	/**
	 * Path were database files are stored.
	 */
	private Path path;
	/**
	 * Trees managed by this database.
	 */
	private final Collection<XMLEntry> storage = new LinkedList<>();
	
	@Override
	public List<String> getLoadParams() {

		return new ArrayList<>( loadParams );
		
	}

	@Override
	public synchronized boolean load( List<String> params ) throws IllegalStateException, IllegalArgumentException {

		if ( loaded ) {
			throw new IllegalStateException( "Database is already loaded." );
		}
		
		if ( params.size() != loadParams.size() ) {
			throw new IllegalStateException( "Parameter list size does not match expectation." );
		}
		
		LOG.info( "Loading database." );
		
		path = Paths.get( params.get( 0 ) );
		
		LOG.debug( "Requested path: {}", path );
		
		if ( path.toFile().exists() ) { // Path already exists.
            if ( !path.toFile().isDirectory() ) { // Check if path is directory.
                LOG.error( "Database path is not a directory." );
                return false;
            }
        } else { // Create path.
            try {
                Files.createDirectories( path );
            } catch ( IOException e ) {
                LOG.error( "Could not create database directory.", e );
                return false;
            }
        }
		
		LOG.info( "Database path: {}", path );
		
		loaded = true;
		
		SaveManager.registerListener( this ); // Register for autosave events.
		
		return true;
		
	}

	@Override
	public synchronized void close() throws IllegalStateException {
		
		LOG.info( "Closing database." );
		
		SaveManager.unregisterListener( this ); // Unregister for autosave events.

		save(); // Save state.
		
		closed = true;
		
	}
	
	private <T> XMLTranslator<T> getXMLTranslator( Translator<T> translator ) {
		
		if ( translator instanceof com.github.thiagotgm.blakebot.common.storage.translate.XMLTranslator ) {
			return ( (com.github.thiagotgm.blakebot.common.storage.translate.XMLTranslator<T>) translator )
					.getXMLTranslator();
		} else {
			return new CompoundTranslator<>( translator );
		}
		
	}
	
	/**
	 * Loads data stored under the given name using the given XML element.
	 * 
	 * @param dataName The name the data is registered under.
	 * @param element The element to be load data into.
	 * @return The entry that represents the loaded data.
	 * @throws DatabaseException if an error occurred while loading.
	 */
	private XMLEntry load( String dataName, XMLElement element ) throws DatabaseException {
		
		String filename = dataName + ".xml";
		LOG.debug( "Loading file {}.", filename );
		File file = path.resolve( filename ).toFile();
		if ( file.exists() ) {
			FileInputStream in;
			try {
				in = new FileInputStream( file );
				Utils.readXMLDocument( in, element );
			} catch ( FileNotFoundException | XMLStreamException e ) {
				throw new DatabaseException( "Could not read data file.", e );
			}
		}
		return new XMLEntry( dataName, element );
		
	}

	@Override
	protected synchronized <K,V> Tree<K,V> newTree( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {

		// Get XML translators.
		XMLTranslator<K> keyXMLTranslator = getXMLTranslator( keyTranslator );
		XMLTranslator<V> valueXMLTranslator = getXMLTranslator( valueTranslator );
		
		// Instantiate tree.
		XMLTreeGraph<K,V> tree = new XMLTreeGraph<>( keyXMLTranslator, valueXMLTranslator );
		
		// Load and register tree.
		storage.add( load( dataName, tree ) );
		
		return tree;
		
	}

	@Override
	protected synchronized <K,V> Map<K, V> newMap( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {
		
		// Get XML translators.
		XMLTranslator<K> keyXMLTranslator = getXMLTranslator( keyTranslator );
		XMLTranslator<V> valueXMLTranslator = getXMLTranslator( valueTranslator );
		
		@SuppressWarnings("unchecked") // Instantiate map.
		Class<? extends Map<K,V>> mapClass = (Class<? extends Map<K,V>>) MAP_CLASS;
		XMLMap.WrappedMap<K,V> map = new XMLMap.WrappedMap<>( mapClass, keyXMLTranslator, valueXMLTranslator );
		
		// Load and register map.
		storage.add( load( dataName, map ) );
		
		return map;
		
	}
	
	@Override
	public synchronized void save() {
		
		if ( closed ) {
			return; // Already closed, abort.
		}
		
		LOG.info( "Saving database files." );
		
		for ( XMLEntry data : storage ) { // Save each storage element.
			
			String filename = data.getName() + ".xml";
			LOG.debug( "Saving file {}.", filename );
			File file = path.resolve( filename ).toFile();
			try {
				FileOutputStream out = new FileOutputStream( file );
				Utils.writeXMLDocument( out, data.getElement() );
				out.close();
			} catch ( FileNotFoundException e1 ) {
				LOG.error( "Could not open database file " + file.toString() + ".", e1 );
			} catch ( XMLStreamException e2 ) {
				LOG.error( "Could not save database file " + file.toString() + ".", e2 );
			} catch ( IOException e3 ) {
				LOG.error( "Could not close database file " + file.toString() + ".", e3 );
			}
			
		}
		
	}
	
	/* Full translator for XML */
	
	/**
	 * Compound translator that cascade a T-String translator with a
	 * String-XML translator to translate to XML format objects that
	 * do not have an XML translator, but have a String translator.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-07-27
	 * @param <T> The type of object that is to be translated.
	 */
	public class CompoundTranslator<T> implements XMLTranslator<T> {
		
		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = 7676473726689961837L;
		
		private final Translator<T> dataTranslator;
		private final XMLString stringTranslator;
		
		/**
		 * Initializes a cascaded translator that uses the given translator
		 * and a {@link XMLString String-XML translator}.
		 * 
		 * @param dataTranslator The translator to encode objects with.
		 */
		public CompoundTranslator( Translator<T> dataTranslator ) {
			
			this.dataTranslator = dataTranslator;
			this.stringTranslator = new XMLString();
			
		}

		@Override
		public T read( XMLStreamReader in ) throws XMLStreamException {

			try {
				return dataTranslator.decode( stringTranslator.read( in ) );
			} catch ( IOException e ) {
				throw new XMLStreamException( "Could not translate data.", e );
			}
			
		}

		@Override
		public void write( XMLStreamWriter out, T instance ) throws XMLStreamException {

			try {
				stringTranslator.write( out, dataTranslator.encode( instance ) );
			} catch ( IOException e ) {
				throw new XMLStreamException( "Could not translate data.", e );
			}
			
		}
		
	}
	
	/**
	 * Database entry for XML storage elements.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-08
	 */
	private class XMLEntry {
		
		private final String name;
		private final XMLElement element;
		
		/**
		 * Initializes an XML entry for the given storage element, under the given name.
		 * 
		 * @param name The name that the storage element is registered under.
		 * @param element The storage element.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public XMLEntry( String name, XMLElement element ) throws NullPointerException {
			
			if ( ( name == null ) || ( element == null ) ) {
				throw new NullPointerException( "Arguments cannot be null." );
			}
			
			this.name = name;
			this.element = element;
			
		}
		
		/**
		 * Retrieves the name of this storage element.
		 * 
		 * @return The name.
		 */
		public String getName() {
			
			return name;
			
		}
		
		/**
		 * Retrieves the storage element this represents.
		 * 
		 * @return The element.
		 */
		public XMLElement getElement() {
			
			return element;
			
		}
		
	}
	
}
