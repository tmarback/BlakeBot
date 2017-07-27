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

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.MessageBuilder;

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
            subCommands = { SET, CHECK, REMOVE }
    )
    public void autoRoleCommand( CommandContext context ) {
    
        // Do nothing.
        
    }
    
    @SubCommand(
            name = SET,
            aliases = "set",
            description = "Sets the role that new users should be assigned.",
            usage = "{}autorole|ar set <role>"
    )
    public void setRoleCommand( CommandContext context ) {
        
        MessageBuilder msgBuilder = context.getReplyBuilder();
        MessageReceivedEvent event = context.getEvent();
        
        List<IRole> roleList = event.getMessage().getRoleMentions(); // Obtain target role.
        if ( roleList.isEmpty() ) {
            msgBuilder.withContent( "Please specify a role." ).build();
            return;
        }
        IRole role = roleList.get( 0 );
        
        // Register role for server.
        manager.set( event.getMessage().getGuild(), role );
        msgBuilder.withContent( "New users will be set to the role " + role.mention() ).build();
        
    }
    
    @SubCommand(
            name = CHECK,
            aliases = "check",
            description = "Checks what role new users are being assigned.",
            usage = "{}autorole|ar check <role>"
    )
    public void checkRoleCommand( CommandContext context ) {

        MessageReceivedEvent event = context.getEvent();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        IRole targetRole = manager.get( event.getMessage().getGuild() );
        if ( targetRole != null ) {    
            msgBuilder.withContent( "New users are being assigned the role " + targetRole.mention() );
        } else {
            msgBuilder.withContent( "No role set for new users." );
        }
        msgBuilder.build();
        
    }
    
    @SubCommand(
            name = REMOVE,
            aliases = "remove",
            description = "Stops adding new users to a role.",
            usage = "{}autorole|ar remove"
    )
    public void removeRoleCommand( CommandContext context  ) {
        
        MessageReceivedEvent event = context.getEvent();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        boolean removed = manager.remove( event.getMessage().getGuild() );
        if ( removed ) {    
            msgBuilder.withContent( "Disabled automatic role assigning." );
        } else {
            msgBuilder.withContent( "Automatic role assigning not enabled." );
        }
        
    }

}
