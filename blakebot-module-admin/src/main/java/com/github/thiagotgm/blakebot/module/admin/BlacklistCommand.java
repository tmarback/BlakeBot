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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.thiagotgm.blakebot.module.admin.Blacklist.Restriction;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SuccessHandler;

import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

/**
 * Commands that manage a word blacklist.
 *
 * @version 2.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class BlacklistCommand {
    
    private enum Scope { SERVER, CHANNEL }
    
    private static final String NAME = "Blacklist";
    private static final String SERVER_MODIFIER_NAME = "Server-wide blacklist";
    
    private static final String ADD_NAME = "Blacklist Add";
    private static final String REMOVE_NAME = "Blacklist Remove";
    private static final String LIST_NAME = "Blacklist List";
    
    private static final String WORD_NAME = "Word entries";
    private static final String REGEX_NAME = "Regex entries";
    
    private static final String SUCCESS_HANDLER = "success";
    private static final String FAILURE_HANDLER = "failure";
    
    private final Blacklist blacklist;
    
    /**
     * Constructs a new instance of the command.
     * 
     * @param blacklist The blacklist to manage.
     */
    public BlacklistCommand( Blacklist blacklist ) {
        
        this.blacklist = blacklist;
        
    }
    
    @MainCommand(
            name = NAME,
            aliases = { "blacklist", "bl" },
            description = "Manages the message blacklist. A subcommand must be used. "
                    + "The scope is channel-wide unless the 'server' modifier is used.",
            usage = "{}blacklist|bl [server] <subcommand> <arguments>",
            ignorePrivate = true,
            ignorePublic = true,
            subCommands = { ADD_NAME, LIST_NAME, REMOVE_NAME, SERVER_MODIFIER_NAME },
            requiredPermissions = { Permissions.MANAGE_CHANNEL, Permissions.MANAGE_MESSAGES }
    )
    public void blacklistCommand( CommandContext context ) {
    
        context.setHelper( Scope.CHANNEL );
        
    }
    
    @SubCommand(
            name = SERVER_MODIFIER_NAME,
            aliases = "server",
            description = "Sets the scope of the command to be server-wide "
                    + "instead of channel-wide.",
            usage = "{}blacklist|bl server <subcommand> <arguments>",
            ignorePrivate = true,
            ignorePublic = true,
            subCommands = { ADD_NAME, LIST_NAME, REMOVE_NAME },
            requiresParentPermissions = false,
            requiredGuildPermissions = { Permissions.MANAGE_SERVER, Permissions.MANAGE_MESSAGES }
            )
    public void serverModifier( CommandContext context ) {
        
        context.setHelper( Scope.SERVER );
        
    }
    
    /* For editing the blacklist */
    
    @SubCommand(
            name = WORD_NAME,
            aliases = "word",
            description = "Performs the operation (add or remove), but treating each entry as "
                    + "a full word/expression. Is case insensitive.",
            usage = "{}blacklist|bl [server] <operation> word [user/role]... <entry> [entry]...",
            ignorePrivate = true,
            executeParent = true,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER
            )
    public void wordModifier( CommandContext context ) {}
    
    @SubCommand(
            name = REGEX_NAME,
            aliases = "regex",
            description = "Performs the operation (add or remove), but treating each entry as "
                    + "a regex expression. Is case sensitive.",
            usage = "{}blacklist|bl [server] <operation> regex [user/role]... <entry> [entry]...",
            ignorePrivate = true,
            executeParent = true,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER
            )
    public void regexModifier( CommandContext context ) {}
    
    /**
     * Parses the argument given to the command. If the command is the Word or Regex modifier,
     * the entries are parsed accordingly. Else, entries are parsed as Content.
     *
     * @param context Context of the command being executed.
     * @return The parsed arguments.
     */
    private Arguments parseArgs( CommandContext context ) {
        
        Restriction.Type entryType;
        switch ( context.getCommand().getName() ) {
            
            case WORD_NAME:
                entryType = Restriction.Type.WORD;
                break;
                
            case REGEX_NAME:
                entryType = Restriction.Type.REGEX;
                break;
                
            default:
                entryType = Restriction.Type.CONTENT;
            
        }
        return new Arguments( context.getArguments(), entryType );
        
    }
    
    /**
     * Runs an operation on the full scope.
     *
     * @param operation The operation to run.
     * @param entry The entry to run it with.
     * @param message Where to put the result message in.
     * @param successMessage The result message on a success.
     * @param failureMessage The result message on a failure.
     */
    private void runOnScope( Predicate<Restriction> operation, Restriction entry,
            StringBuilder message, String successMessage, String failureMessage ) {
        
        boolean success = operation.test( entry );
        String resultMessage = ( success ) ? successMessage : failureMessage;
        message.append( String.format( resultMessage, entry.getText() ) );
        
    }
    
    /**
     * Runs an operation on certain users and roles.
     *
     * @param userOperation The operation to run on users.
     * @param roleOperation The operation to run on roles.
     * @param entry The entry to run them with.
     * @param users The users to run it for.
     * @param roles The roles to run it for.
     * @param message Where to put the result message in.
     * @param successMessage The result message on a success.
     * @param failureMessage The result message on a failure.
     */
    private void runOnUsersAndRoles( BiPredicate<IUser, Restriction> userOperation, 
            BiPredicate<IRole, Restriction> roleOperation, Restriction entry,
            List<IUser> users, List<IRole> roles,
            StringBuilder message, String successMessage, String failureMessage ) {
        
        List<String> successes = new LinkedList<>();
        List<String> failures = new LinkedList<>();
        
        for ( IUser user : users ) {
            // Apply to users.
            if ( userOperation.test( user, entry ) ) {
                successes.add( user.mention() );
            } else {
                failures.add( user.mention() );
            }
            
        }
        
        for ( IRole role : roles ) {
            // Apply to roles.
            if ( roleOperation.test( role, entry ) ) {
                successes.add( role.mention() );
            } else {
                failures.add( role.mention() );
            }
            
        }
        
        // Build message.
        if ( !successes.isEmpty() ) {
            message.append( String.format( successMessage, entry.getText(),
                    String.join( ", ", successes ) ) );
        }
        if ( !failures.isEmpty() ) {
            message.append( String.format( failureMessage, entry.getText(),
                    String.join( ", ", failures ) ) );
        }
        
    }
    
    /**
     * Runs an operation on a set of entries.
     *
     * @param operation The operation to run.
     * @param entries The entries to run on.
     */
    private void runOnEntries( Consumer<Restriction> operation, List<Restriction> entries ) {
        
        for ( Restriction entry : entries ) { // Run operation on each entry.
            
            operation.accept( entry );
            
        }
        
    }
    
    @SubCommand(
            name = ADD_NAME,
            aliases = "add",
            description = "Adds a new blacklist entry. By default, each entry is treated as "
                    + "content anywhere in each message. Modifiers can be used to make the "
                    + "entries be treated as words/expressions (must be surrounded by spaces) "
                    + "or a regex expression. With exception of when the regex modifier is used, "
                    + "entries are case insensitive.",
            usage = "{}blacklist|bl [server] add [modifier] [user/role]... <entry> [entry]...",
            ignorePrivate = true,
            executeParent = true,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER,
            subCommands = { WORD_NAME, REGEX_NAME }
    )
    public boolean blacklistAddCommand( CommandContext context ) {
        
        Arguments args = parseArgs( context );
        if ( args.getEntries().isEmpty() ) {
            return false; // Missing any entries.
        }
        
        Scope scope = (Scope) context.getHelper().get();
        
        Consumer<Restriction> operation; // Set up operation to run.
        StringBuilder message = new StringBuilder();
        if ( args.getUsers().isEmpty() && args.getRoles().isEmpty() ) {
            /* Scope-wide add */
            Predicate<Restriction> tempSetter = null;
            switch ( scope ) {
                
                case CHANNEL:
                    tempSetter = ( entry ) -> {
                        return blacklist.addRestriction( entry, context.getChannel() );
                    };
                    break;
                    
                case SERVER:
                    tempSetter = ( entry ) -> {
                        return blacklist.addRestriction( entry, context.getGuild() );
                    };
                    break;
                
            }
            final Predicate<Restriction> setter = tempSetter;
            operation = ( entry ) -> {
                runOnScope( setter, entry, message, "Blacklisted `%s`!\n",
                        "Failure: `%s` is already blacklisted.\n" );
            };
        } else {
            /* Add for specific users and roles. */
            BiPredicate<IUser, Restriction> tempUserSetter = null;
            BiPredicate<IRole, Restriction> tempRoleSetter = null;
            switch ( scope ) {
                
                case CHANNEL:
                    tempUserSetter = ( user, entry ) -> {
                        return blacklist.addRestriction( entry, user, context.getChannel() );
                    };
                    tempRoleSetter = ( role, entry ) -> {
                        return blacklist.addRestriction( entry, role, context.getChannel() );
                    };
                    break;
                    
                case SERVER:
                    tempUserSetter = ( user, entry ) -> {
                        return blacklist.addRestriction( entry, user, context.getGuild() );
                    };
                    tempRoleSetter = ( role, entry ) -> {
                        return blacklist.addRestriction( entry, role, context.getGuild() );
                    };
                    break;
                
            }
            final BiPredicate<IUser, Restriction> userSetter = tempUserSetter;
            final BiPredicate<IRole, Restriction> roleSetter = tempRoleSetter;
            operation = ( entry ) -> {
                runOnUsersAndRoles( userSetter, roleSetter, entry, 
                        args.getUsers(), args.getRoles(), message,
                        "Blacklisted `%s` for %s!\n",
                        "Failure: `%s` is already blacklisted for %s.\n" );
            };
        }
        runOnEntries( operation, args.getEntries() ); // Run on all entries.

        message.deleteCharAt( message.length() - 1 ); // Remove last line break.
        context.setHelper( message.toString() );
        return true; // Finished adding.
        
    }
    
    @SubCommand(
            name = REMOVE_NAME,
            aliases = { "remove", "rm" },
            description = "Removes a blacklist entry. The modifier must match the modifier used "
                    + "to add the entry.",
            usage = "{}blacklist|bl [server] remove|rm [modifier] [user/role]... <entry> [entry]...",
            ignorePrivate = true,
            executeParent = true,
            successHandler = SUCCESS_HANDLER,
            failureHandler = FAILURE_HANDLER,
            subCommands = { WORD_NAME, REGEX_NAME }
    )
    public boolean blacklistRemoveCommand( CommandContext context ) {
        
        Arguments args = parseArgs( context );
        if ( args.getEntries().isEmpty() ) {
            return false; // Missing any entries.
        }
        
        Scope scope = (Scope) context.getHelper().get();
        
        Consumer<Restriction> operation; // Set up operation to run.
        StringBuilder message = new StringBuilder();
        if ( args.getUsers().isEmpty() && args.getRoles().isEmpty() ) {
            /* Scope-wide add */
            Predicate<Restriction> tempSetter = null;
            switch ( scope ) {
                
                case CHANNEL:
                    tempSetter = ( entry ) -> {
                        return blacklist.removeRestriction( entry, context.getChannel() );
                    };
                    break;
                    
                case SERVER:
                    tempSetter = ( entry ) -> {
                        return blacklist.removeRestriction( entry, context.getGuild() );
                    };
                    break;
                
            }
            final Predicate<Restriction> setter = tempSetter;
            operation = ( entry ) -> {
                runOnScope( setter, entry, message, "Removed `%s` from the blacklist!\n",
                        "Failure: `%s` is not blacklisted.\n" );
            };
        } else {
            /* Add for specific users and roles. */
            BiPredicate<IUser, Restriction> tempUserSetter = null;
            BiPredicate<IRole, Restriction> tempRoleSetter = null;
            switch ( scope ) {
                
                case CHANNEL:
                    tempUserSetter = ( user, entry ) -> {
                        return blacklist.removeRestriction( entry, user, context.getChannel() );
                    };
                    tempRoleSetter = ( role, entry ) -> {
                        return blacklist.removeRestriction( entry, role, context.getChannel() );
                    };
                    break;
                    
                case SERVER:
                    tempUserSetter = ( user, entry ) -> {
                        return blacklist.removeRestriction( entry, user, context.getGuild() );
                    };
                    tempRoleSetter = ( role, entry ) -> {
                        return blacklist.removeRestriction( entry, role, context.getGuild() );
                    };
                    break;
                
            }
            final BiPredicate<IUser, Restriction> userSetter = tempUserSetter;
            final BiPredicate<IRole, Restriction> roleSetter = tempRoleSetter;
            operation = ( entry ) -> {
                runOnUsersAndRoles( userSetter, roleSetter, entry, 
                        args.getUsers(), args.getRoles(), message,
                        "Removed `%s` from the blacklist for %s!\n",
                        "Failure: `%s` is not blacklisted for %s.\n" );
            };
        }
        runOnEntries( operation, args.getEntries() ); // Run on all entries.

        message.deleteCharAt( message.length() - 1 ); // Remove last line break.
        context.setHelper( message.toString() );
        return true; // Finished adding.
        
    }
    
    /* For displaying the blacklist */
    
    /**
     * Formats a restriction set for display.
     *
     * @param restrictions The restrictions to be formatted.
     * @return The formatted list.
     */
    private String formatRestrictions( Collection<Restriction> restrictions ) {
        
        List<String> restrictionStrings = new ArrayList<>( restrictions.size() );
        restrictions.stream().forEachOrdered( r -> restrictionStrings.add( r.toString() ) );
        return ( restrictions.isEmpty() ) ? "\u200B" : String.join( "\n", restrictionStrings );
        
    }
      
    @SubCommand(
            name = LIST_NAME,
            aliases = "list",
            description = "Lists blacklist entries.",
            usage = "{}blacklist|bl [server] list [user/role]...",
            ignorePrivate = true,
            executeParent = true,
            requiresParentPermissions = false
    )
    public void blacklistListCommand( CommandContext context ) {
        
        Scope scope = (Scope) context.getHelper().get();
        EmbedBuilder builder = new EmbedBuilder();
        
        List<Argument> args = context.getArguments();
        if ( args.isEmpty() ) { // Scope-wide.
            String text = null;
            Set<Restriction> restrictions = null;
            switch ( scope ) {
                
                case CHANNEL:
                    text = "Channel";
                    restrictions = blacklist.getRestrictions( context.getChannel() );
                    break;
                    
                case SERVER:
                    text = "Server";
                    restrictions = blacklist.getRestrictions( context.getGuild() );
                    break;
                
            }
            String title = String.format( "%s-wide blacklist", text );
            builder.appendField( title, formatRestrictions( restrictions ), false );
        } else { // For specific users/roles.
            String text = null;
            Function<IUser, Set<Restriction>> userGetter = null;
            Function<IRole, Set<Restriction>> roleGetter = null;
            switch ( scope ) { // Set how to get restrictions for selected scope.
                
                case CHANNEL:
                    text = "channel";
                    userGetter = ( user ) -> {
                        
                        return blacklist.getRestrictions( user, context.getChannel() );
                        
                    };
                    roleGetter = ( role ) -> {
                        
                        return blacklist.getRestrictions( role, context.getChannel() );
                        
                    };
                    break;
                    
                case SERVER:
                    text = "server";
                    userGetter = ( user ) -> {
                        
                        return blacklist.getRestrictions( user, context.getGuild() );
                        
                    };
                    roleGetter = ( role ) -> {
                        
                        return blacklist.getRestrictions( role, context.getGuild() );
                        
                    };
                    break;
                
            }
            builder.withDescription( String.format( "In this %s:", text ) );
            
            for ( Argument arg : args ) { // Shows restrictions for each mention.
                
                String name;
                Set<Restriction> restrictions;
                switch ( arg.getType() ) {
                    
                    case USER_MENTION:
                        IUser user = (IUser) arg.getArgument();
                        name = user.getDisplayName( context.getGuild() );
                        restrictions = userGetter.apply( user );
                        break;
                        
                    case ROLE_MENTION:
                        IRole role = (IRole) arg.getArgument();
                        name = role.getName();
                        restrictions = roleGetter.apply( role );
                        break;
                        
                    default: // Ignore non-mentions.
                        continue;
                    
                }
                builder.appendField( "Blacklist for " + name,
                        formatRestrictions( restrictions ), false );
                
            }
        }
        context.getReplyBuilder().withEmbed( builder.build() ).build();
        
    }

    /* Success and failure handlers */
    
    @SuccessHandler( SUCCESS_HANDLER )
    public void successMessage( CommandContext context ) {
        
        String message = (String) context.getHelper().get();
        context.getReplyBuilder().withContent( "\u200B" ).appendContent( message ).build();
        
    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void failureMessage( CommandContext context, FailureReason reason ) {
        
        MessageBuilder builder = context.getReplyBuilder().withContent( "\u200B" );
        switch ( reason ) {
            
            case USER_MISSING_PERMISSIONS:
                builder.appendContent( "You do not have the required permissions." );
                break;
                
            case COMMAND_OPERATION_FAILED:
                builder.appendContent( "Please specify at least one entry." );
                break;
                
            default:
                return; // Do nothing.
            
        }
        builder.build();
        
    }
    
    /* Helper for parsing the arguments */
    
    /**
     * Encapsulates the arguments given to the command.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-12
     */
    private class Arguments {
        
        private final List<IUser> users;
        private final List<IRole> roles;
        private final List<Restriction> entries;
        
        /**
         * Parses the arguments given to the command, splitting into
         * mentioned users and roles and all text that came after.
         *
         * @param args The arguments received by the command.
         * @param restrictionType The type of restrictions being used.
         */
        public Arguments( List<Argument> args, Restriction.Type restrictionType ) {
            
            /* Parse the target users and roles */
            List<IUser> users = new LinkedList<>();
            List<IRole> roles = new LinkedList<>();
            int curr = 0;
            mentionParser: while ( curr < args.size() ) {
                
                Argument argument = args.get( curr );
                switch ( argument.getType() ) {
                    
                    case USER_MENTION:
                        users.add( (IUser) argument.getArgument() );
                        break;
                        
                    case ROLE_MENTION:
                        roles.add( (IRole) argument.getArgument() );
                        break;
                        
                    default: // Found first non-mention arg.
                        break mentionParser;
                    
                }
                curr++;
                
            }
            
            /* Parse the target expressions for the blacklist */
            List<Restriction> entries = new LinkedList<>();
            while ( curr < args.size() ) {
                
                entries.add( new Restriction( args.get( curr++ ).getText(), restrictionType ) );
                
            }
            
            /* Store lists */
            this.users = Collections.unmodifiableList( users );
            this.roles = Collections.unmodifiableList( roles );
            this.entries = Collections.unmodifiableList( entries );
            
        }
        
        /**
         * Retrieves the target users.
         *
         * @return The users.
         */
        public List<IUser> getUsers() {
            
            return users;
            
        }
        
        /**
         * Retrieves the target roles.
         *
         * @return The roles.
         */
        public List<IRole> getRoles() {
            
            return roles;
            
        }
        
        /**
         * Retrieves the target text entries.
         *
         * @return The target entries.
         */
        public List<Restriction> getEntries() {
            
            return entries;
            
        }
        
    }

}
