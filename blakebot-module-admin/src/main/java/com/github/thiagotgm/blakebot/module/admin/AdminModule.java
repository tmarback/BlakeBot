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

package com.github.thiagotgm.blakebot.module.admin;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'admin' module.
 * 
 * @author ThiagoTGM
 * @version 0.1.0
 * @since 2017-02-04
 */
public class AdminModule implements IModule {

    private static final String MODULE_NAME = "Admin Module";
    
    public static final String PREFIX = "^";
    static IDiscordClient client;
    private static final BlacklistEnforcer enforcer = new BlacklistEnforcer();
    
    
    @Override
    public void disable() {
        
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.unregisterListener( enforcer );
        TimeoutController.getInstance().terminate();
        AdminModule.client = null;

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        AdminModule.client = arg0;
        
        CommandHandlerD4J commandHandler;
        commandHandler = (CommandHandlerD4J) Ordinator.getCommandRegistry().getCommandHandler();
        registerCommands( commandHandler );
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener( enforcer );
        return true;
        
    }

    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param handler Hander the commands should be registered with.
     */
    private void registerCommands( CommandHandlerD4J handler ) {
        
        handler.registerAnnotatedCommands( new BlacklistCommand() );
        handler.registerAnnotatedCommands( new TimeoutCommand() );
        handler.registerAnnotatedCommands( new AutoRoleCommand() );
        
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
