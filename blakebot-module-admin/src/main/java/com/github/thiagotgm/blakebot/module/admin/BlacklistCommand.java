package com.github.thiagotgm.blakebot.module.admin;

import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Commands that manage a word blacklist.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class BlacklistCommand {
    
    private static final String NAME = "Blacklist";
    
    @MainCommand(
            prefix = AdminModule.PREFIX,
            name = NAME,
            alias = { "blacklist", "bl" },
            description = "Manages the message blacklist. A subcommand must be used.",
            usage = AdminModule.PREFIX + "blacklist|bl <subcommand>"
    )
    public void blacklistCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
    
        try {
            IUser author = event.getMessage().getAuthor();
            String content = author.getName() + "\n" + author.getDiscriminator() + "\n" + author.getID() + "\n";
            content += AdminModule.client.getUserByID( author.getID() );
            msgBuilder.withContent( content ).build();
        } catch ( DiscordException | MissingPermissionsException e ) {
            CommandHandlerD4J.logMissingPerms( event, NAME, e );
        } catch ( RateLimitException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
