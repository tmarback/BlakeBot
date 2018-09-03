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
import java.util.function.Supplier;

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
	 * When the no-arg constructor is used, the instances returned when
	 * decoding will be of this class.
	 */
	public static final Class<?> DEFAULT_CLASS = ArrayList.class;
	
	/**
	 * Initializes a list translator that uses the given translator for
	 * each of the elements in the list, and uses the given supplier
	 * to create list instances.
	 * 
	 * @param instanceSupplier Supplier to get instances from when decoding.
	 * @param elementTranslator The translator to be used for the elements
	 *                          in the list.
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 */
	public ListTranslator( Supplier<? extends List<E>> instanceSupplier,
			Translator<E> elementTranslator ) throws NullPointerException {
		
		super( instanceSupplier, elementTranslator );
		
	}
	
	/**
	 * Initializes a list translator that uses the given translator for
	 * each of the elements in the list, and instances of the given class
	 * for decoded lists. The given class must have a public no-arg
	 * constructor.
	 * 
	 * @param clazz Class to make instances of when decoding.
	 * @param elementTranslator The translator to be used for the elements
	 *                          in the list.
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given class does not have
	 *                                  a public no-arg constructor.
	 */
	public ListTranslator( Class<? extends List<E>> clazz, Translator<E> elementTranslator )
			throws NullPointerException, IllegalArgumentException {
		
		super( clazz, elementTranslator );
		
	}
	
	/**
	 * Initializes a list translator that uses the given translator for
	 * each of the elements in the list, and instances of the 
	 * {@link #DEFAULT_CLASS default class} for decoded lists.
	 * 
	 * @param elementTranslator The translator to be used for the elements
	 *                          in the list.
	 * @throws NullPointerException if the given translator is <tt>null</tt>.
	 */
	@SuppressWarnings("unchecked")
	public ListTranslator( Translator<E> elementTranslator ) throws NullPointerException {
		
		super( (Class<List<E>>) DEFAULT_CLASS, elementTranslator );
		
	}

}
