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
    private static final Logger log = LoggerFactory.getLogger( SquareCommand.class );
    
    @MainCommand(
            name = NAME,
            aliases = "square",
            description = "Makes a square with a given message.",
            usage = "{}square <message>",
            failureHandler = FAILURE_HANDLER
    )
    public void squareCommand( CommandContext context ) {
        
        /* Obtains full message w/o spaces. */
        String message = "";
        for ( String word : context.getArgs() ) {
            
            message += word;
            
        }
        
        /* Makes first line of square. */
        StringBuilder builder = new StringBuilder( "`\u200B " );
        for ( char letter : message.toCharArray() ) {
            
            builder.append( letter + " " );
            
        }
        builder.append( "\u200B`\n" );
        
        /* Makes middle lines of square. */
        int spaceSize = ( message.length() * 2 ) - 3; // Space size is double the string size (every char has a space
                                                      // after, excluding the first char, the last char, and the
                                                      // space after last char.
        for ( int i = 1, j = message.length() - 2; j > 0; i++, j-- ) {
            
            builder.append( "`\u200B " + message.charAt( i ) );
            for ( int k = 0; k < spaceSize; k++ ) {
                
                builder.append( ' ' );
                
            }
            builder.append( message.charAt( j ) + " \u200B`\n" );
            
        }
        
        /* Makes last line of square. */
        builder.append( "`\u200B " );
        for ( int i = message.length() - 1; i >= 0; i-- ) {
            
            builder.append( message.charAt( i ) + " " );
            
        }
        builder.append( "\u200B`" );
        
        /* Outputs the message. */
        context.getReplyBuilder().withContent( builder.toString() ).build();  

    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void failureHandler( CommandContext context, FailureReason reason ) {
        
        if ( reason == FailureReason.DISCORD_ERROR ) {
            log.warn( "Exceeded maximum amount of characters." );
            context.getReplyBuilder().withContent( "\u200BSorry, too many characters!" ).build();
        }
        
    }

}
