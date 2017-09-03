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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.thiagotgm.blakebot.common.utils.AbstractXMLWrapper;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;

/**
 * Shared implementation for wrappers for objects with an ID.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 * @param <T> Type of object being wrapped.
 */
abstract class AbstractXMLIDLinkedObject<T extends IIDLinkedObject> extends AbstractXMLWrapper<T> {
    
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
     * Instantiates a wrapper with no object.
     *
     * @param client The client to use for obtaining objects.
     */
    public AbstractXMLIDLinkedObject( IDiscordClient client ) {
        
        this( client, null );
        
    }
    
    /**
     * Instantiates a wrapper with the given object.
     *
     * @param client The client to use for obtaining objects.
     * @param obj The object to wrap initially.
     */
    public AbstractXMLIDLinkedObject( IDiscordClient client, T obj ) {
        
        super( obj );
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
    
    /**
     * Reads only the start element from the XML stream.
     * <p>
     * The object will already be read, but will not reach the end element in
     * the stream yet. Advancing the stream until the end element is then responsibility
     * of the caller.
     *
     * @param in The stream to read data from.
     * @throws XMLStreamException if an error occurred.
     */
    public void readStart( XMLStreamReader in ) throws XMLStreamException {
        
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
          setObject( obj );
        
    }
    
    /**
     * Reads the stream until the end of the element.
     * <p>
     * The start element is expected to already have been read, and no data is expected
     * to be found.
     *
     * @param in The stream to read data from.
     * @throws XMLStreamException if an error occurred.
     */
    public void readEnd( XMLStreamReader in ) throws XMLStreamException {
        
        while ( in.hasNext() ) {
            
            switch ( in.next() ) {
                
                case XMLStreamConstants.START_ELEMENT:
                    throw new XMLStreamException( "Unexpected subelement found." );
                    
                case XMLStreamConstants.CHARACTERS:
                    throw new XMLStreamException( "Unexpected character data found." );
                
                case XMLStreamConstants.END_ELEMENT:
                    if ( in.getLocalName().equals( getTag() ) ) {
                        return;
                    } else {
                        throw new XMLStreamException( "Unexpected closing tag found." );
                    }
                
            }
            
        }
        
        throw new XMLStreamException( "Unexpected end of document encountered." );
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        readStart( in );
        readEnd( in );
        
    }
    
    /**
     * Retrieves the tag that identifies the object.
     *
     * @return The object tag.
     */
    public abstract String getTag();
    
    /**
     * Retrieves the guild that the object is in.
     * <p>
     * If the guild is not necessary for obtaining the object from its ID (eg it can be obtained
     * directly from an <tt>IDiscordClient</tt>), <tt>null</tt> may be retrieved.
     *
     * @return The associated guild, or <tt>null</tt> if not necessary.
     */
    protected abstract IGuild getGuild();
    
    /**
     * Writes only the start element to the XML stream.
     * <p>
     * Eventually closing the element (using {@link XMLStreamWriter#writeEndElement()}) is
     * responsibility of the caller.
     *
     * @param out The stream to write data to.
     * @throws XMLStreamException if an error occurred while writing.
     * @throws IllegalStateException if there is no object currently wrapped.
     */
    public void writeStart( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {
        
        if ( getObject() == null ) {
            throw new IllegalStateException( "No object currently wrapped." );
        }
        
        out.writeStartElement( getTag() );
        IGuild guild = getGuild();
        if ( guild != null ) {
            out.writeAttribute( GUILD_ATTRIBUTE, Long.toUnsignedString( guild.getLongID() ) );
        }
        out.writeAttribute( ID_ATTRIBUTE, Long.toUnsignedString( getObject().getLongID() ) );
        
    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {

        writeStart( out );
        out.writeEndElement();
        
    }
    
}
