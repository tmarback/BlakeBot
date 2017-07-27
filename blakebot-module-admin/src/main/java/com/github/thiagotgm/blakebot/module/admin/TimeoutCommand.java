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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
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
    
    private static final String NAME_1 = "Timeout";
    private static final String NAME_2 = "Untimeout";
    private static final String NAME_3 = "Check Timeout";
    private static final String SUB_NAME = "Un/Timeout Server";
    private static final String SUB_ALIAS = "server";
    
    private static final Permissions REQUIRED_CHANNEL = Permissions.MANAGE_MESSAGES;
    private static final Permissions REQUIRED_SERVER = Permissions.MANAGE_MESSAGES;
    
    private final Hashtable<Long, List<Executor>> executors;
    
    /**
     * Creates a new instance of this class.
     */
    public TimeoutCommand() {
        
        executors = new Hashtable<>();
        
    }
    
    @MainCommand(
            name = NAME_1,
            aliases = { "timeout", "to" },
            description = "Times a user out.",
            usage = "{}timeout|to <time> <user(s)>",
            subCommands = { SUB_NAME }
    )
    public void timeoutCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
   
        boolean permission = true;
        boolean hasSub = false; // Checks if has subcommand.
        if ( !args.isEmpty() && args.get( 0 ).equals( SUB_ALIAS ) ) {
            if ( !event.getMessage().getAuthor().getPermissionsForGuild( event.getMessage().getGuild() ).contains( REQUIRED_SERVER ) ) {
                permission = false; // No permissions to run command at this level.
            } else {
                hasSub = true;
                args = new LinkedList<>( args );
                args.remove( 0 );
            }
        } else {
            if ( !event.getMessage().getChannel().getModifiedPermissions( event.getMessage().getAuthor() ).contains( REQUIRED_CHANNEL ) ) {
                permission = false; // No permissions to run command at this level.
            }
        }
        
        if ( !permission ) {
            msgBuilder.withContent( "You do not have the right permission to run this command." ).build();
            return;
        }
        
        if ( args.size() < 2 ) { // Checks minimum amount of arguments.
            msgBuilder.withContent( "Please specify a time and the user(s) to be timed out." ).build();
            return;
        }
        
        long timeout;
        try { // Obtains time amount.
            timeout = Long.parseLong( args.get( 0 ) );
        } catch ( NumberFormatException e1 ) {
            msgBuilder.withContent( "Invalid time amount." ).build();
            return;
        }
        timeout *= 1000; // Converts to milliseconds.
        
        List<IUser> targets = event.getMessage().getMentions();
        if ( targets.isEmpty() ) { // Checks if a user was specified.
            msgBuilder.withContent( "Please specify a the user(s) to be timed out." ).build();
            return;
        }
        
        IUser thisUser = event.getClient().getOurUser();
        IChannel channel = event.getMessage().getChannel();
        List<Executor> execs = new LinkedList<>();
        for ( IUser target : targets ) {
            
            if ( thisUser.equals( target ) ) {
                continue; // Don't affect this bot.
            }
            
            Executor exec = new Executor( target, msgBuilder ).withTimeout( timeout );
            if ( hasSub ) { // Has subcommand, stores for it.
                execs.add( exec );
            } else { // No subcommand, run now.
                exec.withTargetChannel( channel ).execute();
            }
            
        }
        if ( !execs.isEmpty() ) {
            executors.put( event.getMessage().getLongID(), execs );
        }
        
    }
    
    @MainCommand(
            name = NAME_2,
            aliases = { "untimeout", "uto" },
            description = "Un-times out a user.",
            usage = "{}untimeout|uto <user(s)>",
            subCommands = { SUB_NAME }
    )
    public void untimeoutCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
   
        boolean permission = true;
        boolean hasSub = false; // Checks if has subcommand.
        if ( !args.isEmpty() && args.get( 0 ).equals( SUB_ALIAS ) ) {
            if ( !event.getMessage().getAuthor().getPermissionsForGuild( event.getMessage().getGuild() ).contains( REQUIRED_SERVER ) ) {
                permission = false; // No permissions to run command at this level.
            } else {
                hasSub = true;
                args = new LinkedList<>( args );
                args.remove( 0 );
            }
        } else {
            if ( !event.getMessage().getChannel().getModifiedPermissions( event.getMessage().getAuthor() ).contains( REQUIRED_CHANNEL ) ) {
                permission = false; // No permissions to run command at this level.
            }
        }
        
        if ( !permission ) {
            msgBuilder.withContent( "You do not have the right permission to run this command." ).build();
            return;
        }
        
        List<IUser> targets = event.getMessage().getMentions();
        if ( targets.isEmpty() ) { // Checks if a user was specified.
            msgBuilder.withContent( "Please specify a the user(s) to be untimed out." ).build();
        }
        
        IUser thisUser = event.getClient().getOurUser();
        IChannel channel = event.getMessage().getChannel();
        List<Executor> execs = new LinkedList<>();
        for ( IUser target : targets ) {
            
            if ( thisUser.equals( target ) ) {
                continue; // Don't affect this bot.
            }
            
            Executor exec = new Executor( target, msgBuilder );
            if ( hasSub ) { // Has subcommand, stores for it.
                execs.add( exec );
            } else { // No subcommand, run now.
                exec.withTargetChannel( channel ).execute();
            }
            
        }
        if ( !execs.isEmpty() ) {
            executors.put( event.getMessage().getLongID(), execs );
        }
        
    }
    
    @SubCommand(
            name = SUB_NAME,
            aliases = { SUB_ALIAS },
            description = "Applies the (un)timeout for the entire server.",
            usage = "{}timeout|to/untimeout|uto server <user>"
    )
    public void serverSubCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        
        List<Executor> execs = executors.remove( event.getMessage().getLongID() );
        if ( execs != null ) { // If there are users to (un)time out.
            IGuild guild = event.getMessage().getGuild();
            for ( Executor exec : execs ) {
                // Executes for each user with a guild-wide scope.
                exec.withTargetGuild( guild ).execute();
                
            }
        }
        
    }
    
    @MainCommand(
            name = NAME_3,
            aliases = { "istimedout", "isto" },
            description = "Checks if a user is timed out. Checks current channel, or server"
                    + "if the 'server' option is used.",
            usage = "{}istimeout|isto [server] <user(s)>"
    )
    public void checkCommand( CommandContext context ) {
        
        MessageReceivedEvent event = context.getEvent();
        List<String> args = context.getArgs();
        MessageBuilder msgBuilder = context.getReplyBuilder();
   
        boolean hasSub = false; // Checks if has subcommand.
        if ( !args.isEmpty() && args.get( 0 ).equals( SUB_ALIAS ) ) {
            hasSub = true;
            args.remove( 0 );
        }
        
        List<IUser> targets = event.getMessage().getMentions();
        if ( targets.isEmpty() ) { // Checks if a user was specified.
            msgBuilder.withContent( "Please specify a the user(s) to be checked." ).build();
            return;
        }
        
        TimeoutController controller = TimeoutController.getInstance();
        IChannel channel = event.getMessage().getChannel();
        IGuild guild = channel.getGuild();
        for ( IUser target : targets ) {
            // Checks if each user specified has a timeout.
            boolean timedOut = ( hasSub ) ? controller.hasTimeout( target, guild ) :
                                            controller.hasTimeout( target, channel );
            String message = "User " + target.mention() + " is " + ( ( timedOut ) ? "" : "not " ) +
                    "currently timed out on this " + ( ( hasSub ) ? "server" : "channel" ) + ".";
            msgBuilder.withContent( message ).build();
            
        }
        
    }
    
    /**
     * Executor class that calls in the appropriate (un)timeout for a particular scope.
     * Will only use the last scope set (either a channel or a guild).
     * If a timeout is set, performs a timeout. Else, performs an untimeout.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-03-07
     */
    private class Executor {
        
        private final IUser targetUser;
        private IChannel targetChannel;
        private IGuild targetGuild;
        private long timeout;
        private final MessageBuilder response;
        
        /**
         * Creates a new instance of this object with a certain user as a target.
         *
         * @param targetUser User targeted by the command.
         * @param response Message builder to be used for response messages.
         */
        public Executor( IUser targetUser, MessageBuilder response ) {
            
            this.targetUser = targetUser;
            targetChannel = null;
            targetGuild = null;
            timeout = -1;
            this.response = response;
            
        }
        
        /**
         * Sets the scope to be a certain channel.
         *
         * @param targetChannel Command scope.
         * @return A reference to this object.
         */
        public Executor withTargetChannel( IChannel targetChannel ) {
            
            this.targetChannel = targetChannel;
            this.targetGuild = null;
            return this;
            
        }
        
        /**
         * Sets the scope to be a certain guild.
         *
         * @param targetGuild Command scope.
         * @return A reference to this object.
         */
        public Executor withTargetGuild( IGuild targetGuild ) {
            
            this.targetGuild = targetGuild;
            this.targetChannel = null;
            return this;
            
        }
        
        /**
         * Sets the time the timeout should last (executes a timeout).
         *
         * @param untimeout Time the timeout should last for.
         * @return A reference to this object.
         */
        public Executor withTimeout( long timeout ) {
            
            this.timeout = timeout;
            return this;
            
        }
        
        /**
         * Executes the command appropriate for the conditions given.
         *
         * @throws IllegalStateException If the target scope wasn't set.
         */
        public void execute() throws IllegalStateException {
            
            if ( ( targetGuild == null ) && ( targetChannel == null ) ) {
                throw new IllegalStateException();
            }
            
            TimeoutController controller = TimeoutController.getInstance();
            if ( timeout == -1 ) {
                // Un-times out the user.
                if ( targetGuild != null ) { // For the guild.
                    controller.untimeout( targetUser, targetGuild );
                } else { // For the channel.
                    controller.untimeout( targetUser, targetChannel );
                }
                
            } else {
                boolean success; // Times out the user.
                if ( targetGuild != null ) { // For the guild.
                    success = controller.timeout( targetUser, targetGuild, timeout );
                } else { // For the channel.
                    success = controller.timeout( targetUser, targetChannel, timeout );
                }
                if ( !success ) {
                    response.withContent( "User is already timed out." ).build();
                }
            }
            
        }
        
    }

}
