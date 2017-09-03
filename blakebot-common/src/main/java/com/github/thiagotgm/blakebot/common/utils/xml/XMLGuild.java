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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

/**
 * XML wrapper for <tt>IGuild</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLGuild extends AbstractXMLIDLinkedObject<IGuild> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -2877808979476994881L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "guild";

    /**
     * Instantiates a wrapper with no guild.
     *
     * @param client Client to use to obtain guilds.
     */
    public XMLGuild( IDiscordClient client ) {
        
        super( client );
        
    }
    
    /**
     * Instantiates a wrapper with the given guild.
     *
     * @param client Client to use to obtain guilds.
     * @param guild The guild to wrap initially.
     */
    public XMLGuild( IDiscordClient client, IGuild guild ) {
        
        super( client, guild );
        
    }

    @Override
    protected IGuild getObject( long id, IGuild guild ) {

        return client.getGuildByID( id );
        
    }

    @Override
    public String getTag() {

        return TAG;
        
    }

    @Override
    protected IGuild getGuild() {

        return null;
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @param client The client the wrappers should use to obtain the objects.
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLGuild> newFactory( IDiscordClient client ) {
        
        return new Factory( client );
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLGuild> {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -2837445536215031771L;
        
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
        public XMLGuild newInstance() {

            return new XMLGuild( client );
            
        }
        
    }

}
