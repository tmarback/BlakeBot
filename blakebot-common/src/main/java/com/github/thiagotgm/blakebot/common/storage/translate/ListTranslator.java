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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.utils.Utils;

/**
 * Translator for lists of objects, that translates each object and then joins them
 * using {@link Utils#encodeList(List)}.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-28
 * @param <T> The type of objects in the lists to be translated.
 */
public class ListTranslator<T> implements Translator<List<T>> {
	
	private final Translator<T> elementTranslator;
	
	/**
	 * Initializes a list translator that uses the given translator for
	 * each of the objects in the string.
	 * 
	 * @param elementTranslator The translator to be used for the elements
	 *                          in the list.
	 */
	public ListTranslator( Translator<T> elementTranslator ) {
		
		this.elementTranslator = elementTranslator;
		
	}

	@Override
	public String encode( List<T> list ) throws IOException {

		List<String> translatedList = new ArrayList<>( list.size() );
		for ( T element : list ) { // Translate each element in the list.
			
			try {
				translatedList.add( elementTranslator.encode( element ) );
			} catch ( IOException e ) {
				throw new IOException( "Could not translate element.", e );
			}
			
		}
		return Utils.encodeList( translatedList ); // Encode into a string.
		
	}

	@Override
	public List<T> decode( String str ) throws IOException {

		List<String> translatedList = Utils.decodeList( str ); // Decode from string.
		List<T> list = new ArrayList<>();
		for ( String element : translatedList ) { // Decode each element.
			
			try {
				list.add( elementTranslator.decode( element ) );
			} catch ( IOException e ) {
				throw new IOException( "Could not translate element.", e );
			}
			
		}
		return list;
		
	}

}
