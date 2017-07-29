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

import com.github.thiagotgm.modular_commands.api.CommandRegistry;
import com.github.thiagotgm.modular_commands.registry.annotation.HasPrefix;

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
@HasPrefix( "^" )
public class AdminModule implements IModule {

    private static final String MODULE_NAME = "Admin";
    
    IDiscordClient client;
    private static final BlacklistEnforcer enforcer = new BlacklistEnforcer();
    
    
    @Override
    public void disable() {
        
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.unregisterListener( enforcer );
        
        TimeoutController controller = TimeoutController.getInstance();
        dispatcher.unregisterListener( controller );
        controller.terminate(); // Ensure timeouts are reverted.
        
        CommandRegistry.getRegistry( client ).removeSubRegistry( this );
        client = null;

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        client = arg0;
        
        CommandRegistry registry = CommandRegistry.getRegistry( arg0 ).getSubRegistry( this );
        registerCommands( registry );
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener( enforcer );
        dispatcher.registerListener( TimeoutController.getInstance() );
        return true;
        
    }

    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param registry Registry the commands should be registered in.
     */
    private void registerCommands( CommandRegistry registry ) {
        
        registry.registerAnnotatedCommands( new BlacklistCommand() );
        registry.registerAnnotatedCommands( new TimeoutCommand() );
        registry.registerAnnotatedCommands( new AutoRoleCommand() );
        
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
