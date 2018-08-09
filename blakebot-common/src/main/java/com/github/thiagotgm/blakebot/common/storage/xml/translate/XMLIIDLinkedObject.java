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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * XML translator for <tt>IIDLinkedObject</tt>s. The actual reading and writing is
 * delegated to a translator of the IIDLinkedObject subtype, so the supported
 * IIDLinkedObjects are the ones that have their own XML wrappers.
 * <p>
 * Note: Can only be used when the client is connected.
 *
 * @version 2.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLIIDLinkedObject implements XMLTranslator<IIDLinkedObject> {

    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 6752712491496563108L;
    
    /**
     * Client to use for obtaining objects from their IDs.
     */
    protected final IDiscordClient client;
    
    /**
     * Map for obtaining the correct translator given an XML tag.
     */
    protected final Map<String,
			AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> tagMap;

    /**
     * Map for obtaining the correct translator given a class.
     */
	protected final Map<Class<? extends IIDLinkedObject>,
			AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> classMap;

    /**
     * Instantiates a translator.
     *
     * @param client The client to use for obtaining objects.
     */
    public XMLIIDLinkedObject( IDiscordClient client ) {

    	this.client = client;
    	
    	// Make translator instances.
    	List<AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> translators = new LinkedList<>();
        translators.add( new XMLChannel( client ) );
        translators.add( new XMLEmoji( client ) );
        translators.add( new XMLGuild( client ) );
        translators.add( new XMLMessage( client ) );
        translators.add( new XMLRole( client ) );
        translators.add( new XMLUser( client ) );
        translators.add( new XMLVoiceChannel( client ) );
        translators.add( new XMLWebhook( client ) );
    	
    	// Create maps.
    	Map<String, AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> tagMap =
    			new HashMap<>();
    	Map<Class<? extends IIDLinkedObject>, 
				AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> classMap =
				new HashMap<>();
    	
    	// Set up maps.
    	for ( AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject> translator : translators ) {
    		
    		tagMap.put( translator.getTag(), translator );
    		classMap.put( translator.getTranslatedClass(), translator );
    		
    	}
    	
    	// Register maps.
    	this.tagMap = Collections.unmodifiableMap( tagMap );
    	this.classMap = Collections.unmodifiableMap( classMap );

    }

    @Override
    public IIDLinkedObject read( XMLStreamReader in ) throws XMLStreamException {

    	if ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Stream not in start element." );
        }
    	
    	return tagMap.get( in.getLocalName() ).read( in );

    }

    @Override
    public void write( XMLStreamWriter out, IIDLinkedObject instance ) throws XMLStreamException {

        for ( Map.Entry<Class<? extends IIDLinkedObject>,
			AbstractXMLIDLinkedTranslator<? extends IIDLinkedObject>> entry : classMap.entrySet() ) {
        	
        	if ( entry.getKey().isAssignableFrom( instance.getClass() ) ) {
        		entry.getValue().writeGeneric( out, instance );
        		return;
        	}
        	
        }

    }

}
