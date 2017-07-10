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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Class that checks every message received for blacklisted content.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-11
 */
public class BlacklistEnforcer {
    
    private static final Logger log = LoggerFactory.getLogger( BlacklistEnforcer.class );
    
    private final Blacklist blacklist;
    
    public BlacklistEnforcer() {
        
        blacklist = Blacklist.getInstance();
        
    }
    
    /**
     * When a message is received, checks if it contains blacklisted content. If it does, deletes it.
     * 
     * @param event Event fired by the message.
     */
    @EventSubscriber
    public void onMessageReceivedEvent( MessageReceivedEvent event ) {
        
        IMessage message = event.getMessage();
        IUser author = message.getAuthor();
        IChannel channel = message.getChannel();
        IGuild guild = message.getGuild();
        String content = message.getContent();
        
        if ( author.equals( AdminModule.client.getOurUser() ) ) {
            return; // Ignores the bot's own messages.
        }
        
        List<String> restrictions = blacklist.getAllRestrictions( author, channel );
        log.trace( "Restrictions for author " + author.getName() + " in channel " + channel.getName() + " of guild " + 
                guild.getName() + ": " + restrictions );
        for ( String restriction : restrictions ) {
            
            if ( content.contains( restriction ) ) {
                log.debug( "Blacklist match: \"" + content + "\" from " + author.getName() + " in channel " +
                        channel.getName() + " of guild " + guild.getName() );
                RequestBuffer.request( () -> {
                    
                    try {
                        message.delete();
                    } catch ( MissingPermissionsException e ) {
                        log.warn( "Does not have permissions to delete message.", e );
                    } catch ( RateLimitException | DiscordException e ) {
                        log.warn( "Failed to delete message.", e );
                    }
                
                });
                return;
            }
            
        }
        
    }

}
