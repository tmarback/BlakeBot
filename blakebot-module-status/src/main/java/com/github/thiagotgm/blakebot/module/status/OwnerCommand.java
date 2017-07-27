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
import com.github.thiagotgm.blakebot.Bot;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that displays the owner of the bot account.
 * 
 * @author ThiagoTGM
 * @version 1.1
 * @since 2017-01-01
 */
public class OwnerCommand {
    
    private static final String NAME = "Owner";
    private static final String FAILURE_HANDLER = "handler";
    
    @MainCommand(
            name = NAME,
            aliases = "owner",
            description = "Displays the information of the owner of this bot account.",
            usage = "{}owner",
            failureHandler = FAILURE_HANDLER
    )
    public void ownerCommand( CommandContext context ) {
        
        String name;
        String nick;
        String image;
        Bot bot = Bot.getInstance();
        name = bot.getOwner();
        nick = bot.getOwner( context.getMessage().getGuild() );
        image = bot.getOwnerImage();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withThumbnail( image );
        embedBuilder.appendField( "Username", name, false );
        if ( nick == null ) {
            nick = "None found in this server";
        }
        embedBuilder.appendField( "Nickname", nick, false );
        embedBuilder.withColor( Color.RED );
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();

    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void handler( CommandContext context, FailureReason reason ) {
        
        if ( reason == FailureReason.DISCORD_ERROR ) {
            context.getReplyBuilder().withContent( "\u200BSorry, I could not retrieve my owner's data." );
        }
        
    }

}
