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

package com.github.thiagotgm.blakebot.module.status;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import com.github.thiagotgm.bot_utils.storage.DatabaseStats;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.CommandStats;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that displays advanced bot information.
 * 
 * @author ThiagoTGM
 * @version 2.1
 * @since 2017-01-11
 */
public class StatsCommand {
    
    private static final String NAME = "Bot Statistics";
    private static final String DATABASE_NAME = "Database Statistics";
    
    @MainCommand(
            name = NAME,
            aliases = "stats",
            description = "Retrieves bot statistics.",
            usage = "{}stats",
            subCommands = DATABASE_NAME
    )
    public void statsCommand( CommandContext context ) {
        
        /* Configure builder */
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withColor( Color.RED );
        
        /* Channel and server counts */
        IDiscordClient client = context.getEvent().getClient();
        int channelAmount = client.getChannels().size();
        int serverAmount = client.getGuilds().size();
        embedBuilder.appendField( "Public Channels", channelAmount + " channels", true );
        embedBuilder.appendField( "Servers", serverAmount + " servers", true );
        
        /* Message stats */
        double uptime = TimeUnit.MILLISECONDS.toMinutes(
                UptimeTracker.getInstance().getTotalUptime().getTotalTime() );
        long publicMessages = MessageStats.getPublicMessageCount();
        long privateMessages = MessageStats.getPrivateMessageCount();
        long totalMessages = publicMessages + privateMessages;
        double averageMessages = totalMessages / uptime;
        embedBuilder.appendField( "Public Messages Received", publicMessages + " messages", true );
        embedBuilder.appendField( "Private Messages Received", privateMessages + " messages", true );
        embedBuilder.appendField( "Total Messages Received", totalMessages + " messages", true );
        embedBuilder.appendField( "Average Messages Received",
                String.format( "%.4f messages/min", averageMessages ), true );
        
        /* Command stats */
        long commands = CommandStats.getCount();
        double averageCommands = commands / uptime;
        embedBuilder.appendField( "Commands Executed", commands + " commands", true );
        embedBuilder.appendField( "Average Commands Executed",
                String.format( "%.4f commands/min", averageCommands ), true );
        
        /* Sends status message */ 
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();
        
    }
    
    @SubCommand(
            name = DATABASE_NAME,
            aliases = { "database", "db" },
            description = "Retrieves statistics about the bot's database.",
            usage = "{}stats database|db"
    )
    public void advancedStatsCommand( CommandContext context ) {
    	
    	/* Configure builder */
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withColor( Color.RED );
        
        /* Cache stats */
        long hits = DatabaseStats.getCacheHits();
        long misses = DatabaseStats.getCacheMisses();
        String hitRate;
        if ( hits + misses > 0 ) {
        	hitRate = String.format( "%.4f%%", ( 100.0 * hits ) / ( hits + misses ) );
        } else {
        	hitRate = "-";
        }
        embedBuilder.appendField( "Cache Hit Rate", hitRate, true );
        
        /* Fetch stats */
        long avgSuccess = DatabaseStats.getAverageFetchSuccessTime();
        embedBuilder.appendField( "Average Successful Fetch Time", ( avgSuccess == -1 ) ? 
        		"-" : avgSuccess + "ms", true );
        long avgFail = DatabaseStats.getAverageFetchFailTime();
        embedBuilder.appendField( "Average Failed Fetch Time", ( avgFail == -1 ) ?
        		"-" : avgFail + "ms", true );
        
        /* Sends status message */ 
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();
    	
    }

}
