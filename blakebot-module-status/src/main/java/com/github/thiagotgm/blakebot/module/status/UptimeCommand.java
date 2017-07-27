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
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.util.EmbedBuilder;

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
            name = NAME,
            aliases = { "uptime", "up" },
            description = "Displays how long the bot has been up.",
            usage = "{}uptime|up"
    )
    public void uptimeCommand( CommandContext context ) {
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.appendField( "Connection uptime", Bot.getInstance().getUptime().toString(), false );
        embedBuilder.withColor( Color.RED );
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();

    }

}
