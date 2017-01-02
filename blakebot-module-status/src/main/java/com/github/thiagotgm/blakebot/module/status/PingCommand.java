package com.github.thiagotgm.blakebot.module.status;

import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that simply replies with "pong!".
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-31
 */
public class PingCommand {
    
    private static final String NAME = "Ping";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = "ping",
            description = "pings the bot, and gets a pong response",
            usage = StatusModule.PREFIX + "ping"
    )
    public void pingCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "\u200Bpong!" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }

}
