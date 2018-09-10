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
 * <p>
 * Setting values are extracted from 3 different files, in the following precedence order
 * (highest precedence to lowest precedence):
 * <ul>
 * 	<li>{@value #SETTINGS_FILE}: Values specified by the user.</li>
 * 	<li>{@value #DEFAULTS_FILE}: Default values specified by the bot maker (optional).</li>
 * 	<li>{@value #LIB_DEFAULTS_FILE}: Default values specified by this library.</li>
 * </ul>
 * When a setting is retrieved, it uses the value specified in the highest precedence file
 * where that setting is present.
 * <p>
 * When the value of a setting is changed, it will be reflected in the
 * {@link #SETTINGS_FILE user-defined settings} file. Changes aren't written to the file
 * immediately, but rather are cached in memory and flushed at regular intervals as
 * defined by {@link SaveManager}.
 *
 * @version 1.1
 * @author ThiagoTGM
 * @since 2017-07-28
 */
public class Settings implements SaveManager.Saveable {
    
    private static final Logger LOG = LoggerFactory.getLogger( Settings.class );
    
    private static final int FILE_ERROR = 2;
    
    /**
     * File with default values provided by this library are stored, which takes
     * the lowest precedence. A settings in this file is used only if there is no
     * override for its value in the other files.
     */
    public static final String LIB_DEFAULTS_FILE = "defaultLibSettings.xml";
    /**
     * File with default values provided by the bot, which takes the second lowest
     * precedence. A settings in this file is used if there is no override for its
     * value in the user-set file, and overrides any value it may have in the library
     * defaults file.
     * <p>
     * The existence of this file is optional. If it does not exist, then there are
     * no bot-default values (same as if it existed but specified no values).
     */
    public static final String DEFAULTS_FILE = "defaultSettings.xml";
    /**
     * File with settings specified by the user. It takes the highest precedence,
     * which means a value specified for a setting here is used even if other
     * values are provided in the default files.
     * <p>
     * If it does not exist at startup, it is created.
     */
    public static final String SETTINGS_FILE = "settings.xml";
    private static final String SETTINGS_COMMENT = "Bot settings.";
    
    private static final Properties SETTINGS;

    static {
    	
    	/* Reads library-default settings */
        Properties libDefaults = new Properties();
        try {
            ClassLoader loader = Settings.class.getClassLoader();
            InputStream input = loader.getResourceAsStream( LIB_DEFAULTS_FILE );
            libDefaults.loadFromXML( input );
            input.close();
            LOG.info( "Loaded library-default properties." );
        } catch ( IOException e ) {
            LOG.error( "Error reading library-default properties file.", e );
            System.exit( FILE_ERROR );
        }
        
        /* Reads default settings */
        Properties defaults = new Properties( libDefaults );
        try {
            ClassLoader loader = Settings.class.getClassLoader();
            InputStream input = loader.getResourceAsStream( DEFAULTS_FILE );
            if ( input != null ) { // Found bot-default properties.
	            defaults.loadFromXML( input );
	            input.close();
	            LOG.info( "Loaded bot-default properties." );
            } else { // Did not find bot-default properties.
            	LOG.info( "No bot-default properties found." );
            }
        } catch ( IOException e ) {
            LOG.error( "Error reading bot-default properties file.", e );
            System.exit( FILE_ERROR );
        }

        /* Reads settings */
        SETTINGS = new Properties( defaults );
        try {
            FileInputStream input = new FileInputStream( SETTINGS_FILE );
            SETTINGS.loadFromXML( input );
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
        
        return SETTINGS.getProperty( setting ) != null;
        
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
        return SETTINGS.getProperty( setting );
        
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
            return Long.valueOf( SETTINGS.getProperty( setting ) );
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
            return Integer.valueOf( SETTINGS.getProperty( setting ) );
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
        String str = SETTINGS.getProperty( setting );
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
        SETTINGS.setProperty( setting, value );
        
    }
    
    /**
     * Sets the value of a long-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, long value ) {
        
        SETTINGS.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Sets the value of an int-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, int value ) {
        
        SETTINGS.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Sets the value of a boolean-valued setting.
     *
     * @param setting The name of the setting.
     * @param value The value of the setting.
     */
    public synchronized static void setSetting( String setting, boolean value ) {
        
        SETTINGS.setProperty( setting, String.valueOf( value ) );
        
    }
    
    /**
     * Saves the settings to the settings file.
     */
    public synchronized static void saveSettings() {
        
        LOG.info( "Saving settings..." );
        FileOutputStream file;
        try {
            file = new FileOutputStream( SETTINGS_FILE );
            SETTINGS.storeToXML( file, SETTINGS_COMMENT );
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
