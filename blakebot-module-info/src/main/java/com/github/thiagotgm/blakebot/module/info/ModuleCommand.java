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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuilder;

/**
 * Command that display information about installed modules and their commands.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-15
 */
public class ModuleCommand {
    
    private static final String HERE_MODIFIER = "Local Module Command";
    private static final String FAILURE_HANDLER = "failure";
    
    private static final String BLOCK_FORMAT = "```%s\n%s\n```";
    private static final String NO_HIGHLIGHT_FORMAT = String.format( BLOCK_FORMAT, "", "%s" );
    private static final int LIST_BLOCK_MAX =
            IMessage.MAX_MESSAGE_LENGTH - String.format( NO_HIGHLIGHT_FORMAT, "" ).length();
    private static final String LIST_HEADER = "===[MODULE LIST]===\nTo display the information of a "
            + "specific module, use this command followed by the module name.";
    private static final String LIST_ITEM = "[%s]\n(version %s)\n%s";
    private static final String INFO_HEADER = "[%s]\n%s\nVersion %s\n\n%s";
    
    @MainCommand(
            name = "Module Command",
            aliases = "module",
            description = "Shows the modules installed on this bot. If a module is specified "
                    + "as an argument (using the name shown in the module list), shows the "
                    + "information of that module.",
            usage = "{}module [module]",
            replyPrivately = true,
            subCommands = HERE_MODIFIER,
            failureHandler = FAILURE_HANDLER
            )
    public boolean module( CommandContext context ) {
        
        List<String> blocks;
        if ( context.getArgs().isEmpty() ) { // No args.
            blocks = formatList( ModuleInfoManager.getInfos() ); // Show module list.
        } else { // Arg given. Find module with the arg alias.
            ModuleInfo info = ModuleInfoManager.getInfo( context.getArgs().get( 0 ) );
            if ( info == null ) {
                return false; // Invalid module alias.
            }
            blocks = formatInfoLong( info ); // Show module info.
        }
        
        MessageBuilder reply = context.getReplyBuilder();
        RequestBuilder request = new RequestBuilder( context.getEvent().getClient() ).setAsync( false )
                .shouldBufferRequests( true ).doAction( () -> { return true; } );
        blocks.stream().forEachOrdered( ( block ) -> {
            
            request.andThen( () -> {  // Add block to execution queue.
                
                reply.withContent( block ).build();
                return true;
                
            });
            
        });
        request.execute(); // Send all blocks in order.
        return true;
        
    }
    
    /**
     * Formats a collection of information instances, in short format, for sending in Discord messages.
     * <p>
     * Each block is to be sent in a separate message.
     * 
     * @param infos The colletion of information instances to be formatted.
     * @return The list of blocks containing the short-format of the given informations.
     */
    private static List<String> formatList( Collection<ModuleInfo> infos ) {
        
        List<String> blocks = new LinkedList<>(); // Create header.
        blocks.add( String.format( NO_HIGHLIGHT_FORMAT, LIST_HEADER ) );
        
        Iterator<ModuleInfo> iter = infos.iterator(); // First block starts with first info.
        StringBuilder builder = new StringBuilder( formatInfoShort( iter.next() ) );
        while ( iter.hasNext() ) {
            
            String next = formatInfoShort( iter.next() );
            if ( builder.length() + 2 + next.length() > LIST_BLOCK_MAX ) { // Finished block.
                blocks.add( String.format( NO_HIGHLIGHT_FORMAT, builder.toString() ) );
                builder = new StringBuilder( next ); // Start another block.
            } else { // Still fits in current block.
                builder.append( "\n\n" );
                builder.append( next );
            }
            
        }
        blocks.add( String.format( NO_HIGHLIGHT_FORMAT, builder.toString() ) );
        
        return blocks;
        
    }
    
    /**
     * Formats the short version of a module information.
     * <p>
     * Includes only alias, version, and description.
     * 
     * @param info The information to be formatted.
     * @return The short format of the information.
     */
    private static String formatInfoShort( ModuleInfo info ) {
        
        return String.format( LIST_ITEM, info.getAlias(), info.getVersion(), info.getDescription() );
        
    }
    
    /**
     * Formats the long version of a module information.
     * <p>
     * Includes alias, name, version, description, and info blocks, using the info's specified
     * syntax highlighting (if any) for the info blocks.
     * <p>
     * Each block returned should be sent in a separate message.
     * 
     * @param info The information to format.
     * @return The full information, in blocks.
     */
    private static List<String> formatInfoLong( ModuleInfo info ) {
        
        List<String> blocks = new LinkedList<>();
        String header = String.format( INFO_HEADER, info.getAlias(), info.getName(),
                info.getVersion(), info.getDescription() ); // Create header.
        blocks.add( String.format( NO_HIGHLIGHT_FORMAT, header ) );
        
        for ( String infoBlock : info.getInfo() ) { // Add each info block.
            
            blocks.add( String.format( BLOCK_FORMAT, info.getHighlight(), infoBlock ) );
            
        }
        
        return blocks;
        
    }
    
    @SubCommand(
            name = HERE_MODIFIER,
            aliases = "here",
            description = "Sends the requested information to the channel where "
                    + "the command was called, instead of always sending a private message.",
            usage = "{}module here [module]",
            executeParent = true,
            failureHandler = FAILURE_HANDLER
            )
    public void modifier( CommandContext context ) {}
    
    @FailureHandler( FAILURE_HANDLER )
    public void failure( CommandContext context, FailureReason reason ) {
        
        if ( reason == FailureReason.COMMAND_OPERATION_FAILED ) {
            context.getReplyBuilder().withContent( "Sorry, I don't recognize that module." ).build();
        }
        
    }

}
