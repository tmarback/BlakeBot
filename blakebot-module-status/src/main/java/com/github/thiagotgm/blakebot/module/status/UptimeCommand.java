package com.github.thiagotgm.blakebot.module.status;

import java.awt.Color;
import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.thiagotgm.blakebot.Bot;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that displays how long the bot has been connected to Discord.
 * 
 * @author ThiagoTGM
 * @version 1.2
 * @since 2017-01-01
 */
public class UptimeCommand {
    
    private static final String NAME = "Uptime";
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = { "uptime", "up" },
            description = "Displays how long the bot has been up.",
            usage = StatusModule.PREFIX + "uptime|up"
    )
    public void uptimeCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.appendField( "Connection uptime", Bot.getInstance().getUptime().toString(), false );
                embedBuilder.withColor( Color.RED );
                msgBuilder.withEmbed( embedBuilder.build() ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }

}
