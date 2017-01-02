package com.github.thiagotgm.blakebot.module.status;

import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.alphahelix00.ordinator.commands.SubCommand;
import com.github.thiagotgm.blakebot.Bot;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that displays the owner of the bot account.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2017-01-01
 */
public class OwnerCommand {
    
    private static final String NAME = "Owner";
    private static final String NAME_SUB_NAME = "Name";
    private static final String NICK_SUB_NAME = "Nick";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = "owner",
            description = "Displays the username (name) or nickname (nick) of the owner of this bot account.",
            usage = StatusModule.PREFIX + "owner <sub command>",
            subCommands = { NAME_SUB_NAME, NICK_SUB_NAME }
    )
    public void ownerCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        // Do nothing.

    }
    
    @SubCommand(
            prefix = StatusModule.PREFIX,
            name = NAME_SUB_NAME,
            alias = "name",
            description = "Displays the username of the owner of this bot account.",
            usage = StatusModule.PREFIX + "owner name"
    )
    public void nameCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            String message;
            try {
                message = "\u200BMy owner's username is " + Bot.getInstance().getOwner() + "!";
            } catch ( DiscordException e ) {
                message = "\u200BSorry, I could not retrieve my owner's name.";
            }
            try {
                msgBuilder.withContent( message ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        
    }
    
    @SubCommand(
            prefix = StatusModule.PREFIX,
            name = NICK_SUB_NAME,
            alias = "nick",
            description = "Displays the nickname of the owner of this bot account on this server.",
            usage = StatusModule.PREFIX + "owner nick"
    )
    public void nickCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            String message;
            try {
                String name = Bot.getInstance().getOwner( event.getMessage().getGuild() );
                if ( name != null ) {
                    message = "\u200BMy owner's nickname in this server is " + name + "!";
                } else {
                    message = "\u200BNo nickname found for my owner in this server.";
                }
            } catch ( DiscordException e ) {
                message = "\u200BSorry, I could not retrieve my owner's nickname.";
            }
            try {
                msgBuilder.withContent( message ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }

}
