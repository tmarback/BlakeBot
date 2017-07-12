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

package com.github.thiagotgm.blakebot.module.status;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;
import com.github.thiagotgm.blakebot.Bot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'status' module.
 * 
 * @author ThiagoTGM
 * @version 1.0.0
 * @since 2016-12-31
 */
public class StatusModule implements IModule {
    
    private static final String MODULE_NAME = "Status";
    
    public static final String PREFIX = "^";
    
    private StatusCommand statusCommand;
    
    @Override
    public void disable() {
        
        Bot.unregisterListener( statusCommand );
        
    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        CommandHandlerD4J commandHandler;
        commandHandler = (CommandHandlerD4J) Ordinator.getCommandRegistry().getCommandHandler();
        registerCommands( commandHandler );
        Bot.registerListener( statusCommand );
        return true;
        
    }
    
    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param handler Hander the commands should be registered with.
     */
    private void registerCommands( CommandHandlerD4J handler ) {
        
        handler.registerAnnotatedCommands( new PingCommand() );
        handler.registerAnnotatedCommands( new UptimeCommand() );
        handler.registerAnnotatedCommands( new OwnerCommand() );
        statusCommand = new StatusCommand();
        handler.registerAnnotatedCommands( statusCommand );
        
    }

    @Override
    public String getAuthor() {

        return "ThiagoTGM";
        
    }

    @Override
    public String getMinimumDiscord4JVersion() {

        return getClass().getPackage().getSpecificationVersion();
                
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
