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

/**
 * A graph that is capable of being saved to an XML format, then loaded back up from it.
 * <p>
 * The XML representation of a graph must be readable by a properly configured object of the same graph
 * type, but it is not required to be readable by instances of other graph implementations or by
 * instances of its own supertypes.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-23
 * @param <K> The type of the keys that define connections on the graph.
 * @param <V> The type of the values to be stored.
 */
public interface XMLGraph<K,V> extends XMLElement, Graph<K,V> {}
