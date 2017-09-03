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
import com.github.thiagotgm.blakebot.common.utils.XMLElement;
import com.github.thiagotgm.blakebot.common.utils.XMLWrapper;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * XML wrapper for <tt>IIDLinkedObject</tt>s.
 * The actual reading and writing is delegated to a wrapper
 * of the IIDLinkedObject subtype, so the supported IIDLinkedObjects are the ones that
 * have their own XML wrappers.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLIDLinkedObject extends AbstractXMLWrapper<IIDLinkedObject> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 6752712491496563108L;
    
    /**
     * Client to use for obtaining objects from their IDs.
     */
    protected final IDiscordClient client;
    
    /**
     * Instantiates a wrapper with no object.
     *
     * @param client The client to use for obtaining objects.
     */
    public XMLIDLinkedObject( IDiscordClient client ) {
        
        this( client, null );
        
    }
    
    /**
     * Instantiates a wrapper with the given object.
     *
     * @param client The client to use for obtaining objects.
     * @param obj The object to wrap initially.
     */
    public XMLIDLinkedObject( IDiscordClient client, IIDLinkedObject obj ) {
        
        super( obj );
        this.client = client;
        
    }

    @Override
    public void read( XMLStreamReader in ) throws XMLStreamException {

        if ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Stream not in start element." );
        }
        
        XMLWrapper<? extends IIDLinkedObject> wrapper;
        switch ( in.getLocalName() ) {
            
            case XMLVoiceChannel.TAG:
                wrapper = new XMLVoiceChannel( client );
                break;
                
            case XMLChannel.TAG:
                wrapper = new XMLChannel( client );
                break;
                
            case XMLEmoji.TAG:
                wrapper = new XMLEmoji( client );
                break;
                
            case XMLGuild.TAG:
                wrapper = new XMLGuild( client );
                break;
                
            case XMLMessage.TAG:
                wrapper = new XMLMessage( client );
                break;
                
            case XMLRole.TAG:
                wrapper = new XMLRole( client );
                break;
                
            case XMLUser.TAG:
                wrapper = new XMLUser( client );
                break;
                
            case XMLWebhook.TAG:
                wrapper = new XMLWebhook( client );
                break;
                
            default:
                throw new XMLStreamException( "Unrecognized local name." );
            
        }
        wrapper.read( in );
        setObject( wrapper.getObject() );
        
    }

    @Override
    public void write( XMLStreamWriter out ) throws XMLStreamException, IllegalStateException {

        XMLWrapper<? extends IIDLinkedObject> wrapper;
        IIDLinkedObject obj = getObject();
        if ( obj == null ) {
            throw new IllegalStateException( "No wrapped object to write." );
        }
        
        if ( obj instanceof IVoiceChannel ) {
            wrapper = new XMLVoiceChannel( client, (IVoiceChannel) obj );
        } else if ( obj instanceof IChannel ) {
            wrapper = new XMLChannel( client, (IChannel) obj );
        } else if ( obj instanceof IEmoji ) {
            wrapper = new XMLEmoji( client, (IEmoji) obj );
        } else if ( obj instanceof IGuild ) {
            wrapper = new XMLGuild( client, (IGuild) obj );
        } else if ( obj instanceof IMessage ) {
            wrapper = new XMLMessage( client, (IMessage) obj );
        } else if ( obj instanceof IRole ) {
            wrapper = new XMLRole( client, (IRole) obj );
        } else if ( obj instanceof IUser ) {
            wrapper = new XMLUser( client, (IUser) obj );
        } else if ( obj instanceof IWebhook ) {
            wrapper = new XMLWebhook( client, (IWebhook) obj );
        } else {
            throw new XMLStreamException( "Unsupported type of IDLinkedObject." );
        }
        
        wrapper.write( out );
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @param client The client the wrappers should use to obtain the objects.
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLIDLinkedObject> newFactory( IDiscordClient client ) {
        
        return new Factory( client );
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLIDLinkedObject> {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -1185141972303969323L;
        
        private final IDiscordClient client;
        
        /**
         * Creates an instance that produces wrappers that use the given client 
         *
         * @param client The client the wrappers should use to obtain the objects.
         */
        public Factory( IDiscordClient client ) {
            
            this.client = client;
            
        }

        @Override
        public XMLIDLinkedObject newInstance() {

            return new XMLIDLinkedObject( client );
            
        }
        
    }

}
