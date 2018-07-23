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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IWebhook;

/**
 * XML translator for <tt>IWebhook</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLWebhook extends AbstractXMLIDLinkedTranslator<IWebhook> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = -2652556526120576152L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "webhook";

    /**
     * Instantiates a translator.
     *
     * @param client Client to use to obtain webhook.
     */
    public XMLWebhook( IDiscordClient client ) {
        
        super( client );
        
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
    public Class<IWebhook> getTranslatedClass() {
    	
    	return IWebhook.class;
    	
    }

    @Override
    protected IGuild getGuild( IWebhook obj ) {

        return obj.getGuild();
        
    }
   
}
