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
 * Interface for a class that gets notified when the bot changes connection
 * status (connects or disconnects).
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-30
 */
public interface ConnectionStatusListener {
    
    /**
     * Event triggered when the bot changes connection status.
     * 
     * @param isConnected if true, the bot just connected.
     *                    if false, the bot just disconnected.
     */
    void connectionChange( boolean isConnected );

}
