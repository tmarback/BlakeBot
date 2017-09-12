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

package com.github.thiagotgm.blakebot.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages the settings for the bot.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-28
 */
public class Settings implements SaveManager.Saveable {
    
    private static final Logger LOG = LoggerFactory.getLogger( Settings.class );
    
    private static final int FILE_ERROR = 2;
    private static final String DEFAULTS_FILE = "defaultProperties.xml";
    private static final String SETTINGS_FILE = "properties.xml";
    private static final String SETTINGS_COMMENT = "Bot settings.";
    
    private static final Properties settings;

    static {
        
        /* Reads default settings */
        Properties defaults = new Properties();
        try {
            ClassLoader loader = Settings.class.getClassLoader();
            InputStream input = loader.getResourceAsStream( DEFAULTS_FILE );
            defaults.loadFromXML( input );
            input.close();
            LOG.info( "Loaded default properties." );
        } catch ( IOException e ) {
            LOG.error( "Error reading default properties file.", e );
            System.exit( FILE_ERROR );
        }

        /* Reads settings */
        settings = new Properties( defaults );
        try {
            FileInputStream input = new FileInputStream( SETTINGS_FILE );
            settings.loadFromXML( input );
            input.close();
            LOG.info( "Loaded bot properties." );
        } catch ( FileNotFoundException e ) {
            LOG.error(
                    "Properties file not found. A new one will be created." );
        } catch ( IOException e ) {
            LOG.error( "Error reading properties file.", e );
            System.exit( FILE_ERROR );
        }
        
    }
    
    /**
     * Retrieves whether a setting exists.
     *
     * @param setting The name of the setting.
     * @return true if there is a setting with the given name.
     *         false otherwise.
     */
    public synchronized static boolean hasSetting( String setting ) {
        
        return settings.getProperty( setting ) != null;
        
    }
    
    /**
     * Retrieves the value of a <b>String</b>-valued setting.
     *
     * @param setting The name of the setting.
     * @return The value of the setting.
     * @throws IllegalArgumentException if the setting does not exist.
     */
    public synchronized static String getStringSetting( String setting ) throws IllegalArgumentException {
        
        if ( !hasSetting( setting ) ) { // Check if setting exists.
            throw new IllegalArgumentException( "Setting does not exist." );
        }
        return settings.getProperty( setting );
        
    }
    
    /**
     * Retrieves the value of a <b>long</b>-valued setting.
     *
     * @param setting The name of the setting.
     * @return The value of the setting.
     * @throws IllegalArgumentException if the setting does not exist or it does not have a
     *                                  <b>long</b> value.
     */
    public synchronized static long getLongSetting( String setting ) throws IllegalArgumentException {
        
        if ( !hasSetting( setting ) ) { // Check if setting exists.
            throw new IllegalArgumentException( "Setting does not exist." );
        }
        try {
            return Long.valueOf( settings.getProperty( setting ) );
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException( "Setting does not have a long value.", e );
        }
        
    }
    
    /**
     * Retrieves the value of an <b>int</b>-valued setting.
     *
     * @param setting The name of the setting.
     * @return The value of the setting.
     * @throws IllegalArgumentException if the setting does not exist or it does not have a
     *                                  <b>int</b> value.
     */
    public synchronized static int getIntSetting( String setting ) throws IllegalArgumentException {
        
        if ( !hasSetting( setting ) ) { // Check if setting exists.
            throw new IllegalArgumentException( "Setting does not exist." );
        }
        try {
            return Integer.valueOf( settings.getProperty( setting ) );
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException( "Setting does not have a int value.", e );
        }
        
    }
    
    /**
     * Retrieves the value of a <b>boolean</b>-valued setting.
     *
     * @param setting The name of the setting.
     * @return The value of the setting.
     * @throws IllegalArgumentException if the setting does not exist or it does not have a
     *                                  <b>boolean</b> value.
     */
    public synchronized static boolean getBooleanSetting( String setting ) throws IllegalArgumentException {
        
        if ( !hasSetting( setting ) ) { // Check if setting exists.
            throw new IllegalArgumentException( "Setting does not exist." );
        }
        String str = settings.getProperty( setting );
        boolean value = Boolean.valueOf( str );
        if ( !str.equalsIgnoreCase( String.valueOf( value ) ) ) {
            throw new IllegalArgumentException( "Setting does not have a boolean value." );
        }
        return value;
        
    }
    
    /**
     * Sets the value of a String-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     * @throws NullPointerException if the value given is null.
     */
    public synchronized static void setSetting( String setting, String value ) throws NullPointerException {

        if ( value == null ) {
            throw new NullPointerException( "Value cannot be null." );
        }
        settings.setProperty( setting, value );
        
    }
    
    /**
     * Sets the value of a long-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, long value ) {
        
        settings.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Sets the value of an int-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, int value ) {
        
        settings.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Sets the value of a boolean-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, boolean value ) {
        
        settings.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Saves the settings to the settings file.
     */
    public synchronized static void saveSettings() {
        
        LOG.info( "Saving properties." );
        FileOutputStream file;
        try {
            file = new FileOutputStream( SETTINGS_FILE );
            settings.storeToXML( file, SETTINGS_COMMENT );
            file.close();
        } catch ( FileNotFoundException e ) {
            LOG.error( "Could not open properties file.", e );
        } catch ( IOException e ) {
            LOG.error( "Could not write to properties file.", e );
        }
        
    }
    
    static {
        // Register for autosaving.
        SaveManager.registerListener( new Settings() );
        
    }
    
    /**
     * Creates a new instance.
     */
    private Settings() {}
    
    @Override
    public void save() {
        
        saveSettings();
        
    }

}
