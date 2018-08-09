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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

/**
 * Stores auto-role data, eg the role to be auto-set for each guild (that
 * specified one).
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-13
 */
public class AutoRoleManager {
    
    private static final Logger LOG = LoggerFactory.getLogger( AutoRoleManager.class );
    
    private final Map<String,String> roles;
    
    private static AutoRoleManager instance;
    
    /**
     * Creates a new instance of this class. If the autorole file exists, load in
     * the server/role settings from it.
     */
    private AutoRoleManager() {
        
    	LOG.info( "Initializing auto-role manager." );
        roles = DatabaseManager.getDatabase().getDataMap( "AutoRole" );
        
    }
    
    /**
     * Returns the current instance. If there isn't one, creates it.
     *
     * @return The AutoRoleManager instance.
     */
    public static AutoRoleManager getInstance() {
        
        if ( instance == null ) {
            instance = new AutoRoleManager();
        }
        return instance;
        
    }
    
    /**
     * Sets a role to autoset in a guild.
     *
     * @param guild Guild where the autorole should be set.
     * @param role Role to be set as autorole.
     */
    public void set( IGuild guild, IRole role ) {
        
    	LOG.debug( "Set auto-role in guild '{}' to '{}'.", guild.getName(), role.getName() );
        roles.put( guild.getStringID(), role.getStringID() );
        
    }
    
    /**
     * Retrieves which role is set as the autorole in a server.
     *
     * @param guild Guild to check the autorole of.
     * @return The role set as autorole in that guild.
     *         Returns <tt>null</tt> if no autorole is set for the given guild.
     */
    public IRole get( IGuild guild ) {
        
        String roleID = roles.get( guild.getStringID() );
        return ( roleID != null ) ? guild.getRoleByID( Long.valueOf( roleID ) ) : null;
        
    }
    
    /**
     * Removes the autorole for a given guild.
     *
     * @param guild The guild for which autorole should be disabled.
     * @return <tt>true</tt> if autorole was disabled with this method call;
     *         <tt>false</tt> if it was not enabled.
     */
    public boolean remove( IGuild guild ) {
        
    	LOG.debug( "Removed auto-role in guild '{}'.", guild.getName() );
        return roles.remove( guild.getStringID() ) != null;
        
    }

}
