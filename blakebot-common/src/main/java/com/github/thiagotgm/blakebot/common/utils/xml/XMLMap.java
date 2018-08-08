/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * BlakeBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.common.utils.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.XMLElement;
import com.github.thiagotgm.blakebot.common.utils.XMLTranslator;

/**
 * Translator that stores and loads maps to/from an XML format.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-08
 * @param <K> The type of keys in the map.
 * @param <V> The type of the values to be stored.
 */
public class XMLMap<K,V> implements XMLTranslator<Map<K,V>> {
	
	/**
	 * UID that represents this class.
	 */
	private static final long serialVersionUID = -903749802183805231L;

	/**
     * Local name of the XML element.
     */
    public static final String TAG = "map";
	
    private final Constructor<? extends Map<K,V>> mapCtor;
	private final XMLTranslator<Map.Entry<K,V>> entryTranslator;
	
	/**
     * Instantiates an map translator that uses instances of the given map class and
     * uses the given translators for keys and values.
     *
     * @param mapClass The class of map to instantiate.
     * @param keyTranslator The translator to use for the map keys.
     * @param valueTranslator The translator to use for the map values.
     * @throws IllegalArgumentException if the given class does not have an accessible no-arg constructor.
     * @throws NullPointerException if any of the arguments is <tt>null</tt>.
     */
	public XMLMap( Class<? extends Map<K,V>> mapClass, XMLTranslator<K> keyTranslator,
			XMLTranslator<V> valueTranslator ) throws IllegalArgumentException, NullPointerException {
		
		if ( ( mapClass == null ) || ( keyTranslator == null ) || ( valueTranslator == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		try { // Get collection ctor.
			mapCtor = mapClass.getConstructor();
		} catch ( NoSuchMethodException | SecurityException e ) {
			throw new IllegalArgumentException( "Map class does not have a public no-args constructor.", e );
		}
        
        try { // Check that ctor works.
			mapCtor.newInstance();
		} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e ) {
			throw new IllegalArgumentException(
					"Map class cannot be initialized using no-arg constructor.", e );
		}
		
		entryTranslator = new XMLMapEntry( keyTranslator, valueTranslator );
		
	}
	
	/**
	 * Reads a map from an XML stream, placing read mappings into the given map instance.
	 * 
	 * @param in The stream to read from.
	 * @param map The instance to place mappings into.
	 * @throws XMLStreamException if an error occurred.
	 */
	private void read( XMLStreamReader in, Map<K,V> map ) throws XMLStreamException {
		
		if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
	              !in.getLocalName().equals( TAG ) ) {
			throw new XMLStreamException( "Did not find element start." );
	    }
		while ( in.hasNext() ) { // Read each mapping.
            
            switch ( in.next() ) {
                
                case XMLStreamConstants.START_ELEMENT:
                	if ( in.getLocalName().equals( XMLMapEntry.TAG ) ) { // Read mapping.
                		Map.Entry<K,V> entry = entryTranslator.read( in );
                		map.put( entry.getKey(), entry.getValue() ); // Insert mapping.
                	} else {
                		throw new XMLStreamException( "Unexpected start element." );
                	}
                	break;
                    
                case XMLStreamConstants.END_ELEMENT:
                	if ( in.getLocalName().equals( TAG ) ) {
                		return; // Finished reading.
                	} else {
                		throw new XMLStreamException( "Unexpected end element." );
                	}
	                
            }
            
        }
        throw new XMLStreamException( "Unexpected end of document." );
		
	}

	@Override
	public Map<K,V> read( XMLStreamReader in ) throws XMLStreamException {
		
		Map<K,V> map; // Make map instance.
		try {
			map = mapCtor.newInstance();
		} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e ) {
			throw new XMLStreamException( "Could not create new map instance.", e );
		}
		
		read( in, map ); // Read map.
		
		return map;

	}

	@Override
	public void write( XMLStreamWriter out, Map<K,V> instance ) throws XMLStreamException {

		out.writeStartElement( TAG );
		
		for ( Map.Entry<K,V> entry : instance.entrySet() ) {
			
			entryTranslator.write( out, entry ); // Write each mapping.
			
		}
		
		out.writeEndElement();
		
	}
	
	/* Delegate for writing mappings */
	
	/**
	 * Translator for converting mappings in the map.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-08
	 */
	protected class XMLMapEntry implements XMLTranslator<Map.Entry<K,V>> {
		
		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = -522271733189736282L;
		
		/**
	     * Local name of the XML element.
	     */
	    public static final String TAG = "entry";
	    /**
	     * Local name of the key element.
	     */
	    protected static final String KEY_TAG = "key";
	    /**
	     * Local name of the value element.
	     */
	    protected static final String VALUE_TAG = "value";
		
	    /**
	     * Translator used to translate mapping keys.
	     */
	    protected final XMLTranslator<K> keyTranslator;
	    /**
	     * Translator used to translate mapping values.
	     */
	    protected final XMLTranslator<V> valueTranslator;
		
		/**
		 * Instantiates a mapping translator that uses the given translators for keys and
		 * values.
		 * 
		 * @param keyTranslator Translator to use to translate mapping keys.
		 * @param valueTranslator Translator to use to translate mapping values.
		 */
		public XMLMapEntry( XMLTranslator<K> keyTranslator, XMLTranslator<V> valueTranslator ) {
			
			this.keyTranslator = keyTranslator;
			this.valueTranslator = valueTranslator;
			
		}

		@Override
		public Entry<K,V> read( XMLStreamReader in ) throws XMLStreamException {

			if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
		              !in.getLocalName().equals( TAG ) ) {
				throw new XMLStreamException( "Did not find element start." );
		    }
		        
	        K key = null;
	        V value = null;
	        boolean readingKey = false;
	        boolean readingValue = false;
	        while ( in.hasNext() ) { // Read each element.
	            
	            switch ( in.next() ) {
	                
	                case XMLStreamConstants.START_ELEMENT:
	                	if ( readingKey ) { // Is reading a key.
	                		key = keyTranslator.read( in );
	                	} else if ( readingValue ) { // Is reading a value.
	                		value = valueTranslator.read( in );
	                	} else { // Not currently reading anything.
		                    switch ( in.getLocalName() ) {
		                    
			                    case KEY_TAG: // Found key element.
			                    	if ( key != null ) {
			                    		throw new XMLStreamException( "Mapping has multiple keys." );
			                    	}
			                    	readingKey = true;
			                    	break;
			                    	
			                    case VALUE_TAG: // Found value element.
			                    	if ( value != null ) {
			                    		throw new XMLStreamException( "Mapping has multiple values." );
			                    	}
			                    	readingValue = true;
			                    	break;
			                    	
		                    	default: // Unrecognized element.
		                    		throw new XMLStreamException( "Unexpected XML element." );
		                 
		                    }
	                	}
	                	break;
	                    
	                case XMLStreamConstants.END_ELEMENT:
	                	switch ( in.getLocalName() ) {
	                    
		                    case KEY_TAG: // Finished reading key.
		                    	readingKey = false;
		                    	break;
		                    	
		                    case VALUE_TAG: // Finished reading value.
		                    	readingValue = false;
		                    	break;
		                    	
		                    case TAG: // Finished reading entry.
		                    	if ( readingKey || readingValue ) {
		                    		throw new XMLStreamException( "Element end while still reading key or value" );
		                    	}
		                    	if ( ( key == null ) || ( value == null ) ) {
		                    		throw new XMLStreamException( "Mapping missing key or value." );
		                    	}
		                    	return new EntryImpl( key, value );
		                    	
	                    	default: // Unrecognized tag.
	                    		throw new XMLStreamException( "Unexpected end element." );
	                 
	                    }
	                	break;
		                
	            }
	            
	        }
	        throw new XMLStreamException( "Unexpected end of document." );
			
		}

		@Override
		public void write( XMLStreamWriter out, Entry<K,V> instance ) throws XMLStreamException {

			out.writeStartElement( TAG );
			
			out.writeStartElement( KEY_TAG ); // Write key.
			keyTranslator.write( out, instance.getKey() );
			out.writeEndElement();
			
			out.writeStartElement( VALUE_TAG ); // Write value.
			valueTranslator.write( out, instance.getValue() );
			out.writeEndElement();
			
			out.writeEndElement();
			
		}
		
		/**
		 * A mapping read from the XML stream.
		 * 
		 * @version 1.0
		 * @author ThiagoTGM
		 * @since 2018-08-08
		 */
		private class EntryImpl implements Map.Entry<K,V> {
			
			private final K key;
			private final V value;
			
			/**
			 * Instantiates an entry that represents the given key mapped to the
			 * given value.
			 * 
			 * @param key The key of the mapping.
			 * @param value The value of the mapping.
			 */
			public EntryImpl( K key, V value ) {
				
				this.key = key;
				this.value = value;
				
			}

			@Override
			public K getKey() {

				return key;
				
			}

			@Override
			public V getValue() {

				return value;
				
			}

			@Override
			public V setValue( V value ) throws UnsupportedOperationException {

				throw new UnsupportedOperationException();
				
			}
			
		}
		
	}

	/* Wrapper for maps */
	
	/**
	 * Wrapper that allows the use of any Map class as an XMLElement, using a backing
	 * instance of the desired class and an {@link XMLMap} translator.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-08
	 * @param <K> The type of keys in the map.
	 * @param <V> The type of the values in the map.
	 */
	public static class WrappedMap<K,V> implements XMLElement, Map<K,V> {
		
		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = -6400842899888638846L;
		
		private final Map<K,V> backing;
		private final XMLMap<K,V> translator;
		
		/**
		 * Instantiates a wrapper using an instance of the given class as backing map.
		 * <p>
		 * The given class must have a no-args constructor available for instancing.
		 * 
		 * @param mapClass The class to use for the backing map.
		 * @param keyTranslator The translator to use for keys.
		 * @param valueTranslator The translator to use for values.
		 * @throws IllegalArgumentException if the given class does not have an accessible no-arg constructor.
		 * @throws NullPointerException if any of the arguments is <tt>null</tt>.
		 */
		public WrappedMap( Class<? extends Map<K,V>> mapClass, XMLTranslator<K> keyTranslator,
				XMLTranslator<V> valueTranslator ) throws IllegalArgumentException, NullPointerException {
			
			this.translator = new XMLMap<>( mapClass, keyTranslator, valueTranslator );
			
			try { // Make backing instance.
				this.backing = translator.mapCtor.newInstance();
			} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e ) {
				throw new IllegalArgumentException( "Could not create backing map instance.", e );
			}
			
		}

		@Override
		public int size() {

			return backing.size();
			
		}

		@Override
		public boolean isEmpty() {

			return backing.isEmpty();
			
		}

		@Override
		public boolean containsKey( Object key ) {

			return backing.containsKey( key );
			
		}

		@Override
		public boolean containsValue( Object value ) {

			return backing.containsValue( value );
			
		}

		@Override
		public V get( Object key ) {

			return backing.get( key );
			
		}

		@Override
		public V put( K key, V value ) {

			return backing.put( key, value );
			
		}

		@Override
		public V remove(Object key) {
			
			return backing.remove( key );
			
		}

		@Override
		public void putAll( Map<? extends K, ? extends V> m ) {

			backing.putAll( m );
			
		}

		@Override
		public void clear() {

			backing.clear();
			
		}

		@Override
		public Set<K> keySet() {

			return backing.keySet();
			
		}

		@Override
		public Collection<V> values() {

			return backing.values();
			
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			
			return backing.entrySet();
			
		}

		@Override
		public void read( XMLStreamReader in ) throws XMLStreamException {

			translator.read( in, backing );
			
		}

		@Override
		public void write(XMLStreamWriter out) throws XMLStreamException {

			translator.write( out, backing );
			
		}
		
	}
	
}
