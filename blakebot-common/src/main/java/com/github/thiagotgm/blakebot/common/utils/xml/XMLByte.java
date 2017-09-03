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
 * XML wrapper for <tt>byte</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLByte extends XMLTextData<Byte> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 7275877933820716020L;

    /**
     * Initializes a wrapper with no wrapped byte.
     */
    public XMLByte() {
        
        super();
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given byte.
     *
     * @param num The byte to be wrapped.
     */
    public XMLByte( Byte num ) {
        
        super( num );
        
    }

    @Override
    protected Byte fromString( String str ) {

        try {
            return Byte.valueOf( str );
        } catch ( NumberFormatException e ) {
            return null;
        }
        
    }

    @Override
    protected String toString( Byte obj ) {

        return String.valueOf( obj );
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLByte> newFactory() {
        
        return new Factory();
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLByte> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = 4504498337766142899L;     

        @Override
        public XMLByte newInstance() {

            return new XMLByte();
            
        }
        
    }

}
