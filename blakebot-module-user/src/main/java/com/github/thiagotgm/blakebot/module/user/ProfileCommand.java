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

package com.github.thiagotgm.blakebot.module.user;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.module.user.LevelingManager.LevelState;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.Argument.Type;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Command to display user profile information.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-04
 */
public class ProfileCommand {
	
	private static final Clock CLOCK = Clock.systemDefaultZone();
	private static final int EXP_BAR_SIZE = 10;
	
	private final Map<String,String> infoData;
	
	public ProfileCommand() {
		
		infoData = DatabaseManager.getDatabase().getDataMap( "CustomInfo",
				new StringTranslator(), new StringTranslator() );
		
	}
	
	@MainCommand(
			name = "Profile command",
			aliases = { "profile","mystats" },
			description = "Displays user information. If no user is specified, shows"
					+ " info of the calling user.",
			usage = "{}profile|mystats [user]"
			)
	public void profileCommand( CommandContext context ) {
		
		IUser user;
		List<Argument> args = context.getArguments();
		if ( !args.isEmpty() && ( args.get( 0 ).getType() == Type.USER_MENTION ) ) {
			user = (IUser) args.get( 0 ).getArgument();
		} else {
			user = context.getAuthor();
		}

		EmbedBuilder embed = new EmbedBuilder().withTimestamp( CLOCK.instant() )
				.withColor( UserModule.EMBED_COLOR );
		
		// Basic info.
		embed.withThumbnail( user.getAvatarURL() );
		embed.appendField( "Name", user.getName(), true );
		String nickname = user.getNicknameForGuild( context.getGuild() );
		if ( nickname != null ) {
			embed.appendField( "Nickname", nickname, true );
		}
		String customInfo = infoData.get( user.getStringID() );
		if ( customInfo == null ) {
			if ( user.isBot() ) {
				customInfo = "Hi, I am a bot!";
			} else {
				customInfo = "Wow such empty";
			}
		}
		embed.appendField( "Custom Info", customInfo, false );
		
		// Level info.
		LevelState state = LevelingManager.getInstance().getLevelState( user );
		embed.appendField( "Level", "Lvl. " + state.getLevel(), true );
		long exp = state.getExp();
		long maxExp = state.getExpToNextLevel();
		float percent = (float) exp / maxExp;
		int progress = Math.round( percent * 100 );
		int bars = Math.round( percent * EXP_BAR_SIZE );
		StringBuilder barBuilder = new StringBuilder();
		int i = 0;
		for ( ; i < bars; i++ ) {
			
			barBuilder.append( '*' );
			
		}
		for ( ; i < EXP_BAR_SIZE; i++ ) {
			
			barBuilder.append( '.' );
			
		}
		embed.appendField( "EXP", String.format( "%d/%d `[%s]` %d%%", exp, maxExp,
				barBuilder.toString(), progress ), true );
		
		// Currency info
		embed.appendField( "Money",
				CurrencyManager.format( CurrencyManager.getInstance().getCurrency( user ) ), true );
		
		context.getReplyBuilder().withEmbed( embed.build() ).build();
		
	}
	
	@MainCommand(
			name = "Custom info set",
			aliases = { "setinfo" },
			description = "Sets the message for the Custom Info field of the calling user's profile. "
					+ "Remember to put the argument between quotes (\") if it has any blank space "
					+ "(spaces or line breaks).",
			usage = "{}setinfo <custom info>",
			replyPrivately = true
			)
	public void setInfoCommand( CommandContext context ) {
		
		if ( context.getArgs().isEmpty() ) {
			context.getReplyBuilder().withContent( "Missing argument." ).build();
			return; // Abort.
		}
		
		String info = context.getArgs().get( 0 );
		infoData.put( context.getAuthor().getStringID(), info );
		context.getReplyBuilder().withContent( String.format( "Set custom info to \"%s\"!", info ) ).build();
		
	}

}