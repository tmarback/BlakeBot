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

import com.github.thiagotgm.bot_utils.utils.Utils;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.api.Argument.Type;

import sx.blah.discord.handle.obj.IUser;

/**
 * Command to gift currency to other users.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-10
 */
public class GiftCommand {
	
	private static final String FAILURE_HANDLER = "failure";
	
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
			name = "Currency gift",
			aliases = { "gift", "giveto" },
			description = "Gifts the specified amount of money to the specified user.",
			usage = "{}gift|giveto <user> <amount>",
			failureHandler = FAILURE_HANDLER
			)
	public boolean giftCommand( CommandContext context ) {
		
		IUser caller = context.getAuthor();
		IUser target = getTarget( context );
		if ( target == null ) {
			context.setHelper( "You must specify a valid user to gift to!" );
			return false;
		}
		if ( target.getLongID() == caller.getLongID() ) {
			context.setHelper( "You cannot send a gift to yourself!" );
			return false;
		}
		
		if ( context.getArgs().size() < 2 ) {
			context.setHelper( "You must specify an amount to gift!" );
			return false;
		}
		long amount;
		try {
			amount = Long.parseLong( context.getArgs().get( 1 ) );
		} catch ( NumberFormatException e ) {
			context.setHelper( "Invalid amount!" );
			return false;
		}
		if ( amount < 0 ) {
			context.setHelper( "The gift amount cannot be negative!" );
			return false;
		}
		
		CurrencyManager manager = CurrencyManager.getInstance();
		if ( manager.withdraw( caller, amount ) < 0 ) {
			context.setHelper( "You do not have enough money to send this gift!" );
			return false;
		}
		manager.deposit( target, amount );
		
		context.getReplyBuilder().withContent( String.format( "**%s** just gifted "
				+ "**%s** to **%s**! :tada::moneybag:", caller.getName(), 
				CurrencyManager.format( amount ), target.getName() ) ).build();
		
		return true;
		
	}
	
	@FailureHandler( FAILURE_HANDLER )
	public void failureHandler( CommandContext context, FailureReason reason ) {
		
		String message;
		switch ( reason ) {
		
			case COMMAND_OPERATION_FAILED:
				message = (String) context.getHelper().get();
				break;
				
			default:
				message = "Sorry, I couldn't do that.";
		
		}
		context.getReplyBuilder().withContent( message ).build();
		
	}

}
