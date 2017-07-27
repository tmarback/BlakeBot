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

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

/**
 * Commands that manage a word blacklist.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class BlacklistCommand {
    
    private static final String NAME = "Blacklist";
    
    private static final String ADD_NAME = "Blacklist Add";
    private static final String ADD_SERVER_NAME = "Blacklist Add (Server)";
    private static final String ADD_CHANNEL_NAME = "Blacklist Add (Channel)";
    
    private static final String LIST_NAME = "Blacklist List";
    private static final String LIST_SERVER_NAME = "Blacklist List (Server)";
    private static final String LIST_CHANNEL_NAME = "Blacklist List (Channel)";
    
    private static final String REMOVE_NAME = "Blacklist Remove";
    private static final String REMOVE_SERVER_NAME = "Blacklist Remove (Server)";
    private static final String REMOVE_CHANNEL_NAME = "Blacklist Remove (Channel)";
    
    private final Blacklist blacklist;
    
    public BlacklistCommand() {
        
        blacklist = Blacklist.getInstance();
        
    }
    
    @MainCommand(
            name = NAME,
            aliases = { "blacklist", "bl" },
            description = "Manages the message blacklist. A subcommand must be used.",
            usage = "{}blacklist|bl <subcommand>",
            subCommands = { ADD_NAME, LIST_NAME, REMOVE_NAME }
    )
    public void blacklistCommand( CommandContext context ) {
    
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
    
    /**
     * Returns the list of mentions for a list of Users.
     *
     * @param users List of Users.
     * @return List with a mention for each User.
     */
    private List<String> userMentions( List<IUser> users ) {       
        
        List<String> list = new LinkedList<>();
        for ( IUser user : users ) {
            
            list.add( user.mention().replace( "!", "" ) );
            
        }
        return list;
        
    }
    
    /**
     * Returns the list of mentions for a list of Roles.
     *
     * @param roles List of Roles.
     * @return List with a mention for each Role.
     */
    private List<String> roleMentions( List<IRole> roles ) {       
        
        List<String> list = new LinkedList<>();
        for ( IRole role : roles ) {
            
            list.add( role.mention().replace( "!", "" ) );
            
        }
        return list;
        
    }
    
    @SubCommand(
            name = ADD_NAME,
            aliases = "add",
            description = "Adds a new blacklist entry. A scope must be specified. Requires 'Manage Messages' permission.",
            usage = "{}blacklist|bl add <scope> <entry>",
            subCommands = { ADD_SERVER_NAME, ADD_CHANNEL_NAME }
    )
    public void blacklistAddCommand( CommandContext context ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = ADD_SERVER_NAME,
            aliases = "server",
            description = "Adds a new blacklist entry that applies to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, the entry will " +
                          "only apply to them.",
            usage = "{}blacklist|bl add server <entry> [@users] [@roles]"
    )
    public void blacklistAddServerCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        if ( !event.getMessage().getAuthor().getPermissionsForGuild( event.getMessage().getGuild() ).contains( Permissions.MANAGE_MESSAGES ) ) {
            return; // User does not have server-wide message management permissions.
        }
        
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        args.removeAll( userMentions( users ) );
        List<IRole> roles = event.getMessage().getRoleMentions();
        args.removeAll( roleMentions( roles ) );
        
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be added.";
        } else {
            String restriction = parseRestriction( args );
            if ( users.isEmpty() && roles.isEmpty() ) {
                boolean success = blacklist.addRestriction( restriction, event.getMessage().getGuild() );
                message = ( success ) ? ( "\u200BSuccessfully blacklisted \"" + restriction + "\" for this server!" ) :
                                        ( "\u200BFailure: \"" + restriction + "\" is already blacklisted in this server." );
            } else {
                
                /* Adds restriction for each specified User. */
                StringBuilder userSuccess = new StringBuilder();
                StringBuilder userFailure = new StringBuilder();
                for ( IUser user : users ) {
                    
                    boolean success = blacklist.addRestriction( restriction, user, event.getMessage().getGuild() );
                    if ( success ) {
                        userSuccess.append( user.mention() + ", " );
                    } else {
                        userFailure.append( user.mention() + ", " );
                    }
                    
                }
                if ( userSuccess.length() > 0 ) {
                    userSuccess.delete( userSuccess.length() - 2, userSuccess.length() - 1 );
                }
                if ( userFailure.length() > 0 ) {
                    userFailure.delete( userFailure.length() - 2, userFailure.length() - 1 );
                }
                
                /* Adds restriction for each specified Role. */
                StringBuilder roleSuccess = new StringBuilder();
                StringBuilder roleFailure = new StringBuilder();
                for ( IRole role : roles ) {
                    
                    boolean success = blacklist.addRestriction( restriction, role, event.getMessage().getGuild() );
                    if ( success ) {
                        roleSuccess.append( role.mention() + ", " );
                    } else {
                        roleFailure.append( role.mention() + ", " );
                    }
                    
                }
                if ( roleSuccess.length() > 0 ) {
                    roleSuccess.delete( roleSuccess.length() - 2, roleSuccess.length() - 1 );
                }
                if ( roleFailure.length() > 0 ) {
                    roleFailure.delete( roleFailure.length() - 2, roleFailure.length() - 1 );
                }
                
                message = "\u200B";
                /* Adds successes to output message */
                if ( ( userSuccess.length() > 0 ) || ( roleSuccess.length() > 0 )  ) {
                    message += "Successfully blacklisted \"" + restriction + "\" for ";
                    if ( userSuccess.length() > 0 ) {
                        message += "users " + userSuccess;
                    }
                    if ( ( userSuccess.length() > 0 ) && ( roleSuccess.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleSuccess.length() > 0 ) {
                        message += "roles " + roleSuccess;
                    }
                    message += " in this server!";
                    
                }
                /* Adds failures to output message */
                if ( ( userFailure.length() > 0 ) || ( roleFailure.length() > 0 )  ) {
                    if ( !message.equals( "\u200B" ) ) {
                        message += "\n";
                    }
                    message += "Failure: \"" + restriction + "\" is already blacklisted for ";
                    if ( userFailure.length() > 0 ) {
                        message += "users " + userFailure;
                    }
                    if ( ( userFailure.length() > 0 ) && ( roleFailure.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleFailure.length() > 0 ) {
                        message += "roles " + roleFailure;
                    }
                    message += " in this server.";
                    
                }
                
            }
            
        }
        msgBuilder.withContent( message ).build();
        
    }
    
    @SubCommand(
            name = ADD_CHANNEL_NAME,
            aliases = "channel",
            description = "Adds a new blacklist entry that applies to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, the entry will " +
                          "only apply to them.",
            usage = "{}blacklist|bl add channel <entry> [@users] [@roles]",
            requiredPermissions = Permissions.MANAGE_MESSAGES
    )
    public void blacklistAddChannelCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        args.removeAll( userMentions( users ) );
        List<IRole> roles = event.getMessage().getRoleMentions();
        args.removeAll( roleMentions( roles ) );
        
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be added.";
        } else {
            String restriction = parseRestriction( args );
            if ( users.isEmpty() && roles.isEmpty() ) {
                boolean success = blacklist.addRestriction( restriction, event.getMessage().getChannel() );
                message = ( success ) ? ( "\u200BSuccessfully blacklisted \"" + restriction + "\" for this channel!" ) :
                                        ( "\u200BFailure: \"" + restriction + "\" is already blacklisted in this channel." );
            } else {
                
                /* Adds restriction for each specified User. */
                StringBuilder userSuccess = new StringBuilder();
                StringBuilder userFailure = new StringBuilder();
                for ( IUser user : users ) {
                    
                    boolean success = blacklist.addRestriction( restriction, user, event.getMessage().getChannel() );
                    if ( success ) {
                        userSuccess.append( user.mention() + ", " );
                    } else {
                        userFailure.append( user.mention() + ", " );
                    }
                    
                }
                if ( userSuccess.length() > 0 ) {
                    userSuccess.delete( userSuccess.length() - 2, userSuccess.length() - 1 );
                }
                if ( userFailure.length() > 0 ) {
                    userFailure.delete( userFailure.length() - 2, userFailure.length() - 1 );
                }
                
                /* Adds restriction for each specified Role. */
                StringBuilder roleSuccess = new StringBuilder();
                StringBuilder roleFailure = new StringBuilder();
                for ( IRole role : roles ) {
                    
                    boolean success = blacklist.addRestriction( restriction, role, event.getMessage().getChannel() );
                    if ( success ) {
                        roleSuccess.append( role.mention() + ", " );
                    } else {
                        roleFailure.append( role.mention() + ", " );
                    }
                    
                }
                if ( roleSuccess.length() > 0 ) {
                    roleSuccess.delete( roleSuccess.length() - 2, roleSuccess.length() - 1 );
                }
                if ( roleFailure.length() > 0 ) {
                    roleFailure.delete( roleFailure.length() - 2, roleFailure.length() - 1 );
                }
                
                message = "\u200B";
                /* Adds successes to output message */
                if ( ( userSuccess.length() > 0 ) || ( roleSuccess.length() > 0 )  ) {
                    message += "Successfully blacklisted \"" + restriction + "\" for ";
                    if ( userSuccess.length() > 0 ) {
                        message += "users " + userSuccess;
                    }
                    if ( ( userSuccess.length() > 0 ) && ( roleSuccess.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleSuccess.length() > 0 ) {
                        message += "roles " + roleSuccess;
                    }
                    message += " in this channel!";
                    
                }
                /* Adds failures to output message */
                if ( ( userFailure.length() > 0 ) || ( roleFailure.length() > 0 )  ) {
                    if ( !message.equals( "\u200B" ) ) {
                        message += "\n";
                    }
                    message += "Failure: \"" + restriction + "\" is already blacklisted for ";
                    if ( userFailure.length() > 0 ) {
                        message += "users " + userFailure;
                    }
                    if ( ( userFailure.length() > 0 ) && ( roleFailure.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleFailure.length() > 0 ) {
                        message += "roles " + roleFailure;
                    }
                    message += " in this channel.";
                    
                }
                
            }
            
        }
        msgBuilder.withContent( message ).build();
        
    }
    
    private String formatRestrictionList( List<String> list ) {
        
        StringBuilder builder = new StringBuilder();
        for ( String restriction : list ) {
            
            builder.append( restriction );
            builder.append( '\n' );
            
        }
        if ( builder.length() > 0 ) {
            builder.deleteCharAt( builder.length() - 1 );
        } else {
            builder.append( "\u200B\n\u200B" );
        }
        return builder.toString();
        
    }
      
    @SubCommand(
            name = LIST_NAME,
            aliases = "list",
            description = "Lists blacklist entries. A scope must be specified.",
            usage = "{}blacklist|bl list <scope>",
            subCommands = { LIST_SERVER_NAME, LIST_CHANNEL_NAME }
    )
    public void blacklistListCommand( CommandContext context ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = LIST_SERVER_NAME,
            aliases = "server",
            description = "Lists entries that apply to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, displays their " +
                          "specific entries.",
            usage = "{}blacklist|bl list server [@users] [@roles]"
    )
    public void blacklistListServerCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        MessageBuilder msgBuilder = context.getReplyBuilder();
       
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        List<IRole> roles = event.getMessage().getRoleMentions();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor( Color.BLACK );
        String restrictions;
        IGuild guild = event.getMessage().getGuild();
        if ( users.isEmpty() && roles.isEmpty() ) {
            restrictions = formatRestrictionList( blacklist.getRestrictions( guild ) );
            builder.appendField( "Server-wide blacklist", restrictions, false );
        } else {
            builder.withDescription( "In this server:" );
            for ( IUser user : users ) {
                
                List<String> restrictionList = blacklist.getRestrictions( user, guild );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + user.getDisplayName( guild ), restrictions, false );
                
            }
            for ( IRole role : roles ) {
                
                List<String> restrictionList = blacklist.getRestrictions( role, guild );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + role.getName(), restrictions, false );
                
            }
        }
        
        msgBuilder.withEmbed( builder.build() ).build();
        
    }
    
    @SubCommand(
            name = LIST_CHANNEL_NAME,
            aliases = "channel",
            description = "Lists entries that apply to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, displays their " +
                          "specific entries.",
            usage = "{}blacklist|bl list channel [@users] [@roles]"
    )
    public void blacklistListChannelCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        MessageBuilder msgBuilder = context.getReplyBuilder();
       
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        List<IRole> roles = event.getMessage().getRoleMentions();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor( Color.BLACK );
        String restrictions;
        IChannel channel = event.getMessage().getChannel();
        if ( users.isEmpty() && roles.isEmpty() ) {
            List<String> restrictionList = blacklist.getRestrictions( channel );
            restrictions = formatRestrictionList( restrictionList );
            builder.appendField( "Channel-wide blacklist", restrictions, false );
        } else {
            builder.withDescription( "In this channel:" );
            for ( IUser user : users ) {
                
                List<String> restrictionList = blacklist.getRestrictions( user, channel );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + user.getDisplayName( channel.getGuild() ), restrictions, false );
                
            }
            for ( IRole role : roles ) {
                
                List<String> restrictionList = blacklist.getRestrictions( role, channel );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + role.getName(), restrictions, false );
                
            }
        }
        msgBuilder.withEmbed( builder.build() ).build();
        
    }
    
    @SubCommand(
            name = REMOVE_NAME,
            aliases = { "remove", "rm" },
            description = "Removes a blacklist entry. A scope must be specified. Requires 'Manage Messages' permission.",
            usage = "{}blacklist|bl remove|rm <scope> <entry>",
            subCommands = { REMOVE_SERVER_NAME, REMOVE_CHANNEL_NAME }
    )
    public void blacklistRemoveCommand( CommandContext context ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = REMOVE_SERVER_NAME,
            aliases = "server",
            description = "Removes an entry that applies to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, removes from " +
                          "their personal entries.",
            usage = "{}blacklist|bl remove|rm server <entry> [@users] [@roles]"
    )
    public void blacklistRemoveServerCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        if ( !event.getMessage().getAuthor().getPermissionsForGuild( event.getMessage().getGuild() ).contains( Permissions.MANAGE_MESSAGES ) ) {
            return; // User does not have server-wide message management permissions.
        }
        
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        args.removeAll( userMentions( users ) );
        List<IRole> roles = event.getMessage().getRoleMentions();
        args.removeAll( roleMentions( roles ) );
        
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be removed.";
        } else {
            String restriction = parseRestriction( args );
            if ( users.isEmpty() && roles.isEmpty() ) {
                boolean success = blacklist.removeRestriction( restriction, event.getMessage().getGuild() );
                message = ( success ) ? ( "\u200BSuccessfully removed \"" + restriction + "\" from this server's blacklist!" ) :
                                        ( "\u200BFailure: \"" + restriction + "\" s not blacklisted in this server." );
            } else {
                
                /* Adds restriction for each specified User. */
                StringBuilder userSuccess = new StringBuilder();
                StringBuilder userFailure = new StringBuilder();
                for ( IUser user : users ) {
                    
                    boolean success = blacklist.removeRestriction( restriction, user, event.getMessage().getGuild() );
                    if ( success ) {
                        userSuccess.append( user.mention() + ", " );
                    } else {
                        userFailure.append( user.mention() + ", " );
                    }
                    
                }
                if ( userSuccess.length() > 0 ) {
                    userSuccess.delete( userSuccess.length() - 2, userSuccess.length() - 1 );
                }
                if ( userFailure.length() > 0 ) {
                    userFailure.delete( userFailure.length() - 2, userFailure.length() - 1 );
                }
                
                /* Adds restriction for each specified Role. */
                StringBuilder roleSuccess = new StringBuilder();
                StringBuilder roleFailure = new StringBuilder();
                for ( IRole role : roles ) {
                    
                    boolean success = blacklist.removeRestriction( restriction, role, event.getMessage().getGuild() );
                    if ( success ) {
                        roleSuccess.append( role.mention() + ", " );
                    } else {
                        roleFailure.append( role.mention() + ", " );
                    }
                    
                }
                if ( roleSuccess.length() > 0 ) {
                    roleSuccess.delete( roleSuccess.length() - 2, roleSuccess.length() - 1 );
                }
                if ( roleFailure.length() > 0 ) {
                    roleFailure.delete( roleFailure.length() - 2, roleFailure.length() - 1 );
                }
                
                message = "\u200B";
                /* Adds successes to output message */
                if ( ( userSuccess.length() > 0 ) || ( roleSuccess.length() > 0 )  ) {
                    message += "Successfully removed \"" + restriction + "\" for ";
                    if ( userSuccess.length() > 0 ) {
                        message += "users " + userSuccess;
                    }
                    if ( ( userSuccess.length() > 0 ) && ( roleSuccess.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleSuccess.length() > 0 ) {
                        message += "roles " + roleSuccess;
                    }
                    message += " in this server!";
                    
                }
                /* Adds failures to output message */
                if ( ( userFailure.length() > 0 ) || ( roleFailure.length() > 0 )  ) {
                    if ( !message.equals( "\u200B" ) ) {
                        message += "\n";
                    }
                    message += "Failure: \"" + restriction + "\" is not blacklisted for ";
                    if ( userFailure.length() > 0 ) {
                        message += "users " + userFailure;
                    }
                    if ( ( userFailure.length() > 0 ) && ( roleFailure.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleFailure.length() > 0 ) {
                        message += "roles " + roleFailure;
                    }
                    message += " in this server.";
                    
                }
                
            }
            
        }
        msgBuilder.withContent( message ).build();
        
    }
    
    @SubCommand(
            name = REMOVE_CHANNEL_NAME,
            aliases = "channel",
            description = "Removes an entry that applies to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, removes from " +
                          "their personal entries.",
            usage = "{}blacklist|bl remove|rm channel <entry> [@users] [@roles]",
            requiredPermissions = Permissions.MANAGE_MESSAGES
    )
    public void blacklistRemoveChannelCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
        
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        args.removeAll( userMentions( users ) );
        List<IRole> roles = event.getMessage().getRoleMentions();
        args.removeAll( roleMentions( roles ) );
        
        String message;
        if ( args.isEmpty() ) {
            message = "Please provide an entry to be added.";
        } else {
            String restriction = parseRestriction( args );
            if ( users.isEmpty() && roles.isEmpty() ) {
                boolean success = blacklist.removeRestriction( restriction, event.getMessage().getChannel() );
                message = ( success ) ? ( "\u200BSuccessfully removed \"" + restriction + "\" from this channel's blacklist!" ) :
                                        ( "\u200BFailure: \"" + restriction + "\" is not blacklisted in this channel." );
            } else {
                
                /* Adds restriction for each specified User. */
                StringBuilder userSuccess = new StringBuilder();
                StringBuilder userFailure = new StringBuilder();
                for ( IUser user : users ) {
                    
                    boolean success = blacklist.removeRestriction( restriction, user, event.getMessage().getChannel() );
                    if ( success ) {
                        userSuccess.append( user.mention() + ", " );
                    } else {
                        userFailure.append( user.mention() + ", " );
                    }
                    
                }
                if ( userSuccess.length() > 0 ) {
                    userSuccess.delete( userSuccess.length() - 2, userSuccess.length() - 1 );
                }
                if ( userFailure.length() > 0 ) {
                    userFailure.delete( userFailure.length() - 2, userFailure.length() - 1 );
                }
                
                /* Adds restriction for each specified Role. */
                StringBuilder roleSuccess = new StringBuilder();
                StringBuilder roleFailure = new StringBuilder();
                for ( IRole role : roles ) {
                    
                    boolean success = blacklist.removeRestriction( restriction, role, event.getMessage().getChannel() );
                    if ( success ) {
                        roleSuccess.append( role.mention() + ", " );
                    } else {
                        roleFailure.append( role.mention() + ", " );
                    }
                    
                }
                if ( roleSuccess.length() > 0 ) {
                    roleSuccess.delete( roleSuccess.length() - 2, roleSuccess.length() - 1 );
                }
                if ( roleFailure.length() > 0 ) {
                    roleFailure.delete( roleFailure.length() - 2, roleFailure.length() - 1 );
                }
                
                message = "\u200B";
                /* Adds successes to output message */
                if ( ( userSuccess.length() > 0 ) || ( roleSuccess.length() > 0 )  ) {
                    message += "Successfully removed \"" + restriction + "\" for ";
                    if ( userSuccess.length() > 0 ) {
                        message += "users " + userSuccess;
                    }
                    if ( ( userSuccess.length() > 0 ) && ( roleSuccess.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleSuccess.length() > 0 ) {
                        message += "roles " + roleSuccess;
                    }
                    message += " in this channel!";
                    
                }
                /* Adds failures to output message */
                if ( ( userFailure.length() > 0 ) || ( roleFailure.length() > 0 )  ) {
                    if ( !message.equals( "\u200B" ) ) {
                        message += "\n";
                    }
                    message += "Failure: \"" + restriction + "\" is not blacklisted for ";
                    if ( userFailure.length() > 0 ) {
                        message += "users " + userFailure;
                    }
                    if ( ( userFailure.length() > 0 ) && ( roleFailure.length() > 0 )  ) {
                        message += " and ";
                    }
                    if ( roleFailure.length() > 0 ) {
                        message += "roles " + roleFailure;
                    }
                    message += " in this channel.";
                    
                }
                
            }
            
        }
        msgBuilder.withContent( message ).build();
        
    }

}
