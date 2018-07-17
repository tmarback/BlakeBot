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

/**
 * Translates (converts) objects of a certain type to a string and vice versa.
 * 
 * @version 1.0
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
	 */
	String encode( T obj );
	
	/**
	 * Restores an object from a String created using {@link #encode(Object)}.
	 * 
	 * @param str The string to be decoded.
	 * @return The translated object.
	 */
	T decode( String str );

}
