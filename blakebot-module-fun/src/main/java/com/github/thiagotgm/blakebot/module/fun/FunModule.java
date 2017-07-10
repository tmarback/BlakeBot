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

package com.github.thiagotgm.blakebot.module.fun;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'fun' module.
 * 
 * @author ThiagoTGM
 * @version 0.2.0
 * @since 2017-02-04
 */
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
