package com.github.thiagotgm.blakebot.module.admin;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.alphahelix00.ordinator.commands.SubCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

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
    private static final String SUB_NAME = "Un/Timeout Server";
    private static final String SUB_ALIAS = "server";
    
    private final Hashtable<String, List<Executor>> executors;
    
    /**
     * Creates a new instance of this class.
     */
    public TimeoutCommand() {
        
        executors = new Hashtable<>();
        
    }
    
    @MainCommand(
            prefix = AdminModule.PREFIX,
            name = NAME_1,
            alias = { "timeout", "to" },
            description = "Times a user out.",
            usage = AdminModule.PREFIX + "timeout|to <user>",
            subCommands = { SUB_NAME }
    )
    public void timeoutCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
   
        if ( args.isEmpty() ) {
            RequestBuffer.request( () -> {
                
                try {
                    msgBuilder.withContent( "Please specify a user(s) to be timed out." ).build();
                } catch ( DiscordException | MissingPermissionsException e ) {
                    CommandHandlerD4J.logMissingPerms( event, NAME_1, e );
                }
                
            });
        }
        
        IChannel channel = event.getMessage().getChannel();
        boolean hasSub = args.get( 0 ).equals( SUB_ALIAS );
        List<Executor> execs = new LinkedList<>();
        
        for ( IUser target : event.getMessage().getMentions() ) {
            
            Executor exec = new Executor( target );
            if ( hasSub ) { // Has subcommand, stores for it.
                execs.add( exec );
            } else { // No subcommand, run now.
                exec.withTargetChannel( channel ).execute();
            }
            
        }
        if ( !execs.isEmpty() ) {
            executors.put( event.getMessage().getID(), execs );
        }
        
    }
    
    @MainCommand(
            prefix = AdminModule.PREFIX,
            name = NAME_2,
            alias = { "untimeout", "uto" },
            description = "Un-times out a user.",
            usage = AdminModule.PREFIX + "untimeout|uto <user>",
            subCommands = { SUB_NAME }
    )
    public void untimeoutCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
   
        if ( args.isEmpty() ) {
            RequestBuffer.request( () -> {
                
                try {
                    msgBuilder.withContent( "Please specify a user(s) to be timed out." ).build();
                } catch ( DiscordException | MissingPermissionsException e ) {
                    CommandHandlerD4J.logMissingPerms( event, NAME_2, e );
                }
                
            });
        }
        
        IChannel channel = event.getMessage().getChannel();
        boolean hasSub = args.get( 0 ).equals( SUB_ALIAS );
        List<Executor> execs = new LinkedList<>();
        
        for ( IUser target : event.getMessage().getMentions() ) {
            
            Executor exec = new Executor( target ).isUntimeout( true );
            if ( hasSub ) { // Has subcommand, stores for it.
                execs.add( exec );
            } else { // No subcommand, run now.
                exec.withTargetChannel( channel ).execute();
            }
            
        }
        if ( !execs.isEmpty() ) {
            executors.put( event.getMessage().getID(), execs );
        }
        
    }
    
    @SubCommand(
            prefix = AdminModule.PREFIX,
            name = SUB_NAME,
            alias = { SUB_ALIAS },
            description = "Applies the (un)timeout for the entire server.",
            usage = AdminModule.PREFIX + "timeout|to/untimeout|uto server <user>"
    )
    public void serverSubCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
    
        List<Executor> execs = executors.remove( event.getMessage().getID() );
        if ( execs != null ) { // If there are users to time out.
            IGuild guild = event.getMessage().getGuild();
            for ( Executor exec : execs ) {
                // Executes for each user with a guild-wide scope.
                exec.withTargetGuild( guild ).execute();
                
            }
        }
        
    }
    
    /**
     * Executor class that calls in the appropriate (un)timeout for a particular scope.
     * Will only use the last scope set (either a channel or a guild).
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-03-07
     */
    private class Executor {
        
        private final IUser targetUser;
        private IChannel targetChannel;
        private IGuild targetGuild;
        private boolean untimeout;
        
        /**
         * Creates a new instance of this object with a certain user as a target.
         *
         * @param targetUser User targeted by the command.
         */
        public Executor( IUser targetUser ) {
            
            this.targetUser = targetUser;
            targetChannel = null;
            targetGuild = null;
            untimeout = false;
            
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
         * Sets whether the command is a timeout or un-timeout.
         * By default (without calling this method), it is a timeout.
         *
         * @param untimeout If true, will be set to untimeout. If false, set to
         *                  timeout.
         * @return A reference to this object.
         */
        public Executor isUntimeout( boolean untimeout ) {
            
            this.untimeout = untimeout;
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
            if ( untimeout ) {
                // Un-times out the user.
                if ( targetGuild != null ) { // For the guild.
                    controller.untimeout( targetUser, targetGuild );
                } else { // For the channel.
                    controller.untimeout( targetUser, targetChannel );
                }
            } else {
                // Times out the user.
                if ( targetGuild != null ) { // For the guild.
                    controller.timeout( targetUser, targetGuild );
                } else { // For the channel.
                    controller.timeout( targetUser, targetChannel );
                }
            }
            
        }
        
    }

}
