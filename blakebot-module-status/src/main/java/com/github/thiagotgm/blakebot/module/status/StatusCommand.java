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

package com.github.thiagotgm.blakebot.module.status;

import java.awt.Color;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that displays advanced bot information.
 * 
 * @author ThiagoTGM
 * @version 2.1
 * @since 2017-01-11
 */
public class StatusCommand {
    
    private static final String NAME = "Status";
    
    @MainCommand(
            name = NAME,
            aliases = "status",
            description = "retrieves advanced information on bot status",
            usage = "{}status",
            requiresOwner = true
    )
    public void statusCommand( CommandContext context ) {
        
        /* Gets status values */
        IDiscordClient client = context.getEvent().getClient();
        int channelAmount = client.getChannels().size();
        int serverAmount = client.getGuilds().size();
        
        /* Structures status message */
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.appendField( "Public channels", channelAmount + " channels", false );
        embedBuilder.appendField( "Servers", serverAmount + " servers", false );
        embedBuilder.withColor( Color.RED );
        
        /* Sends status message */ 
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();
        
    }

}
