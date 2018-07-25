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

import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for Strings that just acts as a pass-through.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-23
 */
public class StringTranslator implements Translator<String> {

	@Override
	public String encode( String obj ) throws IOException {
		
		return obj;
		
	}

	@Override
	public String decode(String str) throws IOException {
		
		return str;
		
	}

}