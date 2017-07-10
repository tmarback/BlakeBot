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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that makes a square out of a string.
 *
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-05
 */
public class SquareCommand {
    
    private static final String NAME = "Square";
    private static final Logger log = LoggerFactory.getLogger( SquareCommand.class );
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME,
            alias = "square",
            description = "Makes a square with a given message.",
            usage = FunModule.PREFIX + "square <message>"
    )
    public void squareCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        /* Obtains full message w/o spaces. */
        String message = "";
        for ( String word : args ) {
            
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
        final String output = builder.toString();
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( output ).build();
            } catch ( MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            } catch ( DiscordException e ) {
                log.warn( "Exceeded maximum amount of characters.", e );
                try {
                    msgBuilder.withContent( "\u200BSorry, too many characters!" ).build();
                } catch ( DiscordException | MissingPermissionsException e1 ) {
                    log.error( "Could not send failure message.", e1 );
                }
            }
            
        });      

    }

}
