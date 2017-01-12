package com.github.thiagotgm.blakebot.module.status;

import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.thiagotgm.blakebot.Bot;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that displays the amount of private and public channels, and servers that
 * the bot is connected to.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2017-01-11
 */
public class StatusCommand {
    
    private static final String NAME = "Status";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = "status",
            description = "retrieves how many channels and servers the bot is connected to.",
            usage = StatusModule.PREFIX + "status"
    )
    public void pingCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                Bot bot = Bot.getInstance();
                int publicAmount = bot.getPublicChannels().size();
                int privateAmount = bot.getPrivateChannels().size();
                int serverAmount = bot.getGuilds().size();
                msgBuilder.withContent( "Public: " + publicAmount + " channels\nPrivate: " +
                privateAmount + " channels\n" + serverAmount + " servers" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }

}
