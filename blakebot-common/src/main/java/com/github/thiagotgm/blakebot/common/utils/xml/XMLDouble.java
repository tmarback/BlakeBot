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

/**
 * XML translator for <tt>double</tt>s.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-29
 */
public class XMLDouble extends XMLTextData<Double> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 5433372977234659394L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "double";
    
    @Override
    public String getTag() {
        
        return TAG;
        
    }

    @Override
    protected Double fromString( String str ) {

        try {
            return Double.valueOf( str );
        } catch ( NumberFormatException e ) {
            return null;
        }
        
    }

    @Override
    protected String toString( Double obj ) {

        return String.valueOf( obj );
        
    }

}
