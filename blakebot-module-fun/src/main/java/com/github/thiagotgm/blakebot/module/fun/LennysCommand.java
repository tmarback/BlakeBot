package com.github.thiagotgm.blakebot.module.fun;

import java.util.List;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Collection of commands that send different Lennys.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2017-02-04
 */
public class LennysCommand {
    
    private static final String NAME_1 = "Lenny";
    private static final String NAME_2 = "Sneaky Lenny";
    private static final String NAME_3 = "Sensei Lenny";
    private static final String NAME_4 = "Lenny Army";
    private static final String NAME_5 = "Nosey Lenny";
    private static final String NAME_6 = "Brawler Lenny";
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_1,
            alias = "lenny",
            description = "Calls Lenny.",
            usage = FunModule.PREFIX + "lenny"
    )
    public void lennyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "( ͡° ͜ʖ ͡°)" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_1, e );
            }
            
        });      

    }
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_2,
            alias = "sneakylenny",
            description = "Calls Lenny's sneaky cousin.",
            usage = FunModule.PREFIX + "sneakylenny"
    )
    public void sneakyLennyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "┬┴┬┴┤ ͜ʖ ͡°) ├┬┴┬┴" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_2, e );
            }
            
        });       

    }
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_3,
            alias = "senseilenny",
            description = "Calls Lenny's wise cousin.",
            usage = FunModule.PREFIX + "senseilenny"
    )
    public void senseiLennyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "( ͡° ╭͜ʖ╮͡°)" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_3, e );
            }
            
        });

    }
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_4,
            alias = "lennyarmy",
            description = "Calls Lenny's family.",
            usage = FunModule.PREFIX + "lennyarmy"
    )
    public void lennyArmyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "( ͡°( ͡° ͜ʖ( ͡° ͜ʖ ͡°)ʖ ͡°) ͡°)" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_4, e );
            }
            
        });

    }
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_5,
            alias = "noseylenny",
            description = "Calls Lenny's nosey cousin.",
            usage = FunModule.PREFIX + "noseylenny"
    )
    public void noseyLennyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "(͡ ͡° ͜ つ ͡͡°)" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_5, e );
            }
            
        });

    }
    
    @MainCommand(
            prefix = FunModule.PREFIX,
            name = NAME_6,
            alias = "brawlerlenny",
            description = "Calls Lenny's brawler cousin.",
            usage = FunModule.PREFIX + "brawlerlenny"
    )
    public void brawlerLennyCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                msgBuilder.withContent( "(ง ͠° ͟ل͜ ͡°)ง" ).build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME_6, e );
            }
            
        });

    }

}
