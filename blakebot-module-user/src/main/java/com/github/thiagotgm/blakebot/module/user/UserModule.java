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

package com.github.thiagotgm.blakebot.module.user;

import java.awt.Color;

import com.github.thiagotgm.modular_commands.api.CommandRegistry;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'user' module.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-04
 */
public class UserModule implements IModule {

    private static final String MODULE_NAME = "User";
    protected static final DailiesCommand DAILIES = new DailiesCommand();
    
    /**
     * Color for embeds of this module.
     */
    public static final Color EMBED_COLOR = Color.CYAN;
    
    IDiscordClient client;
    private LevelingManager levelManager = LevelingManager.getInstance();
    
    @Override
    public void disable() {
    	
    	client.getDispatcher().unregisterListener( levelManager );
        
        CommandRegistry.getRegistry( client ).removeSubRegistry( this ); // Remove commands.
        client = null; // Remove client.

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        client = arg0; // Store client.
        
        client.getDispatcher().registerListener( levelManager );
        
        CommandRegistry registry = CommandRegistry.getRegistry( arg0 ).getSubRegistry( this );
        registerCommands( registry ); // Register commands.
        
        return true;
        
    }

    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param registry Registry the commands should be registered in.
     */
    private void registerCommands( CommandRegistry registry ) {
        
        registry.registerAnnotatedCommands( new ProfileCommand() );
        registry.registerAnnotatedCommands( new CurrencyManagementCommands() );
        registry.registerAnnotatedCommands( DAILIES );
        
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
