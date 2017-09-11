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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.github.thiagotgm.blakebot.common.utils.XMLElement;

/**
 * Adapter for a List of objects that allows writing the list's contents
 * to an XML stream, or read them from one.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-10
 * @param <E> The type of elements being stored in the list.
 */
public class XMLList<E extends XMLElement> extends XMLCollection<E> implements List<E> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -4591024804259782530L;
    
    private final List<E> list;

    /**
     * Instantiates an XMLList backed by the given list that uses the given factory
     * to create new element instances.
     *
     * @param list The backing list.
     * @param factory The factory to create element instances with.
     */
    public XMLList( List<E> list, Factory<? extends E> factory ) {
        
        super( list, factory );
        this.list = list;
        
    }
  
    /* Delegates to the backing list */

    @Override
    public boolean addAll( int index, Collection<? extends E> c ) {

        return list.addAll( index, c );
        
    }

    @Override
    public E get( int index ) {

        return list.get( index );
        
    }

    @Override
    public E set( int index, E element ) {

        return list.set( index, element );
        
    }

    @Override
    public void add( int index, E element ) {

        list.add( index, element );
        
    }

    @Override
    public E remove( int index ) {

        return list.remove( index );
        
    }

    @Override
    public int indexOf( Object o ) {

        return list.indexOf( o );
        
    }

    @Override
    public int lastIndexOf( Object o ) {

        return list.lastIndexOf( o );
        
    }

    @Override
    public ListIterator<E> listIterator() {

        return list.listIterator();
        
    }

    @Override
    public ListIterator<E> listIterator( int index ) {

        return list.listIterator( index );
        
    }

    @Override
    public List<E> subList( int fromIndex, int toIndex ) {

        return list.subList( fromIndex, toIndex );
        
    }      

}
