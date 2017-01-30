package com.github.thiagotgm.blakebot.module.status;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import com.github.thiagotgm.blakebot.Bot;
import com.github.thiagotgm.blakebot.ConnectionStatusListener;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Command that displays advanced bot information.
 * 
 * @author ThiagoTGM
 * @version 2.0
 * @since 2017-01-11
 */
public class StatusCommand implements ConnectionStatusListener {
    
    private static final String NAME = "Status";
    private static final Logger log = LoggerFactory.getLogger( StatusCommand.class );
    
    private long bootTime;
    private long highestUptime;
    private long lowestUptime;
    private long totalUptime;
    private int disconnects;
    
    public StatusCommand() {
        
        bootTime = System.currentTimeMillis();
        highestUptime = 0;
        lowestUptime = 0;
        totalUptime = 0;
        disconnects = 0;
        
    }
    
    @MainCommand(
            prefix = StatusModule.PREFIX,
            name = NAME,
            alias = "status",
            description = "retrieves advanced information on bot status",
            usage = StatusModule.PREFIX + "status"
    )
    public void pingCommand( List<String> args, MessageReceivedEvent event, MessageBuilder msgBuilder ) {
        
        RequestBuffer.request( () -> {
            
            try {
                /* Gets status values */
                Bot bot = Bot.getInstance();
                int publicAmount = bot.getPublicChannels().size();
                int privateAmount = bot.getPrivateChannels().size();
                int serverAmount = bot.getGuilds().size();
                String runtime = getRuntime();
                String uptime = Bot.uptimeString( Bot.parseUptime( Bot.getInstance().getUptime() ) );
                String averageUptime = Bot.uptimeString( Bot.parseUptime( averageUptime() ) );
                String maxUptime = Bot.uptimeString( Bot.parseUptime( highestUptime ) );
                String minUptime = Bot.uptimeString( Bot.parseUptime( lowestUptime ) );
                
                /* Structures status message */
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.appendField( "Public channels", publicAmount + " channels", false );
                embedBuilder.appendField( "Private channels", privateAmount + " channels", false );
                embedBuilder.appendField( "Servers", serverAmount + " servers", false );
                embedBuilder.appendField( "Program runtime", runtime, false );
                embedBuilder.appendField( "Current uptime", uptime, false );
                embedBuilder.appendField( "Average uptime", averageUptime, false );
                embedBuilder.appendField( "Highest uptime", maxUptime, false );
                embedBuilder.appendField( "Lowest uptime", minUptime, false );
                embedBuilder.appendField( "Disconnects", String.valueOf( disconnects ), false );
                embedBuilder.withColor( Color.RED );
                
                /* Sends status message */ 
                msgBuilder.withEmbed( embedBuilder.build() );
                msgBuilder.build();
            } catch ( DiscordException | MissingPermissionsException e ) {
                CommandHandlerD4J.logMissingPerms( event, NAME, e );
            }
            
        });
        

    }
    
    /**
     * Gets the time that the bot has been running.
     * 
     * @return The time elapsed in the form "dd days, hh hours, mm minutes".
     */
    private String getRuntime() {
        
        long time = System.currentTimeMillis() - bootTime;
        long[] uptime = Bot.parseUptime( time );
        
        log.debug( "Runtime: " + time + "ms = " + uptime[0] + "d | " + uptime[1] +
                "h | " + uptime[2] + "m" );
        return Bot.uptimeString( uptime );
        
    }
    
    /**
     * Calculate the average uptime between disconnects.
     *
     * @return The average uptime. If no disconnects happened, returns 0.
     */
    private long averageUptime() {
        
        return ( disconnects == 0 ) ? 0 : ( totalUptime / disconnects );
        
    }

    @Override
    public void connectionChange( boolean isConnected ) {

        if ( isConnected ) {
            return; // Just connected - do nothing.
        }
        long uptime = Bot.getInstance().getLastUptime();
        if ( uptime > highestUptime ) {
            highestUptime = uptime; // New max runtime.
        }
        if ( ( lowestUptime == 0 ) || ( uptime < lowestUptime ) ) {
            lowestUptime = uptime; // New min runtime.
        }
        totalUptime += uptime;
        disconnects++;
        
    }

}
