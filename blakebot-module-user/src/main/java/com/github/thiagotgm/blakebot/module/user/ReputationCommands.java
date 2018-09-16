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

import com.github.thiagotgm.blakebot.module.user.ReputationManager.Vote;
import com.github.thiagotgm.bot_utils.utils.Utils;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.Argument.Type;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SuccessHandler;

import sx.blah.discord.handle.obj.IUser;

/**
 * Commands that give users a way to vote on other user's reputations.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-07
 */
public class ReputationCommands {
	
	private static final String SUCCESS_HANDLER = "voteSuccess";
	private static final String FAILURE_HANDLER = "voteFail";
	
	/**
	 * Gets the target specified in the first arg of the given context.
	 * 
	 * @param context The context of the command.
	 * @return The target, or <tt>null</tt> if the first argument did
	 *         not specify a valid user.
	 */
	private IUser getTarget( CommandContext context ) {
		
		if ( context.getArguments().isEmpty() ) {
			return null;
		}
		
		Argument arg = context.getArguments().get( 0 );
		IUser target = null;
		if ( arg.getType() == Type.USER_MENTION ) { // A mention.
			target = (IUser) arg.getArgument();
		} else { // Try to parse as name#discriminator.
			target = Utils.getUser( arg.getText(), context.getEvent().getClient() );
			if ( target == null ) {
				return null; // Did not find user.
			}
		}
		context.setHelper( target ); // Store target.
		return target;
		
	}
	
	@MainCommand(
			name = "Upvote command",
			aliases = { "upvote", "upv" },
			description = "Places a positive vote towards a user's reputation. "
					+ "If the caller has already voted on the target user, the "
					+ "previous vote is erased.\nThe target user may be specified "
					+ "either by mentioning him/her, or by giving their name (not "
					+ "nickname) and discriminator in the form [name]#[discriminator] "
					+ "(the same way that appears on their Discord profile).",
			usage = "{}upvote|upv <target>",
			successHandler = SUCCESS_HANDLER,
			failureHandler = FAILURE_HANDLER
			)
	public boolean upvoteCommand( CommandContext context ) {
		
		IUser target = getTarget( context );
		if ( target == null ) {
			return false; // Target not found.
		}
		
		return ReputationManager.getInstance().vote( context.getAuthor(), target, Vote.UPVOTE );
		
	}
	
	@MainCommand(
			name = "Downvote command",
			aliases = { "downvote", "downv" },
			description = "Places a negative vote towards a user's reputation. "
					+ "If the caller has already voted on the target user, the "
					+ "previous vote is erased.\nThe target user may be specified "
					+ "either by mentioning him/her, or by giving their name (not "
					+ "nickname) and discriminator in the form [name]#[discriminator] "
					+ "(the same way that appears on their Discord profile).",
			usage = "{}downvote|downv <target>",
			successHandler = SUCCESS_HANDLER,
			failureHandler = FAILURE_HANDLER
			)
	public boolean downvoteCommand( CommandContext context ) {
		
		IUser target = getTarget( context );
		if ( target == null ) {
			return false; // Target not found.
		}
		
		return ReputationManager.getInstance().vote( context.getAuthor(), target, Vote.DOWNVOTE );
		
	}
	
	@MainCommand(
			name = "Clear vote command",
			aliases = { "clearvote", "clrv" },
			description = "Clears the vote towards a user's reputation. E.g., "
					+ "if the caller has already voted on the target user, the "
					+ "previous vote is erased.\nThe target user may be specified "
					+ "either by mentioning him/her, or by giving their name (not "
					+ "nickname) and discriminator in the form [name]#[discriminator] "
					+ "(the same way that appears on their Discord profile).",
			usage = "{}clearvote|clrv <target>",
			successHandler = SUCCESS_HANDLER,
			failureHandler = FAILURE_HANDLER
			)
	public boolean clearVoteCommand( CommandContext context ) {
		
		IUser target = getTarget( context );
		if ( target == null ) {
			return false; // Target not found.
		}
		
		return ReputationManager.getInstance().vote( context.getAuthor(), target, Vote.NO_VOTE );
		
	}
	
	@SuccessHandler( SUCCESS_HANDLER )
	public void success( CommandContext context ) {
		
		IUser target = (IUser) context.getHelper().get();
		context.getReplyBuilder().withContent( "Successfully registered your vote towards **"
				+ target.getName() + "**!"  ).build();
		
	}
	
	@FailureHandler( FAILURE_HANDLER )
	public void failure( CommandContext context, FailureReason reason ) {
		
		String message;
		switch ( reason ) {
		
			case COMMAND_OPERATION_FAILED:
				if ( context.getHelper().isPresent() ) {
					message = "You already had this vote for **" +
							context.getHelper().get() + "**!";
				} else {
					message = "Must give a valid user (mention or [Name]#[Discriminator]) "
							+ "as an argument!";
				}
				break;
				
			case COMMAND_OPERATION_EXCEPTION:
				message = "You can't vote on yourself!";
				break;
				
			default:
				return;
		
		}
		context.getReplyBuilder().withContent( message ).build();
		
	}

}
