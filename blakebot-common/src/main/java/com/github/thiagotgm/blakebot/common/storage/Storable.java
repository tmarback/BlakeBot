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

import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;

/**
 * Interface for object types that are capable of translating themselves to and from
 * Data.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-02
 */
public interface Storable {
	
	/**
	 * Stores (saves) this object's state into a Data format.
	 * 
	 * @return The data encoding of this object.
	 */
	Data toData();
	
	/**
	 * Loads (restores) object state from a Data format.
	 * <p>
	 * Any current data is overwritten by the loaded data.
	 * 
	 * @param data The data encoding to restore.
	 * @throws TranslationException if an error occurred while translating. Usually
	 * 								this would mean the given data does not correspond
	 *                              to a valid state of this object type.
	 */
	void fromData( Data data ) throws TranslationException;

}
