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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.util.MessageBuilder;

/**
 * Command that makes a square out of a string.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-05
 */
public class SquareCommand {
    
    private static final String NAME = "Square";
    private static final String FAILURE_HANDLER = "handler";
    private static final Logger LOG = LoggerFactory.getLogger( SquareCommand.class );
    
    @MainCommand(
            name = NAME,
            aliases = "square",
            description = "Makes a square with a given message.",
            usage = "{}square <message>",
            failureHandler = FAILURE_HANDLER
    )
    public boolean squareCommand( CommandContext context ) {
        
        /* Obtains full message w/o spaces. */
        if ( context.getArgs().isEmpty() ) {
            return false;
        }
        String message = context.getArgs().get( 0 ).replaceAll( "\\s", "" );
        
        /* Makes first line of square. */
        StringBuilder builder = new StringBuilder( "```\n " );
        for ( char letter : message.toCharArray() ) {
            
            builder.append( letter + " " );
            
        }
        builder.append( "\n" );
        
        /* Makes middle lines of square. */
        int spaceSize = ( message.length() * 2 ) - 3; // Space size is double the string size (every char has a space
                                                      // after, excluding the first char, the last char, and the
                                                      // space after last char.
        for ( int i = 1, j = message.length() - 2; j > 0; i++, j-- ) {
            
            builder.append( " " + message.charAt( i ) );
            for ( int k = 0; k < spaceSize; k++ ) {
                
                builder.append( ' ' );
                
            }
            builder.append( message.charAt( j ) + " \n" );
            
        }
        
        /* Makes last line of square. */
        builder.append( " " );
        for ( int i = message.length() - 1; i >= 0; i-- ) {
            
            builder.append( message.charAt( i ) + " " );
            
        }
        builder.append( " \n```" );
        
        /* Outputs the message. */
        context.getReplyBuilder().withContent( builder.toString() ).build();  
        return true;

    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void failureHandler( CommandContext context, FailureReason reason ) {
        
        MessageBuilder builder = context.getReplyBuilder();
        if ( reason == FailureReason.DISCORD_ERROR ) {
            LOG.warn( "Exceeded maximum amount of characters." );
            builder.withContent( "Sorry, too many characters!" );
        } else if ( reason == FailureReason.COMMAND_OPERATION_FAILED ) {
            builder.withContent( "Please provide a message." );
        } else {
            return; // Do nothing.
        }
        builder.build();
        
    }

}
