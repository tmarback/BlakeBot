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

import java.util.List;
import com.github.thiagotgm.blakebot.common.utils.XMLTranslator;

/**
 * Translator for generalized List objects.
 *
 * @version 2.0
 * @author ThiagoTGM
 * @since 2017-09-10
 * @param <E> The type of elements being stored in the list.
 */
public class XMLList<E> extends AbstractXMLCollection<E,List<E>> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -4591024804259782530L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "list";
    
    /**
     * Instantiates an list translator that uses instances of the given List class and
     * uses the given translator for the elements.
     *
     * @param listClass The class of list to instantiate.
     * @param translator The translator to use for the list elements.
     */
    public XMLList( Class<? extends List<E>> listClass, XMLTranslator<E> translator )
    		throws IllegalArgumentException {
    	
    	super( listClass, translator );
    	
    }
	
	@Override
	public String getTag() {
        
        return TAG;
        
    }

}
