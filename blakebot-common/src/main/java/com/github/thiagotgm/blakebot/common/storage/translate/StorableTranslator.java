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
import java.util.function.Supplier;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Storable;
import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for {@link Storable} types. Uses the self-translating methods
 * provided by that interface.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-02
 */
public class StorableTranslator<T extends Storable> implements Translator<T> {

	private final Supplier<? extends T> instanceProducer;
	
	/**
	 * Instantiates a translator that uses the given supplier to obtain
	 * instances to return when decoding from data.
	 * 
	 * @param instanceProducer Supplies instances when decoding.
	 * @throws NullPointerException if the given supplier is <tt>null</tt>.
	 */
	public StorableTranslator( Supplier<? extends T> instanceProducer )
			throws NullPointerException {
		
		if ( instanceProducer == null ) {
			throw new NullPointerException( "Supplier cannot be null." );
		}
		
		this.instanceProducer = instanceProducer;
		
	}
	
	/**
	 * Instantiates a translator that creates instances of the given class
	 * to return when decoding from data. The given class must have a
	 * public no-arg constructor.
	 * 
	 * @param clazz The class to make instances of when decoding.
	 * @throws NullPointerException if the given class is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given class does not have
	 *                                  a public no-arg constructor.
	 */
	public StorableTranslator( Class<? extends T> clazz )
			throws NullPointerException, IllegalArgumentException {
		
		if ( clazz == null ) {
			throw new NullPointerException( "Class cannot be null." );
		}
		
		Constructor<? extends T> ctor;
		try {
			ctor = clazz.getConstructor();
		} catch ( NoSuchMethodException | SecurityException e ) {
			throw new IllegalArgumentException( "No avaliable no-arg ctor." );
		}
		
		if ( !Modifier.isPublic( ctor.getModifiers() ) ) {
			throw new IllegalArgumentException( "No-arg ctor is not public." );
		}
		
		instanceProducer = () -> {
			
			try {
				return ctor.newInstance();
			} catch ( Exception e ) {
				throw new TranslationException( "Could not create instance.", e );
			}
			
		};
		
	}
	
	@Override
	public Data toData( T obj ) throws TranslationException {

		return obj.toData();
		
	}

	@Override
	public T fromData( Data data ) throws TranslationException {

		T obj = instanceProducer.get();
		obj.fromData( data );
		return obj;
		
	}

}
