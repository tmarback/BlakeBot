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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.Bot;
import com.github.thiagotgm.blakebot.ConnectionStatusListener;
import com.github.thiagotgm.blakebot.Time;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that displays advanced bot information.
 * 
 * @author ThiagoTGM
 * @version 2.1
 * @since 2017-01-11
 */
public class StatusCommand implements ConnectionStatusListener {
    
    private static final String NAME = "Status";
    private static final String FAILURE_HANDLER = "handler";
    private static final Logger log = LoggerFactory.getLogger( StatusCommand.class );
    
    private long bootTime;
    private Time maxUptime;
    private Time minUptime;
    private long totalUptime;
    private int disconnects;
    
    public StatusCommand() {
        
        bootTime = System.currentTimeMillis();
        maxUptime = new Time( 0 );
        minUptime = new Time( 0 );
        totalUptime = 0;
        disconnects = 0;
        
    }
    
    @MainCommand(
            name = NAME,
            aliases = "status",
            description = "retrieves advanced information on bot status",
            usage = "{}status",
            requiresOwner = true,
            failureHandler = FAILURE_HANDLER
    )
    public void statusCommand( CommandContext context ) {
        
        /* Gets status values */
        Bot bot = Bot.getInstance();
        int publicAmount = bot.getPublicChannels().size();
        int privateAmount = bot.getPrivateChannels().size();
        int serverAmount = bot.getGuilds().size();
        String runtime = getRuntime().toString();
        String uptime = Bot.getInstance().getUptime().toString();
        String averageUptime = averageUptime().toString();
        String highestUptime = maxUptime.toString();
        String lowestUptime = minUptime.toString();
        
        /* Structures status message */
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.appendField( "Public channels", publicAmount + " channels", false );
        embedBuilder.appendField( "Private channels", privateAmount + " channels", false );
        embedBuilder.appendField( "Servers", serverAmount + " servers", false );
        embedBuilder.appendField( "Program runtime", runtime, false );
        embedBuilder.appendField( "Current uptime", uptime, false );
        embedBuilder.appendField( "Average uptime", averageUptime, false );
        embedBuilder.appendField( "Highest uptime", highestUptime, false );
        embedBuilder.appendField( "Lowest uptime", lowestUptime, false );
        embedBuilder.appendField( "Disconnects", String.valueOf( disconnects ), false );
        embedBuilder.withColor( Color.RED );
        
        /* Sends status message */ 
        context.getReplyBuilder().withEmbed( embedBuilder.build() ).build();
        
    }
    
    @FailureHandler( FAILURE_HANDLER )
    public void handler( CommandContext context, FailureReason reason ) {
        
        if ( reason == FailureReason.COMMAND_OPERATION_EXCEPTION ) {
            context.getReplyBuilder().withContent( "Exception thrown." ).build();
        }
        
    }
    
    /**
     * Gets the time that the bot has been running.
     * 
     * @return The time elapsed.
     */
    private Time getRuntime() {
        
        Time uptime = new Time( System.currentTimeMillis() - bootTime );
        
        log.debug( "Runtime: " + uptime.getTotalTime() + "ms = " + uptime.toString( false ) );
        return uptime;
        
    }
    
    /**
     * Calculate the average uptime between disconnects.
     *
     * @return The average uptime. If no disconnects happened, returns 0.
     */
    private Time averageUptime() {
        
        long time = ( disconnects == 0 ) ? 0 : ( totalUptime / disconnects );
        return new Time( time );
        
    }

    @Override
    public void connectionChange( boolean isConnected ) {

        if ( isConnected ) {
            return; // Just connected - do nothing.
        }
        
        Time lastUptime = Bot.getInstance().getLastUptime();
        long uptime = lastUptime.getTotalTime();
        long highestUptime = maxUptime.getTotalTime();
        long lowestUptime = minUptime.getTotalTime();
        
        if ( uptime > highestUptime ) {
            maxUptime = lastUptime; // New max runtime.
        }
        if ( ( lowestUptime == 0 ) || ( uptime < lowestUptime ) ) {
            minUptime = lastUptime; // New min runtime.
        }
        totalUptime += uptime;
        disconnects++;
        
    }

}
