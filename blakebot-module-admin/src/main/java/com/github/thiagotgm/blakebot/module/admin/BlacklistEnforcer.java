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

import java.util.Set;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.utils.AsyncTools;
import com.github.thiagotgm.blakebot.module.admin.Blacklist.Restriction;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Class that checks every message received for blacklisted content.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-11
 */
public class BlacklistEnforcer {
    
    private static final Logger LOG = LoggerFactory.getLogger( BlacklistEnforcer.class );
    
    private static final ThreadGroup THREADS = new ThreadGroup( "Blacklist Enforcer" );
    private static final Executor EXECUTOR = AsyncTools.createFixedThreadPool( THREADS, ( t, e ) -> {
        
        LOG.error( "Uncaught exception thrown while enforcing blacklist.", e );
        
    });
    
    private final Blacklist blacklist;
    
    /**
     * Constructs a new enforcer.
     */
    public BlacklistEnforcer() {
        
        this.blacklist = Blacklist.getInstance();
        
    }
    
    /**
     * When a message is received, checks if it contains blacklisted content. If it does, deletes it.
     * 
     * @param event Event fired by the message.
     */
    @EventSubscriber
    public void onMessageReceivedEvent( MessageReceivedEvent event ) {
        
        IMessage message = event.getMessage();
        IChannel channel = message.getChannel();
        if ( channel.isPrivate() ) {
            return; // Ignore private messages.
        }
        IUser author = message.getAuthor();
        IGuild guild = message.getGuild();
        String content = message.getContent();
        
        // Check for match asynchronously.
        EXECUTOR.execute( () -> {
            
            Set<Restriction> restrictions = blacklist.getAllRestrictions( author, channel );
            LOG.trace( "Restrictions for author \"{}\" in channel \"{}\" of guild \"{}\": {}.",
                    author.getName(), channel.getName(), guild.getName(), restrictions );
            for ( Restriction restriction : restrictions ) {
                
                if ( restriction.test( content ) ) { // Found restriction in message.
                    LOG.debug( "Blacklist match: \"{}\" from \"{}\" in channel \"{}\" of guild \"{}\""
                            + " - matches {}.", content, author.getName(), channel.getName(),
                            guild.getName(), restriction );
                    RequestBuffer.request( () -> {
                        
                        try { // Attempt to delete the message.
                            message.delete();
                        } catch ( MissingPermissionsException e ) {
                            LOG.debug( "Does not have permissions to delete message.", e );
                        } catch ( DiscordException e ) {
                            LOG.error( "Failed to delete message.", e );
                        }
                    
                    });
                    return;
                }
                
            }
            
        });
        
    }

}
