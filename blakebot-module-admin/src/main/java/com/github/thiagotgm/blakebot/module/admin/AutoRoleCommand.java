package com.github.thiagotgm.blakebot.module.admin;

import java.util.Hashtable;
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.alphahelix00.ordinator.commands.SubCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

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
    
    private final Hashtable<String, String> roles;
    
    /**
     * Creates a new instance of this class.
     */
    public AutoRoleCommand() {
        
        roles = new Hashtable<>();
        
    }
    
    @MainCommand(
            prefix = AdminModule.PREFIX,
            name = NAME,
            alias = { "autorole", "ar" },
            description = "Automatically assigns new users to a certain role.",
            usage = AdminModule.PREFIX + "autorole|ar <subcommand>",
            subCommands = { SET, CHECK }
    )
    public void autoRoleCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
    
        // Do nothing.
        
    }
    
    @SubCommand(
            name = SET,
            alias = "set",
            description = "Sets the role that new users should be assigned.",
            usage = AdminModule.PREFIX + "autorole|ar set <role>"
    )
    public void setRoleCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        List<IRole> roleList = event.getMessage().getRoleMentions(); // Obtain target role.
        if ( roleList.isEmpty() ) {
            RequestBuffer.request( () -> {
                
                try {
                    msgBuilder.withContent( "Please specify a role." );
                    msgBuilder.build();
                } catch ( DiscordException | MissingPermissionsException e ) {
                    CommandHandlerD4J.logMissingPerms( event, CHECK, e );
                }
                
            });
            return;
        }
        IRole role = roleList.get( 0 );
        
        // Register role for server.
        roles.put( event.getMessage().getGuild().getID(), role.getID() );
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "New users will be set to the role " + role.mention() );
                msgBuilder.build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, CHECK, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = CHECK,
            alias = "check",
            description = "Checks what role new users are being assigned.",
            usage = AdminModule.PREFIX + "autorole|ar check <role>"
    )
    public void checkRoleCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        String roleID = roles.get( event.getMessage().getGuild().getID() );
        IRole targetRole = ( roleID != null ) ? event.getMessage().getGuild().getRoleByID( roleID ) : null;
        RequestBuffer.request( () -> {
            
            try {
                if ( targetRole != null ) {    
                    msgBuilder.withContent( "New users are being assigned the role " + targetRole.mention() );
                } else {
                    msgBuilder.withContent( "No role set for new users." );
                }
                msgBuilder.build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, CHECK, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = REMOVE,
            alias = "remove",
            description = "Stops adding new users to a role.",
            usage = AdminModule.PREFIX + "autorole|ar remove"
    )
    public void removeRoleCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        String roleID = roles.remove( event.getMessage().getGuild().getID() );
        RequestBuffer.request( () -> {
            
            try {
                if ( roleID != null ) {    
                    msgBuilder.withContent( "Disabled automatic role assigning." );
                } else {
                    msgBuilder.withContent( "Automatic role assigning not enabled." );
                }
                msgBuilder.build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, CHECK, e );
            }
            
        });
        
    }

}
