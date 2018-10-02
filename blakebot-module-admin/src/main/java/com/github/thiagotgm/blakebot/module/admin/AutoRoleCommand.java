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
import com.github.thiagotgm.modular_commands.api.ICommand;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Command set that allows joined guilds to specify a role to automatically
 * assign to new users.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-03-21
 */
@SuppressWarnings( "javadoc" )
public class AutoRoleCommand {

    private static final String NAME = "Auto Role";
    private static final String SET = "Set Auto Role";
    private static final String CHECK = "Check Auto Role";
    private static final String REMOVE = "Remove Auto Role";

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
            usage = "{signature} <subcommand>",
            subCommands = { SET, CHECK, REMOVE },
            ignorePrivate = true,
            ignorePublic = true,
            requiredGuildPermissions = Permissions.MANAGE_ROLES )
    public void autoRoleCommand( CommandContext context ) {

        // Do nothing.

    }

    @SubCommand(
            name = SET,
            aliases = "set",
            description = "Sets the role that new users should be assigned.\n"
                    + "The role argument can be either a mention to the target role or its name, "
                    + "but in the latter case, if the name of the role is not unique (there "
                    + "are other roles with the same name), the command will fail.",
            usage = "{signature} <role>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER,
            requiresParentPermissions = true,
            ignorePrivate = true )
    public boolean setRoleCommand( CommandContext context ) {

        List<Argument> args = context.getArguments();
        if ( args.isEmpty() ) {
            context.setHelper( "Please specify a role." );
            return false;
        }

        IRole role;
        switch ( args.get( 0 ).getType() ) {

            case ROLE_MENTION: // Argument is already role.
                role = (IRole) args.get( 0 ).getArgument();
                break;

            default: // Check if argument is role name.
                List<IRole> roles = context.getGuild().getRolesByName( args.get( 0 ).getText() );
                if ( roles.size() == 1 ) {
                    role = roles.get( 0 );
                } else {
                    if ( roles.size() == 0 ) {
                        context.setHelper( "Argument is not a role or role name." );
                    } else {
                        context.setHelper( "Argument is ambiguous: multiple roles with that name." );
                    }
                    return false;
                }

        }

        // Register role for server.
        manager.set( context.getGuild(), role );
        context.setHelper( String.format( "New users will be set to the role **%s**.", role.getName() ) );
        return true;

    }

    @SubCommand(
            name = CHECK,
            aliases = "check",
            description = "Checks what role new users are being assigned.",
            usage = "{signature}",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            ignorePrivate = true,
            requiresParentPermissions = false )
    public void checkRoleCommand( CommandContext context ) {

        IRole targetRole = manager.get( context.getGuild() );
        if ( targetRole != null ) {
            context.setHelper( String.format( "New users are being assigned the role **%s**.", targetRole.getName() ) );
        } else {
            context.setHelper( "No role set for new users." );
        }

    }

    @SubCommand(
            name = REMOVE,
            aliases = "remove",
            description = "Stops adding new users to a role.",
            usage = "{signature} remove",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER,
            requiresParentPermissions = true,
            ignorePrivate = true )
    public boolean removeRoleCommand( CommandContext context ) {

        if ( manager.remove( context.getGuild() ) ) {
            context.setHelper( "Disabled automatic role assigning." );
            return true;
        } else {
            context.setHelper( "Automatic role assigning not enabled." );
            return false;
        }

    }

}
