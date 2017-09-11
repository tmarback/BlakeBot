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
 * XML wrapper for <tt>short</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLShort extends XMLTextData<Short> {
     
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -5117056623380307820L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "short";
    
    /**
     * Initializes a wrapper with no wrapped short.
     */
    public XMLShort() {
        
        super();
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given short.
     *
     * @param num The short to be wrapped.
     */
    public XMLShort( Short num ) {
        
        super( num );
        
    }
    
    @Override
    public String getTag() {
        
        return TAG;
        
    }

    @Override
    protected Short fromString( String str ) {

        try {
            return Short.valueOf( str );
        } catch ( NumberFormatException e ) {
            return null;
        }
        
    }

    @Override
    protected String toString( Short obj ) {

        return String.valueOf( obj );
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLShort> newFactory() {
        
        return new Factory();
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLShort> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = 2384344069819925075L; 

        @Override
        public XMLShort newInstance() {

            return new XMLShort();
            
        }
        
    }

}
