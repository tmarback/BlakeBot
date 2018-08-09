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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;

/**
 * XML translator for <tt>IEmoji</tt> objects.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-02
 */
public class XMLEmoji extends AbstractXMLIDLinkedTranslator<IEmoji> {
    
    /**
     * UID that represents this class.
     */
    private static final long serialVersionUID = 3083611487718069855L;
    
    /**
     * Local name of the XML element.
     */
    public static final String TAG = "emoji";

    /**
     * Instantiates a translator.
     *
     * @param client Client to use to obtain emojis.
     */
    public XMLEmoji( IDiscordClient client ) {
        
        super( client );
        
    }

    @Override
    protected IEmoji getObject( long id, IGuild guild ) {

        return guild.getEmojiByID( id );
        
    }

    @Override
    public String getTag() {

        return TAG;
        
    }
    
    @Override
    public Class<IEmoji> getTranslatedClass() {
    	
    	return IEmoji.class;
    	
    }

    @Override
    protected IGuild getGuild( IEmoji obj ) {

        return obj.getGuild();
        
    }

}
