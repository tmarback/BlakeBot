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

import java.io.IOException;

/**
 * Translates (converts) objects of a certain type to a string and vice versa.
 * 
 * @version 1.1
 * @author ThiagoTGM
 * @since 2018-07-16
 * @param <T> The type of object to be translated.
 */
public interface Translator<T> {
	
	/**
	 * Converts the given object into a String format.
	 * 
	 * @param obj The object to be encoded.
	 * @return The String encoding of the object.
	 * @throws IOException if an error was encountered while encoding.
	 */
	String encode( T obj ) throws IOException;
	
	/**
	 * Attempts the given object into a String format. If the given
	 * object is not of the type that can be translated by this Translator,
	 * returns <tt>null</tt>.
	 * 
	 * @param obj The object to be encoded.
	 * @return The String encoding of the object, or <tt>null</tt> if the given
	 *         object is not of the supported type.
	 * @throws IOException if an error was encountered while encoding.
	 */
	@SuppressWarnings("unchecked")
	default String encodeObj( Object obj ) throws IOException {
		
		try {
			return encode( (T) obj ); // Attempt to translate.
		} catch ( ClassCastException e ) {
			return null; // Not the correct type.
		}
		
	}
	
	/**
	 * Restores an object from a String created using {@link #encode(Object)}.
	 * 
	 * @param str The string to be decoded.
	 * @return The translated object.
	 * @throws IOException if an error was encountered while decoding.
	 */
	T decode( String str ) throws IOException;

}
