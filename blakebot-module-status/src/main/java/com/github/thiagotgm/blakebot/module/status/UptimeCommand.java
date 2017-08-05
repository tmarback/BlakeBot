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
import java.lang.management.ManagementFactory;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;

import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that displays how long the bot has been connected to Discord.
 * 
 * @author ThiagoTGM
 * @version 1.2
 * @since 2017-01-01
 */
public class UptimeCommand {
    
    private static final String NAME = "Uptime";
    private static final String SUB_1_NAME = "Uptime Statistics";
    private static final String SUB_2_NAME = "Full Uptime Statistics";
    
    private final UptimeTracker tracker;
    
    /**
     * Initializes an instance.
     */
    public UptimeCommand() {
        
        this.tracker = UptimeTracker.getInstance();
        
    }
    
    /**
     * Prepares an embed with just the uptime.
     *
     * @return The configured embed builder.
     */
    private EmbedBuilder uptimeEmbed() {
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withColor( Color.RED );
        
        embedBuilder.appendField( "Connection uptime", tracker.getCurrentUptime().toString(), false );
        
        return embedBuilder;
        
    }
    
    @MainCommand(
            name = NAME,
            aliases = { "uptime", "up" },
            description = "Displays how long the bot has been up.",
            usage = "{}uptime|up",
            subCommands = SUB_1_NAME
    )
    public void uptimeCommand( CommandContext context ) {
        
        context.getReplyBuilder().withEmbed( uptimeEmbed().build() ).build();

    }
    
    /**
     * Prepares an embed with uptime statistics.
     *
     * @return The configured embed builder.
     */
    private EmbedBuilder statsEmbed() {
        
        EmbedBuilder embedBuilder = uptimeEmbed();
        
        embedBuilder.appendField( "Average uptime", tracker.getMeanUptime().toString(), true );
        embedBuilder.appendField( "Median uptime", tracker.getMedianUptime().toString(), true );
        embedBuilder.appendField( "Highest uptime", tracker.getMaximumUptime().toString(), true );
        embedBuilder.appendField( "Lowest uptime", tracker.getMinimumUptime().toString(), true );
        embedBuilder.appendField( "Uptime std. deviation", tracker.getUptimeStdDev().toString(),
                true );
        embedBuilder.appendField( "Total uptime", tracker.getTotalUptime().toString(), true );
        
        embedBuilder.appendField( "Disconnects", String.valueOf( tracker.getDisconnectAmount() ), false );
        
        return embedBuilder;
        
    }
    
    @SubCommand(
            name = SUB_1_NAME,
            aliases = { "stats" },
            description = "Displays some statistics about bot uptime.",
            usage = "{}uptime|up stats",
            requiresOwner = true,
            subCommands = SUB_2_NAME
    )
    public void detailsCommand( CommandContext context ) {
        
        context.getReplyBuilder().withEmbed( statsEmbed().build() ).build();
        
    }
    
    /**
     * Prepares an embed with full uptime/downtime statistics.
     *
     * @return The configured embed builder.
     */
    private EmbedBuilder fullEmbed() {
        
        EmbedBuilder embedBuilder = statsEmbed();
        
        embedBuilder.appendField( "Average downtime", tracker.getMeanDowntime().toString(), true );
        embedBuilder.appendField( "Median downtime", tracker.getMedianDowntime().toString(), true );
        embedBuilder.appendField( "Highest downtime", tracker.getMaximumDowntime().toString(), true );
        embedBuilder.appendField( "Lowest downtime", tracker.getMinimumDowntime().toString(), true );
        embedBuilder.appendField( "Downtime std. deviation", tracker.getDowntimeStdDev().toString(),
                true );
        embedBuilder.appendField( "Total downtime", tracker.getTotalDowntime().toString(), true );
        
        long jvmUptime = ManagementFactory.getRuntimeMXBean().getUptime();
        embedBuilder.appendField( "JVM runtime", new Time( jvmUptime ).toString(), false );
        
        return embedBuilder;
        
    }
    
    @SubCommand(
            name = SUB_2_NAME,
            aliases = { "full" },
            description = "Displays full statistics about bot uptime and downtime.",
            usage = "{}uptime|up stats full",
            requiresOwner = true
    )
    public void fullCommand( CommandContext context ) {
        
        context.getReplyBuilder().withEmbed( fullEmbed().build() ).build();
        
    }

}
