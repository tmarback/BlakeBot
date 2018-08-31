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

import java.util.ArrayList;
import java.util.List;

import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for lists of objects.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-28
 * @param <E> The type of objects in the lists to be translated.
 */
public class ListTranslator<E> extends CollectionTranslator<E,List<E>> {
	
	/**
	 * Initializes a list translator that uses the given translator for
	 * each of the objects in the string.
	 * 
	 * @param elementTranslator The translator to be used for the elements
	 *                          in the list.
	 */
	public ListTranslator( Translator<E> elementTranslator ) {
		
		super( elementTranslator );
		
	}

	@Override
	protected List<E> newInstance() {
		
		return new ArrayList<>();
		
	}

}
