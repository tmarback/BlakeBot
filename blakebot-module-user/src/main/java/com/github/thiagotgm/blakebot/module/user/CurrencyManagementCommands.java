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

import com.github.thiagotgm.modular_commands.api.Argument.Type;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

/**
 * Commands for managing the currency system as the bot owner.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-06
 */
public class CurrencyManagementCommands {
	
	private static final String FAILURE_HANDLER = "Failure";
	
	@MainCommand(
			name = "Give currency",
			aliases = "give",
			description = "Gives the specified amount of currency to the specified user.",
			usage = "{}give [user] [amount]",
			requiresOwner = true,
			failureHandler = FAILURE_HANDLER
			)
	public boolean giveCommand( CommandContext context ) {
		
		if ( context.getArguments().get( 0 ).getType() != Type.USER_MENTION ) {
			context.getReplyBuilder().withContent( "First argument must be a user." );
			return false;
		}
		IUser user = (IUser) context.getArguments().get( 0 ).getArgument();
		
		long amount;
		try {
			amount = Long.parseLong( context.getArguments().get( 1 ).getText() );
		} catch ( NumberFormatException e ) {
			context.getReplyBuilder().withContent( "Second argument must be a number." );
			return false;
		}
		
		if ( CurrencyManager.getInstance().deposit( user, amount ) == CurrencyManager.ERROR ) {
			context.getReplyBuilder().withContent( "There was an error while performing the operation." );
			return false;
		}
		
		context.getReplyBuilder().withContent( "Gave " + CurrencyManager.format( amount ) +
				" to " + user.mention() + "!" ).build();
		
		return true;
		
	}
	
	@MainCommand(
			name = "Take currency",
			aliases = "take",
			description = "Takes the specified amount of currency from the specified user.",
			usage = "{}take [user] [amount]",
			requiresOwner = true,
			failureHandler = FAILURE_HANDLER
			)
	public boolean takeCommand( CommandContext context ) {
		
		if ( context.getArguments().get( 0 ).getType() != Type.USER_MENTION ) {
			context.getReplyBuilder().withContent( "First argument must be a user." );
			return false;
		}
		IUser user = (IUser) context.getArguments().get( 0 ).getArgument();
		
		long amount;
		try {
			amount = Long.parseLong( context.getArguments().get( 1 ).getText() );
		} catch ( NumberFormatException e ) {
			context.getReplyBuilder().withContent( "Second argument must be a number." );
			return false;
		}
		
		long result = CurrencyManager.getInstance().withdraw( user, amount );
		if ( result == CurrencyManager.NOT_ENOUGH_FUNDS ) {
			context.getReplyBuilder().withContent( "The given user does not have enough funds to "
					+ "take the given amount." );
			return false;
		} else if ( result == CurrencyManager.ERROR ) {
			context.getReplyBuilder().withContent( "There was an error while performing the operation." );
			return false;
		}
		
		context.getReplyBuilder().withContent( "Took " + CurrencyManager.format( amount ) +
				" from " + user.mention() + "!" ).build();
		
		return true;
		
	}
	
	@FailureHandler( value = FAILURE_HANDLER )
	public void failureHandler( CommandContext context, FailureReason reason ) {
		
		MessageBuilder reply = context.getReplyBuilder();
		switch ( reason ) {
		
			case USER_NOT_OWNER:
				reply.withContent( "You must be the bot owner to use that command." );
				break;
				
			case COMMAND_OPERATION_FAILED:
				break; // Message already in builder.
				
			default:
				reply.withContent( "Sorry, I couldn't do that." );
		
		}
		reply.build();
		
	}

}
