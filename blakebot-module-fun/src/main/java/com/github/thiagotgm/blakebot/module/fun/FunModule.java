package com.github.thiagotgm.blakebot.module.fun;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;


public class FunModule implements IModule {

    private static final String MODULE_NAME = "Fun Module";
    
    public static final String PREFIX = "^";
    
    @Override
    public void disable() {

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        CommandHandlerD4J commandHandler;
        commandHandler = (CommandHandlerD4J) Ordinator.getCommandRegistry().getCommandHandler();
        registerCommands( commandHandler );
        return true;
        
    }
    
    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param handler Hander the commands should be registered with.
     */
    private void registerCommands( CommandHandlerD4J handler ) {
        
        handler.registerAnnotatedCommands( new LennysCommand() );
        handler.registerAnnotatedCommands( new SquareCommand() );
        
    }

    @Override
    public String getAuthor() {

        return "ThiagoTGM";
        
    }

    @Override
    public String getMinimumDiscord4JVersion() {

        return "2.7.0";
        
    }

    @Override
    public String getName() {

        return MODULE_NAME;
        
    }

    @Override
    public String getVersion() {

        return getClass().getPackage().getImplementationVersion();
        
    }

}
