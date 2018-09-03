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

package com.github.thiagotgm.blakebot.common.storage.translate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for maps of objects.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-02
 * @param <K> The type of keys in the maps to be translated.
 * @param <V> The type of values in the maps to be translated.
 */
public class MapTranslator<K,V> implements Translator<Map<K,V>> {
	
	/**
	 * When the no-arg constructor is used, the instances returned when
	 * decoding will be of this class.
	 */
	public static final Class<?> DEFAULT_CLASS = HashMap.class;
	
	private final Supplier<? extends Map<K,V>> instanceSupplier;
	private final Translator<K> keyTranslator;
	private final Translator<V> valueTranslator;
	
	/**
	 * Initializes a map translator that uses the given translators for
	 * the keys and values in the map, and uses the given supplier
	 * to create map instances.
	 * 
	 * @param instanceSupplier Supplier to get instances from when decoding.
	 * @param keyTranslator The translator to be used for the keys
	 *                      in the map.
	 * @param valueTranslator The translator to be used for the keys
	 *                        in the map.
	 * @throws NullPointerException if any of the arguments is <tt>null</tt>.
	 */
	public MapTranslator( Supplier<? extends Map<K,V>> instanceSupplier,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
					throws NullPointerException {
		
		if ( instanceSupplier == null ) {
			throw new NullPointerException( "Supplier cannot be null." );
		}
		
		if ( keyTranslator == null ) {
			throw new NullPointerException( "Key translator cannot be null." );
		}
		
		if ( valueTranslator == null ) {
			throw new NullPointerException( "Value translator cannot be null." );
		}
		
		this.instanceSupplier = instanceSupplier;
		this.keyTranslator = keyTranslator;
		this.valueTranslator = valueTranslator;
		
	}
	
	/**
	 * Initializes a map translator that uses the given translators for
	 * the keys and values in the map, and instances of the given class
	 * for decoded maps. The given class must have a public no-arg
	 * constructor.
	 * 
	 * @param clazz Class to make instances of when decoding.
	 * @param keyTranslator The translator to be used for the keys
	 *                      in the map.
	 * @param valueTranslator The translator to be used for the keys
	 *                        in the map.
	 * @throws NullPointerException if any of the arguments is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given class does not have
	 *                                  a public no-arg constructor.
	 */
	public MapTranslator( Class<? extends Map<K,V>> clazz,
			Translator<K> keyTranslator, Translator<V> valueTranslator )
					throws NullPointerException, IllegalArgumentException {
		
		if ( clazz == null ) {
			throw new NullPointerException( "Class cannot be null." );
		}
		
		if ( keyTranslator == null ) {
			throw new NullPointerException( "Key translator cannot be null." );
		}
		
		if ( valueTranslator == null ) {
			throw new NullPointerException( "Value translator cannot be null." );
		}
		
		Constructor<? extends Map<K,V>> ctor;
		try {
			ctor = clazz.getConstructor();
		} catch ( NoSuchMethodException | SecurityException e ) {
			throw new IllegalArgumentException( "No avaliable no-arg ctor." );
		}
		
		if ( !Modifier.isPublic( ctor.getModifiers() ) ) {
			throw new IllegalArgumentException( "No-arg ctor is not public." );
		}
		
		instanceSupplier = () -> {
			
			try {
				return ctor.newInstance();
			} catch ( Exception e ) {
				throw new TranslationException( "Could not create instance.", e );
			}
			
		};
		this.keyTranslator = keyTranslator;
		this.valueTranslator = valueTranslator;
		
	}
	
	/**
	 * Initializes a map translator that uses the given translators for
	 * the keys and values in the map, and instances of the 
	 * {@link #DEFAULT_CLASS default class} for decoded maps.
	 * 
	 * @param keyTranslator The translator to be used for the keys
	 *                      in the map.
	 * @param valueTranslator The translator to be used for the keys
	 *                        in the map.
	 * @throws NullPointerException if either of the given translators is <tt>null</tt>.
	 */
	@SuppressWarnings("unchecked")
	public MapTranslator( Translator<K> keyTranslator, Translator<V> valueTranslator )
			throws NullPointerException {
		
		this( (Class<Map<K,V>>) DEFAULT_CLASS, keyTranslator, valueTranslator );
		
	}

	@Override
	public Data toData( Map<K,V> obj ) throws TranslationException {

		Map<String,Data> translatedMap = new HashMap<>();
		for ( Map.Entry<K,V> entry : obj.entrySet() ) { // Translate each entry.
			
			translatedMap.put( keyTranslator.encode( entry.getKey() ),
					valueTranslator.toData( entry.getValue() ) );
			
		}
		return Data.mapData( translatedMap );
		
	}

	@Override
	public Map<K,V> fromData( Data data ) throws TranslationException {
		
		if ( !data.isMap() ) {
			throw new TranslationException( "Given data is not a map." );
		}

		Map<K,V> obj = instanceSupplier.get();
		for ( Map.Entry<String,Data> entry : data.getMap().entrySet() ) {
			
			// Translate each entry.
			obj.put( keyTranslator.decode( entry.getKey() ),
					valueTranslator.fromData( entry.getValue() ) );
			
		}
		return obj;
		
	}

}
