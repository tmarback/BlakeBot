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

package com.github.thiagotgm.blakebot.common.storage.xml.translate;

import java.util.Collection;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;

/**
 * Translator for generalized Collection objects.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-07-22
 * @param <E> The type of elements being stored in the collection.
 */
public class XMLCollection<E> extends AbstractXMLCollection<E,Collection<E>> {
	
    /**
	 * UID that represents this class.
	 */
	private static final long serialVersionUID = 1003839832146887345L;
	
	/**
     * Local name of the XML element.
     */
    public static final String TAG = "collection";
    
    /**
     * Instantiates an collection translator that uses instances of the given Collection class and
     * uses the given translator for the elements.
     *
     * @param collectionClass The class of collection to instantiate.
     * @param translator The translator to use for the collection elements.
     */
    public XMLCollection( Class<? extends Collection<E>> collectionClass, XMLTranslator<E> translator )
    		throws IllegalArgumentException {
    	
    	super( collectionClass, translator );
    	
    }
	
	@Override
	public String getTag() {
        
        return TAG;
        
    }

}
