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

import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.translate.ListTranslator;
import com.github.thiagotgm.blakebot.common.utils.Tree;

/**
 * Superclass for table-based databases. Provides an implementation of Tree that
 * is backed by a map from the database.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-10
 */
public abstract class TableDatabase extends AbstractDatabase {

	@Override
	protected <K,V> Tree<K,V> newTree( String dataName, Translator<K> keyTranslator,
			Translator<V> valueTranslator ) throws DatabaseException {

		return Tree.mappedTree( newMap( dataName, new ListTranslator<>( keyTranslator ), valueTranslator ) );
		
	}

}
