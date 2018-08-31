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

package com.github.thiagotgm.blakebot.common.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Translates (converts) objects of a certain type to a map or string and vice versa.
 * 
 * @version 2.0
 * @author ThiagoTGM
 * @since 2018-07-16
 * @param <T> The type of object to be translated.
 */
public interface Translator<T> {
	
	final Gson GSON = new GsonBuilder().registerTypeAdapter( Data.class, new Data.DataAdapter() ).create();
	
	/**
	 * Converts the given object into a Data format.
	 * 
	 * @param obj The object to be encoded.
	 * @return The data encoding of the object.
	 * @throws TranslationException if an error was encountered while encoding.
	 */
	Data toData( T obj ) throws TranslationException;
	
	/**
	 * Attempts to convert the given object into a Data format. If the given
	 * object is not of the type that can be translated by this Translator,
	 * returns <tt>null</tt>.
	 * 
	 * @param obj The object to be encoded.
	 * @return The Data encoding of the object, or <tt>null</tt> if the given
	 *         object is not of the supported type.
	 * @throws TranslationException if an error was encountered while encoding.
	 */
	@SuppressWarnings("unchecked")
	default Data objToData( Object obj ) throws TranslationException {
		
		try {
			return toData( (T) obj ); // Attempt to translate.
		} catch ( ClassCastException e ) {
			return null; // Not the correct type.
		}
		
	}
	
	/**
	 * Converts the given object into a String format.
	 * <p>
	 * By default, this just encodes the return of {@link #toData(T)} into a
	 * JSON format. If this is overriden, {@link #decode(String)} must be
	 * overriden as well.
	 * 
	 * @param obj The object to be encoded.
	 * @return The String encoding of the object.
	 * @throws TranslationException if an error was encountered while encoding.
	 */
	default String encode( T obj ) throws TranslationException {
		
		return GSON.toJson( toData( obj ) );
		
	}
	
	/**
	 * Attempts to convert the given object into a String format. If the given
	 * object is not of the type that can be translated by this Translator,
	 * returns <tt>null</tt>.
	 * 
	 * @param obj The object to be encoded.
	 * @return The String encoding of the object, or <tt>null</tt> if the given
	 *         object is not of the supported type.
	 * @throws TranslationException if an error was encountered while encoding.
	 */
	@SuppressWarnings("unchecked")
	default String encodeObj( Object obj ) throws TranslationException {
		
		try {
			return encode( (T) obj ); // Attempt to translate.
		} catch ( ClassCastException e ) {
			return null; // Not the correct type.
		}
		
	}
	
	/**
	 * Restores an object from data created using {@link #toData(Object)}.
	 * 
	 * @param data The data to be decoded.
	 * @return The translated object.
	 * @throws TranslationException if an error was encountered while decoding.
	 */
	T fromData( Data data ) throws TranslationException;
	
	/**
	 * Restores an object from a String created using {@link #encode(Object)}.
	 * 
	 * @param str The string to be decoded.
	 * @return The translated object.
	 * @throws TranslationException if an error was encountered while decoding.
	 */
	default T decode( String str ) throws TranslationException {
		
		return fromData( GSON.fromJson( str, Data.class ) );
		
	}
	
	/**
	 * Exception that indicates that an error occurred while encoding or decoding a value.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-29
	 */
	class TranslationException extends RuntimeException {
		
		/**
		 * UID that represents this class.
		 */
		private static final long serialVersionUID = 1126112129198492514L;

		/**
		 * Constructs a new translation exception with no cause.
		 * 
		 * @see RuntimeException#RuntimeException()
	     */
		public TranslationException() {
			
			super();

		}

		/**
		 * Constructs a new translation exception with the given detail message and cause.
		 * 
		 * @param message The detail message.
		 * @param cause The cause of this exception.
		 * @see RuntimeException#RuntimeException(String, Throwable)
		 */
		public TranslationException( String message, Throwable cause ) {
			
			super( message, cause );
			
		}

		/**
		 * Constructs a new translation exception with the given detail message and no cause.
		 * 
		 * @param message The detail message.
		 * @see RuntimeException#RuntimeException(String)
		 */
		public TranslationException( String message ) {
			
			super( message );

		}

		/**
		 * Constructs a new translation exception with the given cause.
		 * 
		 * @param cause The cause of this exception.
		 * @see RuntimeException#RuntimeException(Throwable)
		 */
		public TranslationException( Throwable cause ) {
			
			super( cause );

		}
		
	}

}
