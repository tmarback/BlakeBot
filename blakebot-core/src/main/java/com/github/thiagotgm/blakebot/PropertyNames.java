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

package com.github.thiagotgm.blakebot;

/**
 * Constants with the String keys for the bot properties, as well as the
 * name of the property files and the comments for them.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-30
 */
public interface PropertyNames {
    
    String DEFAULTS_FILE = "defaultProperties.xml";
    String DEFAULTS_COMMENT = "Default values for bot properties.";
    String PROPERTIES_FILE = "properties.xml";
    String PROPERTIES_COMMENT = "Bot properties.";
    
    String LOGIN_TOKEN = "token";
    String CONSOLE_WIDTH = "width";
    String CONSOLE_HEIGHT = "height";

}
