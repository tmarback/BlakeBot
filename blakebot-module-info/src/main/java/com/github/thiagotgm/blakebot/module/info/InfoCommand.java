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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.obj.IMessage;

/**
 * Command that provides general information about the overall bot.
 * <p>
 * The bot info is obtained by processing a resource file named {@value #FILE_NAME}. The file is
 * processed by {@link InfoProcessor}, so it must use the expected encoding and may use supported
 * placeholders.<br>
 * The file is sent in a message preceded by <tt>```</tt> and followed by <tt>\n```</tt> (a code
 * block), so the first line of the file is used as the syntax highlighting (may be a blank line
 * for no syntax highlighting).
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-16
 */
@SuppressWarnings( "javadoc" )
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
            builder.append( InfoProcessor.process( infoStream ) ); // Process file.
            builder.append( "\n```" ); // End code block.
            
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
            usage = "{signature} [here]",
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
            usage = "{signature}",
            executeParent = true
            )
    public void modifier( CommandContext context ) {}

}
