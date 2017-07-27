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

package com.github.thiagotgm.blakebot.module.fun;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

/**
 * Collection of commands that send different Lennys.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2017-02-04
 */
public class LennysCommand {
    
    private static final String NAME_1 = "Lenny";
    private static final String NAME_2 = "Sneaky Lenny";
    private static final String NAME_3 = "Sensei Lenny";
    private static final String NAME_4 = "Lenny Army";
    private static final String NAME_5 = "Nosey Lenny";
    private static final String NAME_6 = "Brawler Lenny";
    
    @MainCommand(
            name = NAME_1,
            aliases = "lenny",
            description = "Calls Lenny.",
            usage = "{}lenny"
    )
    public void lennyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "( ͡° ͜ʖ ͡°)" ).build();  

    }
    
    @MainCommand(
            name = NAME_2,
            aliases = "sneakylenny",
            description = "Calls Lenny's sneaky cousin.",
            usage = "{}sneakylenny"
    )
    public void sneakyLennyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "┬┴┬┴┤ ͜ʖ ͡°) ├┬┴┬┴" ).build();      

    }
    
    @MainCommand(
            name = NAME_3,
            aliases = "senseilenny",
            description = "Calls Lenny's wise cousin.",
            usage = "{}senseilenny"
    )
    public void senseiLennyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "( ͡° ╭͜ʖ╮͡°)" ).build();

    }
    
    @MainCommand(
            name = NAME_4,
            aliases = "lennyarmy",
            description = "Calls Lenny's family.",
            usage = "{}lennyarmy"
    )
    public void lennyArmyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "( ͡°( ͡° ͜ʖ( ͡° ͜ʖ ͡°)ʖ ͡°) ͡°)" ).build();

    }
    
    @MainCommand(
            name = NAME_5,
            aliases = "noseylenny",
            description = "Calls Lenny's nosey cousin.",
            usage = "{}noseylenny"
    )
    public void noseyLennyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "(͡ ͡° ͜ つ ͡͡°)" ).build();

    }
    
    @MainCommand(
            name = NAME_6,
            aliases = "brawlerlenny",
            description = "Calls Lenny's brawler cousin.",
            usage = "{}brawlerlenny"
    )
    public void brawlerLennyCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "(ง ͠° ͟ل͜ ͡°)ง" ).build();

    }

}
