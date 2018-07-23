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
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * XML translator for <tt>IChannel</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLChannel extends AbstractXMLIDLinkedTranslator<IChannel> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 6859207705861601041L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "channel";

    /**
     * Instantiates a translator.
     *
     * @param client Client to use to obtain channels.
     */
    public XMLChannel( IDiscordClient client ) {
        
        super( client );
        
    }

    @Override
    protected IChannel getObject( long id, IGuild guild ) {

        return client.getChannelByID( id );
        
    }

    @Override
    public String getTag() {

        return TAG;
        
    }
    
    @Override
    public Class<IChannel> getTranslatedClass() {
    	
    	return IChannel.class;
    	
    }

    @Override
    protected IGuild getGuild( IChannel obj ) {

        return null;
        
    }

}
