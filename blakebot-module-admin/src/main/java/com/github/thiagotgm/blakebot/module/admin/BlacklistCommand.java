package com.github.thiagotgm.blakebot.module.admin;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.discordinator.d4j.permissions.Permission;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.alphahelix00.ordinator.commands.SubCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
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
    private static final String ADD_CHANNEL_NAME = "Add (Channel)";
    
    private static final String LIST_NAME = "List";
    private static final String LIST_SERVER_NAME = "List (Server)";
    private static final String LIST_CHANNEL_NAME = "List (Channel)";
    
    private static final String REMOVE_NAME = "Remove";
    private static final String REMOVE_SERVER_NAME = "Remove (Server)";
    private static final String REMOVE_CHANNEL_NAME = "Remove (Channel)";
    
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
            alias = "add",
            description = "Adds a new blacklist entry. A scope must be specified. Requires 'Manage Messages' permission.",
            usage = AdminModule.PREFIX + "blacklist|bl add <scope> <entry>",
            subCommands = { ADD_SERVER_NAME, ADD_CHANNEL_NAME }
    )
    @Permission(
            permissions = { Permissions.MANAGE_MESSAGES }
    )
    public void blacklistAddCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = ADD_SERVER_NAME,
            alias = "server",
            description = "Adds a new blacklist entry that applies to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, the entry will " +
                          "only apply to them.",
            usage = AdminModule.PREFIX + "blacklist|bl add server <entry> [@users] [@roles]"
    )
    public void blacklistAddServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
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
        final String finalMessage = message;
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( finalMessage ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, ADD_SERVER_NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = ADD_CHANNEL_NAME,
            alias = "channel",
            description = "Adds a new blacklist entry that applies to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, the entry will " +
                          "only apply to them.",
            usage = AdminModule.PREFIX + "blacklist|bl add channel <entry> [@users] [@roles]"
    )
    public void blacklistAddChannelCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
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
        final String finalMessage = message;
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( finalMessage ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, ADD_CHANNEL_NAME, e );
            }
            
        });
        
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
            alias = "list",
            description = "Lists blacklist entries. A scope must be specified.",
            usage = AdminModule.PREFIX + "blacklist|bl list <scope>",
            subCommands = { LIST_SERVER_NAME, LIST_CHANNEL_NAME }
    )
    public void blacklistListCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = LIST_SERVER_NAME,
            alias = "server",
            description = "Lists entries that apply to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, displays their " +
                          "specific entries.",
            usage = AdminModule.PREFIX + "blacklist|bl list server [@users] [@roles]"
    )
    public void blacklistListServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
       
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
                restrictionList.removeAll( blacklist.getRestrictions( guild ) );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + user.mention(), restrictions, false );
                
            }
            for ( IRole role : roles ) {
                
                List<String> restrictionList = blacklist.getRestrictions( role, guild );
                restrictionList.removeAll( blacklist.getRestrictions( guild ) );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + role.mention(), restrictions, false );
                
            }
        }
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withEmbed( builder.build() ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, LIST_SERVER_NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = LIST_CHANNEL_NAME,
            alias = "channel",
            description = "Lists entries that apply to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, displays their " +
                          "specific entries.",
            usage = AdminModule.PREFIX + "blacklist|bl list channel [@users] [@roles]"
    )
    public void blacklistListChannelCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
       
        // Extracts user and role specifiers.
        List<IUser> users = event.getMessage().getMentions();
        List<IRole> roles = event.getMessage().getRoleMentions();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor( Color.BLACK );
        String restrictions;
        IChannel channel = event.getMessage().getChannel();
        if ( users.isEmpty() && roles.isEmpty() ) {
            List<String> restrictionList = blacklist.getRestrictions( channel );
            restrictionList.removeAll( blacklist.getRestrictions( channel.getGuild() ) );
            restrictions = formatRestrictionList( restrictionList );
            builder.appendField( "Channel-wide blacklist", restrictions, false );
        } else {
            builder.withDescription( "In this channel:" );
            for ( IUser user : users ) {
                
                List<String> restrictionList = blacklist.getRestrictions( user, channel );
                restrictionList.removeAll( blacklist.getRestrictions( channel ) );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + user.mention(), restrictions, false );
                
            }
            for ( IRole role : roles ) {
                
                List<String> restrictionList = blacklist.getRestrictions( role, channel );
                restrictionList.removeAll( blacklist.getRestrictions( channel ) );
                restrictions = formatRestrictionList( restrictionList );
                builder.appendField( "Blacklist for " + role.mention(), restrictions, false );
                
            }
        }
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withEmbed( builder.build() ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, LIST_CHANNEL_NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = REMOVE_NAME,
            alias = { "remove", "rm" },
            description = "Removes a blacklist entry. A scope must be specified. Requires 'Manage Messages' permission.",
            usage = AdminModule.PREFIX + "blacklist|bl remove|rm <scope> <entry>",
            subCommands = { REMOVE_SERVER_NAME, REMOVE_CHANNEL_NAME }
    )
    @Permission(
            permissions = { Permissions.MANAGE_MESSAGES }
    )
    public void blacklistRemoveCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.
        
    }
    
    @SubCommand(
            name = REMOVE_SERVER_NAME,
            alias = "server",
            description = "Removes an entry that applies to the server where the " +
                          "command is used.\nIf any users or roles are @mentioned, removes from " +
                          "their personal entries.",
            usage = AdminModule.PREFIX + "blacklist|bl remove|rm server <entry> [@users] [@roles]"
    )
    public void blacklistRemoveServerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
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
        final String finalMessage = message;
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( finalMessage ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, REMOVE_SERVER_NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            name = REMOVE_CHANNEL_NAME,
            alias = "channel",
            description = "Removes an entry that applies to the channel where the " +
                          "command is used.\nIf any users or roles are @mentioned, removes from " +
                          "their personal entries.",
            usage = AdminModule.PREFIX + "blacklist|bl remove|rm channel <entry> [@users] [@roles]"
    )
    public void blacklistRemoveChannelCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
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
        final String finalMessage = message;
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( finalMessage ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, REMOVE_CHANNEL_NAME, e );
            }
            
        });
        
    }

}
