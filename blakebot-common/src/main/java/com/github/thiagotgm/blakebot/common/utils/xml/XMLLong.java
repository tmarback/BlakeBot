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

package com.github.thiagotgm.blakebot.common.utils.xml;

import com.github.thiagotgm.blakebot.common.utils.XMLElement;

/**
 * XML wrapper for <tt>long</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLLong extends XMLTextData<Long> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 6916907635448757798L;

    /**
     * Initializes a wrapper with no wrapped long.
     */
    public XMLLong() {
        
        super();
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given long.
     *
     * @param num The long to be wrapped.
     */
    public XMLLong( Long num ) {
        
        super( num );
        
    }

    @Override
    protected Long fromString( String str ) {

        try {
            return Long.valueOf( str );
        } catch ( NumberFormatException e ) {
            return null;
        }
        
    }

    @Override
    protected String toString( Long obj ) {

        return String.valueOf( obj );
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLLong> newFactory() {
        
        return new Factory();
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLLong> {  

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -7317024187839932039L;

        @Override
        public XMLLong newInstance() {

            return new XMLLong();
            
        }
        
    }

}
