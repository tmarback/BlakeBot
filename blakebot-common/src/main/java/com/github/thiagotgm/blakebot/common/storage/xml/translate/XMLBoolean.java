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

package com.github.thiagotgm.blakebot.common.storage.xml.translate;

/**
 * XML translator for <tt>boolean</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLBoolean extends XMLTextData<Boolean> {
     
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 8113277187822773509L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "boolean";
    
    @Override
    public String getTag() {
        
        return TAG;
        
    }

    @Override
    protected Boolean fromString( String str ) {

        if ( "true".equalsIgnoreCase( str ) ) {
            return Boolean.TRUE;
        }
        if ( "false".equalsIgnoreCase( str ) ) {
            return Boolean.FALSE;
        }
        return null;
        
    }

    @Override
    protected String toString( Boolean obj ) {

        return String.valueOf( obj );
        
    }

}
