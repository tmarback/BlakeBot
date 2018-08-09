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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.storage.xml.XMLTranslator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * Shared implementation for translators for objects with a Discord ID.
 * <p>
 * Note: Translators based on this can only be used when the client is connected.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 * @param <T> Type of object being translated.
 */
abstract class AbstractXMLIDLinkedTranslator<T extends IIDLinkedObject> implements XMLTranslator<T> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -8288837938236407742L;

    private static final String ID_ATTRIBUTE = "id";
    
    private static final String GUILD_ATTRIBUTE = "guild";
    
    /**
     * Client to use for obtaining objects from their IDs.
     */
    protected final IDiscordClient client;
    
    /**
     * Instantiates a translator.
     *
     * @param client The client to use for obtaining objects.
     */
    public AbstractXMLIDLinkedTranslator( IDiscordClient client ) {
        
    	this.client = client;
        
    }
    
    /**
     * Obtains the object from its ID.
     *
     * @param id The ID of the object to retrieve.
     * @param guild The guild of the object. May be <tt>null</tt> if one is not specified
                    when writing due to not being necessary.
     * @return The object with the given ID.
     */
    protected abstract T getObject( long id, IGuild guild );
    
    @Override
    public T read( XMLStreamReader in ) throws XMLStreamException {

        if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
              !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Did not find element start." );
        }

        /* Get guild, if any */
        String guildID = in.getAttributeValue( null, GUILD_ATTRIBUTE );
        IGuild guild = null;
        if ( guildID != null ) {
            try {
                guild = client.getGuildByID( Long.parseUnsignedLong( guildID ) );
            } catch ( NumberFormatException e ) {
                throw new XMLStreamException( "Invalid guild ID.", e );
            }
        }

        /* Get object */
        T obj = null;
        String id = in.getAttributeValue( null, ID_ATTRIBUTE );
        if ( id == null ) {
            throw new XMLStreamException( "Missing object ID." );
        }
        try {
            obj = getObject( Long.parseUnsignedLong( id ), guild );
        } catch ( NumberFormatException e ) {
            throw new XMLStreamException( "Invalid object ID.", e );
        }
        if ( obj == null ) {
            throw new XMLStreamException( "Could not get object." );
        }
        
        while ( in.next() == XMLStreamConstants.ATTRIBUTE ); // Move until end of tag.
        
        /* Check element ended properly */
        if ( ( in.getEventType() != XMLStreamConstants.END_ELEMENT ) ||
                !in.getLocalName().equals( getTag() ) ) {
            throw new XMLStreamException( "Did not find element end." );
        }
        
        return obj;

    }
    
    /**
     * Retrieves the tag that identifies the object.
     *
     * @return The object tag.
     */
    public abstract String getTag();
    
    /**
     * Retrieves the class that is translated.
     * 
     * @return The supported class.
     */
    public abstract Class<T> getTranslatedClass();
    
    /**
     * Retrieves the guild that the object is in.
     * <p>
     * If the guild is not necessary for obtaining the object from its ID (eg it can be obtained
     * directly from an <tt>IDiscordClient</tt>), <tt>null</tt> may be retrieved.
     *
     * @param obj The object to get the guild for.
     * @return The associated guild, or <tt>null</tt> if not necessary.
     */
    protected abstract IGuild getGuild( T obj );

    @Override
    public void write( XMLStreamWriter out, T instance ) throws XMLStreamException {

    	out.writeStartElement( getTag() );
        IGuild guild = getGuild( instance );
        if ( guild != null ) {
            out.writeAttribute( GUILD_ATTRIBUTE, Long.toUnsignedString( guild.getLongID() ) );
        }
        out.writeAttribute( ID_ATTRIBUTE, Long.toUnsignedString( instance.getLongID() ) );
        out.writeEndElement();
        
    }
    
    /**
     * Writes an instance, under the generic type, to an XML file.
     * 
     * @param out The stream to write to.
     * @param instance The instance to encode.
     * @throws XMLStreamException if an error was encountered while encoding.
     * @throws IllegalArgumentException if the instance is not of the subtype that this
     *                                  translator can handle.
     */
    @SuppressWarnings("unchecked")
	void writeGeneric( XMLStreamWriter out, IIDLinkedObject instance )
    		throws XMLStreamException, IllegalArgumentException {
    	
    	if ( !getTranslatedClass().isAssignableFrom( instance.getClass() ) ) {
    		throw new IllegalArgumentException( "Given instance is of the incorrect type." );
    	}
    	
    	write( out, (T) instance );
    	
    }
    
}
