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
import sx.blah.discord.handle.obj.IUser;

/**
 * XML wrapper for <tt>IUser</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLUser extends AbstractXMLIDLinkedObject<IUser> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 5802320658423834230L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "user";

    /**
     * Instantiates a wrapper with no user.
     *
     * @param client Client to use to obtain users.
     */
    public XMLUser( IDiscordClient client ) {
        
        super( client );
        
    }
    
    /**
     * Instantiates a wrapper with the given user.
     *
     * @param client Client to use to obtain users.
     * @param user The user to wrap initially.
     */
    public XMLUser( IDiscordClient client, IUser user ) {
        
        super( client, user );
        
    }

    @Override
    protected IUser getObject( long id, IGuild guild ) {

        return client.fetchUser( id );
        
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
    public static XMLElement.Factory<XMLUser> newFactory( IDiscordClient client ) {
        
        return new Factory( client );
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLUser> {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -1744154067972925265L;
        
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
        public XMLUser newInstance() {

            return new XMLUser( client );
            
        }
        
    }

}
