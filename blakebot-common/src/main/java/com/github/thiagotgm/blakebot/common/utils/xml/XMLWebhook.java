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
import sx.blah.discord.handle.obj.IWebhook;

/**
 * XML wrapper for <tt>IWebhook</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLWebhook extends AbstractXMLIDLinkedObject<IWebhook> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -2652556526120576152L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "webhook";

    /**
     * Instantiates a wrapper with no webhook.
     *
     * @param client Client to use to obtain webhook.
     */
    public XMLWebhook( IDiscordClient client ) {
        
        super( client );
        
    }
    
    /**
     * Instantiates a wrapper with the given webhook.
     *
     * @param client Client to use to obtain webhook.
     * @param hook The webhook to wrap initially.
     */
    public XMLWebhook( IDiscordClient client, IWebhook hook ) {
        
        super( client, hook );
        
    }

    @Override
    protected IWebhook getObject( long id, IGuild guild ) {

        return guild.getWebhookByID( id );
        
    }

    @Override
    public String getTag() {

        return TAG;
        
    }

    @Override
    protected IGuild getGuild() {

        return getObject().getGuild();
        
    }
    
    /**
     * Creates a factory that produces instances of this class.
     *
     * @param client The client the wrappers should use to obtain the objects.
     * @return A new factory.
     */
    public static XMLElement.Factory<XMLWebhook> newFactory( IDiscordClient client ) {
        
        return new Factory( client );
        
    }
    
    /**
     * Factory for new instances of the class.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-29
     */
    private static class Factory implements XMLElement.Factory<XMLWebhook> {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = 1188776855099613012L;
        
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
        public XMLWebhook newInstance() {

            return new XMLWebhook( client );
            
        }
        
    }

}
