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

package com.github.thiagotgm.blakebot.common.event;

import sx.blah.discord.api.IDiscordClient;

/**
 * Event fired after the bot failed to be logged out.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-07-29
 */
public class LogoutFailureEvent extends LogoutEvent {
    
    /**
     * Reasons why the logout might fail.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-07-29
     */
    public enum Reason {
            
        /**
         * The manager was processing its listener queue but it was interrupted.
         */
        QUEUE_INTERRUPTED,

        /**
         * A logout was requested to the client but it failed.
         */
        LOGOUT_FAILED
            
    }
    
    private final Reason reason;

    /**
     * Builds a new instance fired by the given client, with the given reason
     * why the logout failed.
     *
     * @param client The client that attempted to be logged out.
     * @param reason The reason why the logout failed.
     */
    public LogoutFailureEvent( IDiscordClient client, Reason reason ) {
        
        super( client );
        
        this.reason = reason;

    }
    
    /**
     * Retrieves the reason why the logout failed.
     *
     * @return The failure reason.
     */
    public Reason getReason() {
        
        return reason;
        
    }

}
