package com.github.thiagotgm.blakebot.module.status;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'status' module.
 * 
 * @author ThiagoTGM
 * @version {@value #MODULE_VERSION}
 * @since 2016-12-31
 */
public class StatusModule implements IModule {
    
    private static final String MODULE_NAME = "Status Module";
    private static final String MODULE_VERSION = "0.0.1";
    
    @Override
    public void disable() {

        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        CommandHandlerD4J commandHandler;
        commandHandler = (CommandHandlerD4J) Ordinator.getCommandRegistry().getCommandHandler();
        return true;
        
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

        return MODULE_VERSION;
        
    }

}
