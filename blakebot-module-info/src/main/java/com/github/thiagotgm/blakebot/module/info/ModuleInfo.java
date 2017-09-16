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

import sx.blah.discord.handle.obj.IMessage;

/**
 * Information that describes a module.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-15
 */
public class ModuleInfo {
    
    /**
     * Maximum size of a description.
     */
    public static final int MAX_DESCRIPTION_LENGTH = IMessage.MAX_MESSAGE_LENGTH / 4;
    
    private final String alias;
    private final String name;
    private final String version;
    private final String highlight;
    private final String description;
    private final String[] info;
    
    /**
     * Constructs a new instance.
     * 
     * @param alias The alias for this information.
     * @param name The name of the module.
     * @param version The module version.
     * @param highlight The syntax highlighting to display the information with.
     *                  May be <tt>null</tt>.
     * @param description The description of the module.
     * @param info The additional info of the module.
     */
    ModuleInfo( String alias, String name, String version,
            String highlight, String description, String[] info )
                    throws IllegalArgumentException {
        
        if ( description.length() > MAX_DESCRIPTION_LENGTH ) {
            throw new IllegalArgumentException( "Description exceeds maximum size." );
        }
        
        int maxInfoSize = IMessage.MAX_MESSAGE_LENGTH - 
                ( highlight == null ? 0 : highlight.length() ) - 8;
        for ( String infoBlock : info ) { // Check that each info block fits in a message.
            
            if ( infoBlock.length() > maxInfoSize ) {
                throw new IllegalArgumentException( "Information block would not fit in a message." );
            }
            
        }
        
        this.alias = alias;
        this.name = name;
        this.version = version;
        this.highlight = ( highlight == null ) ? "" : highlight;
        this.description = description;
        this.info = info;
        
    }

    /**
     * Retrieves the alias of the described module.
     * 
     * @return The alias.
     */
    public String getAlias() {
    
        return alias;
        
    }

    /**
     * Retrieves the name of the described module.
     * 
     * @return The name.
     */
    public String getName() {
    
        return name;
        
    }

    /**
     * Retrieves the version of the described module.
     * 
     * @return The version.
     */
    public String getVersion() {
    
        return version;
        
    }

    /**
     * Retrieves the syntax highlighting to be used to display this info.
     * 
     * @return The syntaxh highlight. If none, returns the empty string.
     */
    public String getHighlight() {
    
        return highlight;
        
    }

    /**
     * Retrieves the description of the described module.
     * 
     * @return The description.
     */
    public String getDescription() {
    
        return description;
        
    }

    /**
     * Retrives the info of the described module.
     * 
     * @return The info. May be an empty array.
     */
    public String[] getInfo() {
    
        return info;
        
    }
    
}