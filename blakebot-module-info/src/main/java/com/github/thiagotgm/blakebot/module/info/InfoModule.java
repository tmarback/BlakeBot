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

package com.github.thiagotgm.blakebot.module.info;

import com.github.thiagotgm.modular_commands.api.CommandRegistry;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the "info" module.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-15
 */
public class InfoModule implements IModule {
    
    private static final String MODULE_NAME = "Info";
    
    private IDiscordClient client;

    @Override
    public void disable() {

        CommandRegistry.getRegistry( client ).removeSubRegistry( this );
        client = null;

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        client = arg0;
        
        CommandRegistry registry;
        registry = CommandRegistry.getRegistry( arg0 ).getSubRegistry( this );
        registerCommands( registry );

        return true;
        
    }
    
    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param registry Registry the commands should be registered with.
     */
    private void registerCommands( CommandRegistry registry ) {
        
        registry.registerAnnotatedCommands( new InfoCommand() );
        registry.registerAnnotatedCommands( new ModuleCommand() );
        
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
