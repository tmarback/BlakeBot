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
 * Provides common implementation of some of the XMLWrapper methods.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-22
 * @param <T> The type of object being wrapped.
 */
public abstract class AbstractXMLWrapper<T> implements XMLWrapper<T> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -3790139325439880627L;
    
    private T obj;
    
    /**
     * Initializes a wrapper with no wrapped object.
     */
    public AbstractXMLWrapper() {
        
        setObject( null );
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given object.
     *
     * @param obj The object to be wrapped.
     */
    public AbstractXMLWrapper( T obj ) {
        
        setObject( obj );
        
    }
    
    @Override
    public T getObject() {
        
        return obj;
        
    }
    
    @Override
    public void setObject( T obj ) {
        
        this.obj = obj;
        
    }
    
    @Override
    public boolean equals( Object obj ) {
        
        if ( !( obj instanceof XMLWrapper ) ) {
            return false; // Not a wrapper.
        }
        
        XMLWrapper<?> wrapper = (XMLWrapper<?>) obj;
        return this.getObject() == null ? wrapper.getObject() == null
                : this.getObject().equals( wrapper.getObject() );
        
    }
    
    @Override
    public int hashCode() {
        
        return getObject() == null ? 0 : getObject().hashCode();
        
    }
    
    /**
     * Returns the string representation of the wrapped object, or <tt>null</tt> if no wrapped object.
     */
    @Override
    public String toString() {
        
        return ( getObject() == null ) ? null : getObject().toString();
        
    }

}
