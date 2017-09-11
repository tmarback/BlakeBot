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
 * XML wrapper for <tt>String</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLString extends XMLTextData<String> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -1291884558748264684L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "string";
    
    /**
     * Initializes a wrapper with no wrapped string.
     */
    public XMLString() {
        
        super();
        
    }
    
    /**
     * Initializes a wrapper that initially wraps the given string.
     *
     * @param str The string to be wrapped.
     */
    public XMLString( String str ) {
        
        super( str );
        
    }
    
    @Override
    public String getTag() {
        
        return TAG;
        
    }

    @Override
    protected String fromString( String str ) {

        return str;
        
    }

    @Override
    protected String toString( String obj ) {

        return obj;
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLString> newFactory() {
        
        return new Factory();
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLString> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -4889730359972410684L;

        @Override
        public XMLString newInstance() {

            return new XMLString();
            
        }
        
    }

}
