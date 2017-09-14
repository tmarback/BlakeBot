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

package com.github.thiagotgm.blakebot.module.fun;

import java.io.InputStream;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

/**
 * Command that shows the "Stop it. Get some help." meme.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-14
 */
public class GetSomeHelpCommand {

    @MainCommand(
            name = "Stop it. Get some help.",
            aliases = "getHelp",
            description = "Lets someone know they went too far.",
            usage = "{}getHelp"
            )
    public void getSomeHelp( CommandContext context ) {
        
        InputStream imageStream = getClass().getResourceAsStream( "/gifs/GetSomeHelp.gif" );
        context.getReplyBuilder().withFile( imageStream, "GetSomeHelp.gif" ).send();
        
    }

}
