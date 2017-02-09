package com.github.thiagotgm.blakebot.module.admin;

import java.awt.Color;
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.discordinator.d4j.permissions.Permission;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.alphahelix00.ordinator.commands.SubCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Commands that manage a word blacklist.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class BlacklistCommand {
    
    private static final String NAME = "Blacklist";
    
    private static final String ADD_NAME = "Add";
    private static final String ADD_SERVER_NAME = "Add (Server)";
    
    private static final String LIST_NAME = "List";
    private static final String LIST_SERVER_NAME = "List (Server)";
    
    private static final String REMOVE_NAME = "Remove";
    private static final String REMOVE_SERVER_NAME = "Remove (Server)";
    
    private final Blacklist blacklist;
    
    public BlacklistCommand() {
        
        blacklist = Blacklist.getInstance();
        
    }
    
    @MainCommand(
            prefix = AdminModule.PREFIX,
            name = NAME,
            alias = { "blacklist", "bl" },
            description = "Manages the message blacklist. A subcommand must be used.",
            usage = AdminModule.PREFIX + "blacklist|bl <subcommand>",
            subCommands = { ADD_NAME, LIST_NAME, REMOVE_NAME }
    )
    @Permission(
            permissions = {Permissions.MANAGE_MESSAGES}
    )
    public void blacklistCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
    
        // Do nothing.
        
    }
    
    /**
     * Obtains the complete restriction argument from a list of args.
     *
     * @param args The list of args.
     * @return The complete restriction argument.
     */
    private String parseRestriction( List<String> args ) {
        
        StringBuilder restriction = new StringBuilder();
        for ( String arg : args ) {
            
            restriction.append( arg );
            restriction.append( ' ' );
            
        }
        restriction.deleteCharAt( restriction.length() - 1 );
        return restriction.toString();
        
    }
    
    @SubCommand(
            name = ADD_NAME,
            alias = "add",
            description = "Adds a new blacklist entry. A scope must be specified.",
            usage = AdminModule.PREFIX + "blacklist|bl add <scope> <entry>",
            subCommands = { ADD_SERVER_NAME }
    )
    public void blacklistAddCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = ADD_SERVER_NAME,
            alias = "server",
            description = "Adds a new blacklist entry that applies to the server where the " +
                          "command is used.",
            usage = AdminModule.PREFIX + "blacklist|bl add server <entry>"
    )
    public void blacklistAddServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be added.";
        } else {
            String restriction = parseRestriction( args );
            boolean success = blacklist.addRestriction( restriction, event.getMessage().getGuild() );
            message = ( success ) ? ( "\u200BSuccessfully blacklisted \"" + restriction + "\" for this server!" ) :
                                    ( "\u200BFailure: \"" + restriction + "\" is already blacklisted in this server." );
        }
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( message ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, ADD_SERVER_NAME, e );
            }
            
        });
        
    }
    
    private String formatRestrictionList( List<String> list ) {
        
        StringBuilder builder = new StringBuilder();
        for ( String restriction : list ) {
            
            builder.append( restriction );
            builder.append( '\n' );
            
        }
        builder.deleteCharAt( builder.length() - 1 );
        return builder.toString();
        
    }
    
    @SubCommand(
            name = LIST_NAME,
            alias = "list",
            description = "Lists blacklist entries. A scope must be specified.",
            usage = AdminModule.PREFIX + "blacklist|bl list <scope>",
            subCommands = { LIST_SERVER_NAME }
    )
    public void blacklistListCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = LIST_SERVER_NAME,
            alias = "server",
            description = "Lists entry that apply to the server where the " +
                          "command is used.",
            usage = AdminModule.PREFIX + "blacklist|bl list server"
    )
    public void blacklistListServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
       
        RequestBuffer.request( () -> {
            
            try {
                String restrictions = formatRestrictionList( blacklist.getRestrictions( event.getMessage().getGuild() ) );
                EmbedBuilder builder = new EmbedBuilder();
                builder.appendField( "Server-wide blacklist", restrictions, false );
                builder.withColor( Color.BLACK );
                msgBuilder.withEmbed( builder.build() ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, LIST_SERVER_NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = REMOVE_NAME,
            alias = { "remove", "rm" },
            description = "Removes a blacklist entry. A scope must be specified.",
            usage = AdminModule.PREFIX + "blacklist|bl remove|rm <scope> <entry>",
            subCommands = { REMOVE_SERVER_NAME }
    )
    public void blacklistRemoveCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = REMOVE_SERVER_NAME,
            alias = "server",
            description = "Removes an entry that applies to the server where the " +
                          "command is used.",
            usage = AdminModule.PREFIX + "blacklist|bl remove|rm server <entry>"
    )
    public void blacklistRemoveServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
       
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be removed.";
        } else {
            String restriction = parseRestriction( args );
            boolean success = blacklist.removeRestriction( restriction, event.getMessage().getGuild() );
            message = ( success ) ? ( "\u200BSuccessfully removed \"" + restriction + "\" from this server's blacklist!" ) :
                                    ( "\u200BFailure: \"" + restriction + "\" is not blacklisted in this server." );
        }
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( message ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, REMOVE_SERVER_NAME, e );
            }
            
        });
        
    }

}
