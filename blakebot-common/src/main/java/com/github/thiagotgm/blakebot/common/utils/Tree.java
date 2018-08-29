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

package com.github.thiagotgm.blakebot.common.utils;

import java.util.List;
import java.util.Map;

/**
 * Represents a directed tree graph, eg a graph where the same keys in different orders
 * map to different values, and the graph always starts at a immutable <i>root</i> element
 * (represented by an empty path), which may or may not be accessible.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-16
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public interface Tree<K,V> extends Graph<K,V> {

	/**
	 * Obtains a tree that is backed by the given map, by using the path list
	 * of each mapping as a key into the given map.
	 * <p>
	 * In other words, obtains a tree view of a map that is keyed by lists.
	 *
	 * @param <K> The type of keys in the map key list/tree path.
	 * @param <V> The type of values being stored.
	 * @param backing The backing map.
	 * @return A tree view of the given map.
	 */
	public static <K,V> Tree<K,V> mappedTree( Map<List<K>,V> backing ) {
		
		return new MappedTree<>( backing );
		
	}
	
}
