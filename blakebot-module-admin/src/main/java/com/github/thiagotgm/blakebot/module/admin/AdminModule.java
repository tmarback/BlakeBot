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

import com.github.thiagotgm.blakebot.common.LogoutManager;
import com.github.thiagotgm.modular_commands.api.CommandRegistry;
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

    private static final String MODULE_NAME = "Admin";
    
    IDiscordClient client;
    private BlacklistEnforcer enforcer;
    private AutoRoleHandler roleHandler;
    
    
    @Override
    public void disable() {
        
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.unregisterListener( enforcer ); // Remove blacklist enforcer.
        dispatcher.unregisterListener( roleHandler ); // Remove autorole handler.
        
        TimeoutController controller = TimeoutController.getInstance();
        LogoutManager.getManager( client ).unregisterListener( controller );
        controller.terminate(); // Ensure timeouts are reverted.
        
        CommandRegistry.getRegistry( client ).removeSubRegistry( this ); // Remove commands.
        client = null; // Remove client.

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        client = arg0; // Store client.
        
        CommandRegistry registry = CommandRegistry.getRegistry( arg0 ).getSubRegistry( this );
        registerCommands( registry ); // Register commands.
        
        EventDispatcher dispatcher = client.getDispatcher();
        enforcer = new BlacklistEnforcer(); // Make blacklist enforcer.
        dispatcher.registerListener( enforcer );
        roleHandler = new AutoRoleHandler(); // Make autorole handler.
        dispatcher.registerListener( roleHandler );
        
        // Set timeout controller.
        LogoutManager.getManager( client ).registerListener( TimeoutController.getInstance() );
        
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
