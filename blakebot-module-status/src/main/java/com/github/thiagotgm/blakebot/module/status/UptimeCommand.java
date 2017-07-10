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
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.thiagotgm.blakebot.Bot;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that displays how long the bot has been connected to Discord.
 * 
 * @author ThiagoTGM
 * @version 1.2
 * @since 2017-01-01
 */
public class UptimeCommand {
    
    private static final String NAME = "Uptime";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = { "uptime", "up" },
            description = "Displays how long the bot has been up.",
            usage = StatusModule.PREFIX + "uptime|up"
    )
    public void uptimeCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.appendField( "Connection uptime", Bot.getInstance().getUptime().toString(), false );
                embedBuilder.withColor( Color.RED );
                msgBuilder.withEmbed( embedBuilder.build() ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }

}
