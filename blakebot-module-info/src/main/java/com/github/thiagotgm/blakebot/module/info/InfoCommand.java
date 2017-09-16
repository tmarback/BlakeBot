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

package com.github.thiagotgm.blakebot.module.info;

import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.obj.IMessage;

/**
 * Command that provides general information about the overall bot.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-16
 */
public class InfoCommand {
    
    /**
     * Expected name of info file.
     */
    public static final String FILE_NAME = "bot.info";
    private static final String HERE_MODIFIER = "Local Info Command";
    
    private static final Logger LOG = LoggerFactory.getLogger( InfoCommand.class );
    
    private static final String INFO;
    private static final String ERROR = "```\nERROR\n```";
    
    static { // Load bot info.
        
        LOG.info( "Loading bot info file." );
        InputStream infoStream = InfoCommand.class.getClassLoader().getResourceAsStream( FILE_NAME );
        if ( infoStream != null ) {
            StringBuilder builder = new StringBuilder( "```" ); // Start code block.
            Scanner scan = new Scanner( infoStream );
            while ( scan.hasNextLine() ) { // Load each line of the info file.
                
                builder.append( scan.nextLine() );
                builder.append( '\n' );
                
            }
            builder.append( "\n```" ); // End code block.
            scan.close();
            
            if ( builder.length() <= IMessage.MAX_MESSAGE_LENGTH ) { // Ensure info fits
                INFO = builder.toString();                           // in a message.
                LOG.info( "Finished loading bot info file." );
            } else {
                LOG.error( "Bot info file too long." );
                INFO = ERROR;
            }
        } else {
            LOG.error( "Could not find bot info file." );
            INFO = ERROR;
        }
        
    }
    
    @MainCommand(
            name = "Bot information",
            aliases = "info",
            description = "Shows information about the bot.",
            usage = "{}info [here]",
            replyPrivately = true,
            subCommands = HERE_MODIFIER
            )
    public void info( CommandContext context ) {
        
        context.getReplyBuilder().withContent( INFO ).build();
        
    }
    
    @SubCommand(
            name = HERE_MODIFIER,
            aliases = "here",
            description = "Sends the information to the channel where the command was called, "
                    + "instead of always sending a private message.",
            usage = "{}info here",
            executeParent = true
            )
    public void modifier( CommandContext context ) {}

}
