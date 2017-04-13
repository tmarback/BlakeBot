package com.github.thiagotgm.blakebot.module.admin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

public class AutoRoleManager {
    
    private static final String FILENAME = "AutoRole.xml";
    private static final String FILEPATH = Paths.get( "data" ).toString();
    private static final String PATH = Paths.get( FILEPATH, FILENAME ).toString();
    
    private static final Logger log = LoggerFactory.getLogger( AutoRoleManager.class );
    
    private final Properties roles;
    
    private static AutoRoleManager instance;
    
    /**
     * Creates a new instance of this class. If the autorole file exists, load in
     * the server/role settings from it.
     */
    private AutoRoleManager() {
        
        roles = new Properties();
        
        // Opens autorole file.
        FileInputStream input = null;
        try {
            input = new FileInputStream( PATH );
        } catch ( FileNotFoundException e ) {
            log.info( "Autorole file not found. New one will be created." );
            return;
        }
        
        // Loads autorole file.
        try {
            roles.loadFromXML( input );
        } catch ( InvalidPropertiesFormatException e ) {
            log.error( "Autorole file has wrong format.", e );
        } catch ( IOException e ) {
            log.error( "Failed to read autorole file.", e );
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
    private synchronized void save() {
        
        FileOutputStream output;
        try {
            output = new FileOutputStream( PATH );
            roles.storeToXML( output, "Pairs of servers and the roles set for new users," +
                    " by their IDs. key = server ID, value = role ID." );
        } catch ( FileNotFoundException e ) {
            log.error( "Could not open autorole file for writing.", e );
        } catch ( IOException e ) {
            log.error( "Could not write to autorole file.", e );
        }
        
    }
    
    /**
     * Sets a role to autoset in a guild.
     *
     * @param guild Guild where the autorole should be set.
     * @param role Role to be set as autorole.
     */
    public void set( IGuild guild, IRole role ) {
        
        roles.setProperty( guild.getID(), role.getID() );
        save();
        
    }
    
    /**
     * Retrieves which role is set as the autorole in a server.
     *
     * @param guild Guild to check the autorole of.
     * @return The role set as autorole in that guild. Returns null if no autorole is set.
     */
    public IRole get( IGuild guild ) {
        
        String roleID = roles.getProperty( guild.getID() );
        return ( roleID != null ) ? guild.getRoleByID( roleID ) : null;
        
    }
    
    /**
     * Removes the autorole for a given guild.
     *
     * @param guild The guild for which autorole should be disabled.
     * @return true if autorole was disabled with this method call;
     *         false if it was not enabled.
     */
    public boolean remove( IGuild guild ) {
        
        Object roleID = roles.remove( guild.getID() );
        save();
        return ( roleID == null ) ? false : true;
        
    }

}
