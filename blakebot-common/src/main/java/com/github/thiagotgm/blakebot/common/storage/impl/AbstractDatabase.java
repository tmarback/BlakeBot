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

import java.util.Map;

import com.github.thiagotgm.blakebot.common.storage.Database;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.utils.Tree;

/**
 * Common implementations for all database types.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-26
 */
public abstract class AbstractDatabase implements Database {

	/* Entry classes */
	
	/**
	 * Implementation of a database entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the storage.
	 * @param <V> The type of value used by the storage.
	 * @param <T> The type of the storage unit.
	 */
	private abstract class DatabaseEntryImpl<K,V,T> implements DatabaseEntry<K,V,T> {
		
		private final String name;
		private final T storage;
		private final Translator<K> keyTranslator;
		private final Translator<V> valueTranslator;
		
		/**
		 * Initializes a database entry for the given storage unit, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage unit is registered under.
		 * @param storage The storage unit.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public DatabaseEntryImpl( String name, T storage, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			if ( ( name == null ) || ( storage == null ) || ( keyTranslator == null ) ||
					( valueTranslator == null ) ) {
				throw new NullPointerException( "Arguments can't be null." );
			}
			
			this.name = name;
			this.storage = storage;
			this.keyTranslator = keyTranslator;
			this.valueTranslator = valueTranslator;
			
		}
		
		@Override
		public String getName() {
			
			return name;
			
		}
		
		@Override
		public T getStorage() {
			
			return storage;
			
		}
		
		@Override
		public Translator<K> getKeyTranslator() {
			
			return keyTranslator;
			
		}
		
		@Override
		public Translator<V> getValueTranslator() {
			
			return valueTranslator;
			
		}
		
	}
	
	/**
	 * Implementation of a database tree entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the tree.
	 * @param <V> The type of value used by the tree.
	 */
	protected class TreeEntryImpl<K,V> extends DatabaseEntryImpl<K,V,Tree<K,V>> implements TreeEntry<K,V> {
		
		/**
		 * Initializes a tree entry for the given storage tree, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage tree is registered under.
		 * @param tree The storage tree.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public TreeEntryImpl( String name, Tree<K,V> tree, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			super( name, tree, keyTranslator, valueTranslator );
			
		}

	}
	
	/**
	 * Implementation of a database map entry.
	 * 
	 * @since 2018-07-26
	 * @param <K> The type of key used by the map.
	 * @param <V> The type of value used by the map.
	 */
	protected class MapEntryImpl<K,V> extends DatabaseEntryImpl<K,V,Map<K,V>> implements MapEntry<K,V> {
		
		/**
		 * Initializes a map entry for the given storage map, under the given name,
		 * with the given translators.
		 * 
		 * @param name The name that the storage unit is registered under.
		 * @param map The storage map.
		 * @param keyTranslator The translator for keys.
		 * @param valueTranslator The translator for values.
		 * @throws NullPointerException If any of the arguments is <tt>null</tt>.
		 */
		public MapEntryImpl( String name, Map<K,V> map, Translator<K> keyTranslator,
				Translator<V> valueTranslator ) throws NullPointerException {
			
			super( name, map, keyTranslator, valueTranslator );
			
		}
		
	}

}
