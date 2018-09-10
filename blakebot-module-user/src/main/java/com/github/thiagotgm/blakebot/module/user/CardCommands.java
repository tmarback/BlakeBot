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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.thiagotgm.blakebot.module.user.CardManager.Card;
import com.github.thiagotgm.blakebot.module.user.CardManager.UserCards;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.api.Argument.Type;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SuccessHandler;

import sx.blah.discord.handle.obj.IUser;

/**
 * Commands to interact with the card system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-08
 */
public class CardCommands {
	
	private static final Pattern USER_PATTERN = Pattern.compile( "(.+)#(\\d{4})" );
	
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
			Matcher match = USER_PATTERN.matcher( arg.getText() );
			if ( !match.matches() ) {
				return null; // Did not match format.
			}
			
			String name = match.group( 1 );
			String discriminator = match.group( 2 );
			for ( IUser option : context.getEvent().getClient().getUsersByName( name ) ) {
				// Look for user with the right name and discriminator.
				if ( option.getDiscriminator().matches( discriminator ) ) {
					target = option; // Found user.
					break;
				}
				
			}
			
			if ( target == null ) {
				return null; // Did not find user.
			}
		}
		context.setHelper( target ); // Store target.
		return target;
		
	}
	
	private static final String SUCCESS_HANDLER = "success";
	private static final String FAILURE_HANDLER = "failure";
	
	private static final String GET_SUBCOMMAND = "Get custom card";
	private static final String ADD_CARD_SUBCOMMAND = "Add custom card";
	private static final String REMOVE_CARD_SUBCOMMAND = "Remove custom card";
	
	private final CardManager manager = CardManager.getInstance();
	
	@MainCommand(
			name = "Card command",
			aliases = "card",
			description = "Cards that can be customized by the user to be shown in chat. "
					+ "Each user starts off being able to have " + UserCards.STARTING_CARDS
					+ " cards at the same time, and may purchase more card slots using the "
					+ "bot currency (up to " + UserCards.MAX_CARDS + " cards)!",
			usage = "{}card <subcommand>",
			subCommands = { GET_SUBCOMMAND, ADD_CARD_SUBCOMMAND, REMOVE_CARD_SUBCOMMAND },
			ignorePublic = true,
			ignorePrivate = true
			)
	public void cardCommand( CommandContext context ) {
		
		context.setHelper( getTarget( context ) ); // Parse target.
		
	}
	
	@SubCommand(
			name = GET_SUBCOMMAND,
			aliases = "get",
			description = "Retrieves a custom card owned by the specified user (if no user is "
					+ "specified, the calling user) with the given name. The user may be "
					+ "specified through either a mention or through the format "
					+ "[name]#[discriminator].",
			usage = "{}card get [user] <card name>",
			executeParent = true,
			failureHandler = FAILURE_HANDLER
			)
	public boolean getCardCommand( CommandContext context ) {
		
		IUser target = context.getHelper().isPresent() ? (IUser) context.getHelper().get() : null;
		List<String> args = context.getArgs();
		if ( target != null ) {
			args.remove( 0 ); // Remove target arg.
		} else {
			target = context.getAuthor(); // Target is caller.
		}
		
		if ( args.isEmpty() ) {
			context.setHelper( "Must specify a card name!" );
			return false;
		}
		
		String name = args.get( 0 );
		Card card = manager.getCard( target, name );
		if ( card == null ) {
			context.setHelper( "**" + target.getName() + "** has no custom card named '"
					+ name + "'!" );
			return false;
		}
		
		context.getReplyBuilder().withEmbed( card.getEmbed() ).build(); // Send card.
		
		return true;
		
	}
	
	@SubCommand(
			name = ADD_CARD_SUBCOMMAND,
			aliases = { "add" },
			description = "Adds a custom card with the given name (title) to yourself. "
					+ "The card will only be added only if you aren't currently using all "
					+ "your card slots. Also, the title must be limited to " + 
					Card.MAX_TITLE_LENGTH + " characters.",
			usage = "{}card add <card name>",
			executeParent = false,
			successHandler = SUCCESS_HANDLER,
			failureHandler = FAILURE_HANDLER
			)
	public boolean addCardCommand( CommandContext context ) {
		
		if ( context.getArgs().isEmpty() ) {
			context.setHelper( "Must specify a card name!" );
			return false;
		}
		String cardTitle = context.getArgs().get( 0 );
		
		if ( manager.addCard( context.getAuthor(), cardTitle ) ) {
			context.setHelper( "Added card '" + cardTitle + "'!" );
			return true;
		} else {
			context.setHelper( "You are already using all of your card slots!" );
			return false;
		}
		
	}
	
	@SubCommand(
			name = REMOVE_CARD_SUBCOMMAND,
			aliases = { "remove", "rm" },
			description = "Removes a custom card with the given name (title) from yourself.",
			usage = "{}card remove|rm <card name>",
			executeParent = false,
			successHandler = SUCCESS_HANDLER,
			failureHandler = FAILURE_HANDLER
			)
	public boolean removeCardCommand( CommandContext context ) {
		
		if ( context.getArgs().isEmpty() ) {
			context.setHelper( "Must specify a card name!" );
			return false;
		}
		String cardTitle = context.getArgs().get( 0 );
		
		if ( manager.removeCard( context.getAuthor(), cardTitle ) ) {
			context.setHelper( "Removed card '" + cardTitle + "'!" );
			return true;
		} else {
			context.setHelper( "You don't have a card titled '" + cardTitle + "'!" );
			return false;
		}
		
	}
	
	@SuccessHandler( SUCCESS_HANDLER )
	public void successHandler( CommandContext context ) {
		
		context.getReplyBuilder().withContent( (String) context.getHelper().get() ).build();
		
	}
	
	@FailureHandler( FAILURE_HANDLER )
	public void failureHandler( CommandContext context, FailureReason reason ) {
		
		String message;
		switch ( reason ) {
		
			case COMMAND_OPERATION_FAILED:
				message = (String) context.getHelper().get();
				break;
				
			case COMMAND_OPERATION_EXCEPTION:
				message = ( (Exception) context.getHelper().get() ).getMessage();
				break;
				
			default:
				message = "Sorry, I couldn't do that.";
		
		}
		context.getReplyBuilder().withContent( message ).build();
		
	}

}
