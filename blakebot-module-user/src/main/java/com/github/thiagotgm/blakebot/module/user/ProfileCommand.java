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

import java.util.List;

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
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.withThumbnail( user.getAvatarURL() );
		embed.appendField( "Name", user.getName(), true );
		String nickname = user.getNicknameForGuild( context.getGuild() );
		if ( nickname != null ) {
			embed.appendField( "Nickname", nickname, true );
		}
		embed.appendField( "Message", "TODO", false );
		
		context.getReplyBuilder().withEmbed( embed.build() ).build();
		
	}

}
