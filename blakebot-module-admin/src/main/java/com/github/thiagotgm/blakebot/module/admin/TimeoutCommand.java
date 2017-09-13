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

package com.github.thiagotgm.blakebot.module.admin;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;

/**
 * Class with commands that timeout a user from a channel or guild, or reverse that
 * timeout.
 *
 * @version 0.1
 * @author ThiagoTGM
 * @since 2017-03-07
 */
public class TimeoutCommand {
    
    private static final String TIMEOUT_NAME = "Timeout";
    private static final String UNTIMEOUT_NAME = "Untimeout";
    private static final String SERVER_MODIFIER = "Un/Timeout Server";
    private static final String CHECK_NAME = "Check Timeout";
    
    private static final Pattern TIME_PATTERN = Pattern.compile( "(\\d)([smh])" );
    private static final String NOT_USER_ERROR = "Argument \"%s\" not an user.";
    
    private final TimeoutController controller;
    
    /**
     * Creates a new instance of this class.
     */
    public TimeoutCommand() {
        
        controller = TimeoutController.getInstance();
        
    }
    
    @MainCommand(
            name = TIMEOUT_NAME,
            aliases = { "timeout", "to" },
            description = "Times out a user out for the specified time. The time should be in the format #u, "
                    + "where # is a postive nonzero integer number, and 'u' is the unit of time, where the "
                    + "unit of time can be 's' (seconds), 'm' (minutes), or 'h' (hours). Timeout is "
                    + "channel-wide unless the \"server\" modifier is used.",
            usage = "{}timeout|to [server] <time> <user> [user]...",
            subCommands = SERVER_MODIFIER,
            requiredPermissions = Permissions.MANAGE_MESSAGES
    )
    public void timeoutCommand( CommandContext context ) {
        
        List<Argument> args = context.getArguments();
        MessageBuilder reply = context.getReplyBuilder();
        
        if ( args.size() < 2 ) { // Checks minimum amount of arguments.
            reply.withContent( "Please specify a time and the user(s) to be timed out." ).build();
            return;
        }
        
        /* Parse time */
        
        Matcher matcher = TIME_PATTERN.matcher( args.get( 0 ).getText() );
        if ( !matcher.matches() ) {
            reply.withContent( "Invalid time argument." ).build();
            return;
        }
        
        long timeout;
        try { // Obtain time amount.
            timeout = Long.parseLong( matcher.group( 1 ) );
        } catch ( NumberFormatException e1 ) {
            reply.withContent( "Invalid time amount." ).build();
            return;
        }
        if ( timeout <= 0 ) {
            reply.withContent( "Time must be larger than 0." ).build();
            return;
        }
        
        TimeUnit unit; // Obtain time unit.
        switch ( matcher.group( 2 ).charAt( 0 ) ) {
            
            case 's':
                unit = TimeUnit.SECONDS;
                break;
                
            case 'm':
                unit = TimeUnit.MINUTES;
                break;
                
            case 'h':
                unit = TimeUnit.HOURS;
                break;
                
            default:
                reply.withContent( "Invalid time unit." ).build();
                return;
            
        }
        
        timeout = unit.toMillis( timeout ); // Convert to milliseconds.
        
        /* Apply timeout to each user */
        
        boolean serverScope = context.getCommand().getName().equals( SERVER_MODIFIER );
        String scope = serverScope ? "server" : "channel";
        List<String> replies = new LinkedList<>();
        
        for ( Argument arg : args.subList( 1, args.size() ) ) {
            
            String message;
            if ( arg.getType() == Argument.Type.USER_MENTION ) {
                IUser user = (IUser) arg.getArgument();
                boolean success;
                if ( serverScope ) { // Apply on server-scope.
                    success = controller.timeout( user, context.getGuild(), timeout );
                } else { // Apply on channel-scope.
                    success = controller.timeout( user, context.getChannel(), timeout );
                }
                String format = success ? "Timed out %s in this %s." : "%s is already timed out in this %s.";
                message = String.format( format, user.mention(), scope );
            } else { // Argument not a user mention.
                message = String.format( NOT_USER_ERROR, arg.getText() );
            }
            replies.add( message );
            
        }
        
        reply.withContent( String.join( "\n", replies ) ).build();
        
    }
    
    @MainCommand(
            name = UNTIMEOUT_NAME,
            aliases = { "untimeout", "uto" },
            description = "Lifts the timeout placed on the given users. Removes channel-wide timeouts "
                    + "unless the \"server\" modifier is used.",
            usage = "{}untimeout|uto [server] <user> [user]...",
            subCommands = SERVER_MODIFIER,
            requiredPermissions = Permissions.MANAGE_MESSAGES
    )
    public void untimeoutCommand( CommandContext context ) {
        
        List<Argument> args = context.getArguments();
        MessageBuilder reply = context.getReplyBuilder();
        
        if ( args.size() < 1 ) { // Checks minimum amount of arguments.
            reply.withContent( "Please specify the user(s) to be un-timed out." ).build();
            return;
        }
        
        /* Lift timeout from each user */
        
        boolean serverScope = context.getCommand().getName().equals( SERVER_MODIFIER );
        String scope = serverScope ? "server" : "channel";
        List<String> replies = new LinkedList<>();
        
        for ( Argument arg : args.subList( 1, args.size() ) ) {
            
            String message;
            if ( arg.getType() == Argument.Type.USER_MENTION ) {
                IUser user = (IUser) arg.getArgument();
                boolean success;
                if ( serverScope ) { // Apply on server-scope.
                    success = controller.untimeout( user, context.getGuild() );
                } else { // Apply on channel-scope.
                    success = controller.untimeout( user, context.getChannel() );
                }
                String format = success ? "Lifted timeout for %s in this %s." :
                                          "%s is not timed out in this %s.";
                message = String.format( format, user.mention(), scope );
            } else { // Argument not a user mention.
                message = String.format( NOT_USER_ERROR, arg.getText() );
            }
            replies.add( message );
            
        }
        
        reply.withContent( String.join( "\n", replies ) ).build();
        
    }
    
    @SubCommand(
            name = SERVER_MODIFIER,
            aliases = "server",
            description = "Performs the command on a server-wide scope instead of channel-wide.",
            usage = "{}timeout|to|untimeout|uto server <user> [user]...",
            requiredGuildPermissions = Permissions.MANAGE_MESSAGES,
            requiresParentPermissions = false,
            executeParent = true
    )
    public void serverSubCommand( CommandContext context ) {}
    
    @MainCommand(
            name = CHECK_NAME,
            aliases = { "istimedout", "isto" },
            description = "Checks if the given users are timed out on the current channel "
                    + "and/or server.",
            usage = "{}istimeout|isto <user> [user]..."
    )
    public void checkCommand( CommandContext context ) {

        List<Argument> args = context.getArguments();
        MessageBuilder reply = context.getReplyBuilder();

        if ( args.size() < 1 ) { // Checks minimum amount of arguments.
            reply.withContent( "Please specify the user(s) to be checked." ).build();
            return;
        }

        /* Check timeout for each user */

        List<String> replies = new LinkedList<>();

        for ( Argument arg : args ) {

            String message;
            if ( arg.getType() == Argument.Type.USER_MENTION ) {
                IUser user = (IUser) arg.getArgument();
                message = String.format( "%s: %stimed out on channel, %stimed out on server.", user.mention(),
                        controller.hasTimeout( user, context.getChannel() ) ? "" : "not ",
                        controller.hasTimeout( user, context.getGuild() ) ? "" : "not " );
            } else { // Argument not a user mention.
                message = String.format( NOT_USER_ERROR, arg.getText() );
            }
            replies.add( message );

        }

        reply.withContent( String.join( "\n", replies ) ).build();

    }

}
