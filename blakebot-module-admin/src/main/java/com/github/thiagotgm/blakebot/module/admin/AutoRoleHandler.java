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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Handler that applies the automatic role when a user joins a guild that configured
 * an automatic role.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-13
 */
public class AutoRoleHandler implements IListener<UserJoinEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger( AutoRoleHandler.class );
    
    private final AutoRoleManager manager;
    
    /**
     * Constructs a new instance.
     */
    public AutoRoleHandler() {
        
        this.manager = AutoRoleManager.getInstance();
        
    }

    /**
     * Upon a user join, checks if the guild has a configured auto-role, and if so,
     * attempts to apply it to the user.
     *
     * @param event The event fired.
     */
    @Override
    public void handle( UserJoinEvent event ) {

        IRole role = manager.get( event.getGuild() );
        if ( role != null ) {
            LOG.debug( "Auto-setting role \"{}\" for new user \"{}\" in guild \"{}\".",
                    role.getName(), event.getUser().getName(), event.getGuild().getName() );
            RequestBuffer.request( () -> {
                
                try {
                    event.getUser().addRole( role );
                } catch ( MissingPermissionsException e ) {
                    LOG.debug( "Does not have permission to set the role." );
                } catch ( DiscordException e ) {
                    LOG.error( "Error encountered while setting auto-role.", e );
                }
            
            });
        }
        
    }

}
