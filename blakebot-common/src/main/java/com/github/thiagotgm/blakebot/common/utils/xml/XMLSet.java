/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * BlakeBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.common.utils.xml;

import java.util.Set;

import com.github.thiagotgm.blakebot.common.utils.XMLElement;

/**
 * Adapter for a Set of objects that allows writing the set's contents
 * to an XML stream, or read them from one.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-10
 * @param <E> The type of elements being stored in the set.
 */
public class XMLSet<E extends XMLElement> extends XMLCollection<E> implements Set<E> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -7391823040267850261L;

    /**
     * Instantiates an XMLSet backed by the given set that uses the given factory
     * to create new element instances.
     *
     * @param set The backing set.
     * @param factory The factory to create element instances with.
     */
    public XMLSet( Set<E> set, Factory<? extends E> factory ) {
        
        super( set, factory );
        
    }

}
