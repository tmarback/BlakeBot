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
 * Command that displays the owner of the bot account.
 * 
 * @author ThiagoTGM
 * @version 1.1
 * @since 2017-01-01
 */
public class OwnerCommand {
    
    private static final String NAME = "Owner";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = "owner",
            description = "Displays the information of the owner of this bot account.",
            usage = StatusModule.PREFIX + "owner"
    )
    public void ownerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            String name;
            String nick;
            String image;
            Bot bot = Bot.getInstance();
            try {
                name = bot.getOwner();
                nick = bot.getOwner( event.getMessage().getGuild() );
                image = bot.getOwnerImage();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.withThumbnail( image );
                embedBuilder.appendField( "Username", name, false );
                if ( nick == null ) {
                    nick = "None found in this server";
                }
                embedBuilder.appendField( "Nickname", nick, false );
                embedBuilder.withColor( Color.RED );
                msgBuilder.withEmbed( embedBuilder.build() );
            } catch ( DiscordException e ) {
                msgBuilder.withContent( "\u200BSorry, I could not retrieve my owner's data." );
            }
            try {
                msgBuilder.build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });

    }

}
