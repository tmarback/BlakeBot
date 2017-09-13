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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.blakebot.common.SaveManager.Saveable;

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
public class AutoRoleManager implements Saveable {
    
    private static final Path PATH = Settings.DATA_PATH.resolve( "AutoRole.xml" );
    private static final Logger LOG = LoggerFactory.getLogger( AutoRoleManager.class );
    
    private final Properties roles;
    
    private static AutoRoleManager instance;
    
    /**
     * Creates a new instance of this class. If the autorole file exists, load in
     * the server/role settings from it.
     */
    private AutoRoleManager() {
        
        roles = new Properties();
        
        /* Open autorole file */
        File file = PATH.toFile();
        if ( !file.exists() ) {
            LOG.info( "Autorole file not found. New one will be created." );
            return;
        }
        
        LOG.info( "Loading autorole data." );
        
        /* Load autorole file */
        try {
            FileInputStream input = new FileInputStream( PATH.toFile() );
            roles.loadFromXML( input );
            input.close();
            LOG.debug( "Loaded autorole data." );
        } catch ( FileNotFoundException e ) {
            LOG.error( "Could not open autorole file.", e );
        } catch ( InvalidPropertiesFormatException e ) {
            LOG.error( "Autorole file has wrong format.", e );
        } catch ( IOException e ) {
            LOG.error( "Error reading autorole file.", e );
        }
        
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
     * Saves the server/role pairs to the autorole file.
     */
    @Override
    public synchronized void save() {
        
        LOG.info( "Saving Auto-role data..." );
        
        Path folders = PATH.getParent();
        if ( folders != null ) { // Ensure folders exist.
            try {
                Files.createDirectories( folders );
            } catch ( IOException e ) {
                LOG.error( "Failed to create blacklist file directories.", e );
                return;
            }
        }
        
        /* Save to file */
        try {
            FileOutputStream output = new FileOutputStream( PATH.toFile() );
            roles.storeToXML( output, "Pairs of servers and the roles set for new users," +
                    " by their IDs. key = server ID, value = role ID." );
            output.close();
        } catch ( FileNotFoundException e ) {
            LOG.error( "Could not open autorole file for writing.", e );
            return;
        } catch ( IOException e ) {
            LOG.error( "Error writing to autorole file.", e );
            return;
        }
        
        LOG.debug( "Saved." );
        
    }
    
    /**
     * Sets a role to autoset in a guild.
     *
     * @param guild Guild where the autorole should be set.
     * @param role Role to be set as autorole.
     */
    public void set( IGuild guild, IRole role ) {
        
        roles.setProperty( guild.getStringID(), role.getStringID() );
        
    }
    
    /**
     * Retrieves which role is set as the autorole in a server.
     *
     * @param guild Guild to check the autorole of.
     * @return The role set as autorole in that guild.
     *         Returns <tt>null</tt> if no autorole is set for the given guild.
     */
    public IRole get( IGuild guild ) {
        
        String roleID = roles.getProperty( guild.getStringID() );
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
        
        return roles.remove( guild.getStringID() ) != null;
        
    }

}
