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

import java.util.List;

import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SuccessHandler;

import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Command set that allows joined guilds to specify a role to automatically assign
 * to new users.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-03-21
 */
public class AutoRoleCommand {
    
    private static final String NAME = "Auto Role";
    private static final String SET = "Set Auto Role";
    private static final String CHECK = "Check Auto Role";
    private static final String REMOVE = "Remove Auto Role";
    private static final String SUCCESS_HANDLER = "success";
    private static final String FAILURE_HANDLER = "failure";
    
    private final AutoRoleManager manager;
    
    /**
     * Creates a new instance of this class.
     */
    public AutoRoleCommand() {
        
        manager = AutoRoleManager.getInstance();
        
    }
    
    @MainCommand(
            name = NAME,
            aliases = { "autorole", "ar" },
            description = "Automatically assigns new users to a certain role.",
            usage = "{}autorole|ar <subcommand>",
            subCommands = { SET, CHECK, REMOVE },
            ignorePrivate = true,
            ignorePublic = true
    )
    public void autoRoleCommand( CommandContext context ) {
    
        // Do nothing.
        
    }
    
    @SubCommand(
            name = SET,
            aliases = "set",
            description = "Sets the role that new users should be assigned.",
            usage = "{}autorole|ar set <role>",
            requiredGuildPermissions = Permissions.MANAGE_ROLES,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER,
            ignorePrivate = true
    )
    public boolean setRoleCommand( CommandContext context ) {
        
        List<Argument> args = context.getArguments();
        if ( args.isEmpty() ) {
            context.setHelper( "Please specify a role." );
            return false;
        }
        if ( args.get( 0 ).getType() != Argument.Type.ROLE_MENTION ) {
            context.setHelper( "Argument is not a role." );
            return false;
        }
        IRole role = (IRole) args.get( 0 ).getArgument();
        
        // Register role for server.
        manager.set( context.getGuild(), role );
        context.setHelper( String.format( "New users will be set to the role %s.",
                role.mention() ) );
        return true;
        
    }
    
    @SubCommand(
            name = CHECK,
            aliases = "check",
            description = "Checks what role new users are being assigned.",
            usage = "{}autorole|ar check <role>",
            successHandler = SUCCESS_HANDLER,
            ignorePrivate = true
    )
    public void checkRoleCommand( CommandContext context ) {

        IRole targetRole = manager.get( context.getGuild() );
        if ( targetRole != null ) {    
            context.setHelper( String.format( "New users are being assigned the role %s.",
                    targetRole.mention() ) );
        } else {
            context.setHelper( "No role set for new users." );
        }
        
    }
    
    @SubCommand(
            name = REMOVE,
            aliases = "remove",
            description = "Stops adding new users to a role.",
            usage = "{}autorole|ar remove",
            requiredGuildPermissions = Permissions.MANAGE_ROLES,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER,
            ignorePrivate = true
    )
    public boolean removeRoleCommand( CommandContext context ) {
        
        if ( manager.remove( context.getGuild() ) ) {    
            context.setHelper( "Disabled automatic role assigning." );
            return true;
        } else {
            context.setHelper( "Automatic role assigning not enabled." );
            return false;
        }
        
    }
    
    /**
     * Sends the reply set in the given context's helper object.
     *
     * @param context The context of the command.
     */
    @SuccessHandler( SUCCESS_HANDLER )
    public void sendReply( CommandContext context ) {
        
        context.getReplyBuilder().withContent( (String) context.getHelper().orElse( "" ) ).build();
        
    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void failure( CommandContext context, FailureReason reason ) {
        
        switch ( reason ) {
            
            case USER_MISSING_GUILD_PERMISSIONS:
                context.setHelper( "You do not have the required permissions." );
                
            case COMMAND_OPERATION_FAILED:
                sendReply( context );
                break;
                
            default:
                break;
        
        }
        
    }

}
