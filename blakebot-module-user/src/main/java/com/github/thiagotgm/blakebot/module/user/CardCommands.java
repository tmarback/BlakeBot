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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import com.github.thiagotgm.blakebot.module.user.CardManager.Card;
import com.github.thiagotgm.blakebot.module.user.CardManager.UserCards;
import com.github.thiagotgm.bot_utils.utils.Utils;
import com.github.thiagotgm.modular_commands.api.Argument;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.api.FailureReason;
import com.github.thiagotgm.modular_commands.api.ICommand;
import com.github.thiagotgm.modular_commands.api.Argument.Type;
import com.github.thiagotgm.modular_commands.command.annotation.FailureHandler;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;
import com.github.thiagotgm.modular_commands.command.annotation.SubCommand;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

/**
 * Commands to interact with the card system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-08
 */
@SuppressWarnings( "javadoc" )
public class CardCommands {

    /**
     * Gets the target specified in the first arg of the given context.
     * 
     * @param context
     *            The context of the command.
     * @return The target, or <tt>null</tt> if the first argument did not specify a
     *         valid user.
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

    private static final String GET_FAILURE_HANDLER = "failure";

    private static final String GET_SUBCOMMAND = "Get custom card";
    private static final String ADD_CARD_SUBCOMMAND = "Add custom card";
    private static final String REMOVE_CARD_SUBCOMMAND = "Remove custom card";
    private static final String CHANGE_TITLE_SUBCOMMAND = "Change custom card title";

    private static final String BUY_SLOT_SUBCOMMAND = "Buy custom card slot";

    private static final String SET_FIELD_SUBCOMMAND = "Set custom card field";
    private static final String REMOVE_FIELD_SUBCOMMAND = "Remove custom card field";

    private static final String SET_DESCRIPTION_SUBCOMMAND = "Set custom card description";
    private static final String SET_URL_SUBCOMMAND = "Set custom card URL";
    private static final String SET_FOOTER_SUBCOMMAND = "Set custom card footer";
    private static final String SET_FOOTER_ICON_SUBCOMMAND = "Set custom card footer icon";
    private static final String SET_IMAGE_SUBCOMMAND = "Set custom card image";
    private static final String SET_THUMBNAIL_SUBCOMMAND = "Set custom card thumbnail";
    private static final String SET_AUTHOR_SUBCOMMAND = "Set custom card author";
    private static final String SET_AUTHOR_URL_SUBCOMMAND = "Set custom card author URL";
    private static final String SET_AUTHOR_ICON_SUBCOMMAND = "Set custom card author icon";

    private final CardManager manager = CardManager.getInstance();

    @MainCommand(
            name = "Card command",
            aliases = "card",
            description = "Cards that can be customized by the user to be shown in chat.\n"
                    + "Each user starts off being able to have " + UserCards.STARTING_CARDS
                    + " cards at the same time, and may purchase more card slots using the bot currency (up to "
                    + UserCards.MAX_CARDS + " cards)!",
            usage = "{signature} <subcommand>",
            subCommands = { GET_SUBCOMMAND, ADD_CARD_SUBCOMMAND, REMOVE_CARD_SUBCOMMAND, CHANGE_TITLE_SUBCOMMAND,
                    BUY_SLOT_SUBCOMMAND, SET_FIELD_SUBCOMMAND, REMOVE_FIELD_SUBCOMMAND, SET_DESCRIPTION_SUBCOMMAND,
                    SET_URL_SUBCOMMAND, SET_FOOTER_SUBCOMMAND, SET_FOOTER_ICON_SUBCOMMAND, SET_IMAGE_SUBCOMMAND,
                    SET_THUMBNAIL_SUBCOMMAND, SET_AUTHOR_SUBCOMMAND, SET_AUTHOR_URL_SUBCOMMAND,
                    SET_AUTHOR_ICON_SUBCOMMAND },
            ignorePublic = true,
            ignorePrivate = true )
    public void cardCommand( CommandContext context ) {

        // Do nothing.

    }

    @SubCommand(
            name = GET_SUBCOMMAND,
            aliases = "get",
            description = "Retrieves a custom card owned by the specified user (if no user is "
                    + "specified, the calling user) with the given name.\n"
                    + "The user may be specified through either a mention or through the format "
                    + "[name]#[discriminator].",
            usage = "{signature} [user] <card name>",
            failureHandler = GET_FAILURE_HANDLER )
    public boolean getCardCommand( CommandContext context ) {

        IUser target = getTarget( context ); // Parse target.
        List<String> args = new ArrayList<>( context.getArgs() );
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
            context.setHelper( "**" + target.getName() + "** has no custom card named '" + name + "'!" );
            return false;
        }
        try {
            context.getReplyBuilder().withEmbed( card.getEmbed() ).build(); // Send card.
        } catch ( DiscordException e ) {
            context.setHelper( e ); // Store exception.
            throw e;
        }

        return true;

    }

    /**
     * Runs the given operation. If the operation throws an
     * IllegalArgumentException, places the error message as the helper of the
     * context and returns <tt>false</tt>.
     *
     * @param context
     *            The context.
     * @param op
     *            The operation to run. It should return <tt>true</tt> if executed
     *            successfully.
     * @return <tt>true</tt> if executed successfully.
     */
    private boolean runOp( CommandContext context, BooleanSupplier op ) {

        try {
            return op.getAsBoolean();
        } catch ( IllegalArgumentException e ) {
            context.setHelper( e.getMessage() );
            return false;
        }

    }

    @SubCommand(
            name = ADD_CARD_SUBCOMMAND,
            aliases = { "add" },
            description = "Adds a custom card with the given name (title) to yourself.\n"
                    + "The card will only be added only if you aren't currently using all "
                    + "your card slots. Also, the name must be limited to " + Card.MAX_TITLE_LENGTH + " characters.",
            usage = "{signature} <card name>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean addCardCommand( CommandContext context ) {

        if ( context.getArgs().isEmpty() ) {
            context.setHelper( "Must specify a card name!" );
            return false;
        }
        String cardTitle = context.getArgs().get( 0 );

        return runOp( context, () -> {

            if ( manager.addCard( context.getAuthor(), cardTitle ) ) {
                context.setHelper( "Added card '" + cardTitle + "'!" );
                return true;
            } else {
                context.setHelper( "You are already using all of your card slots!" );
                return false;
            }

        } );

    }

    @SubCommand(
            name = REMOVE_CARD_SUBCOMMAND,
            aliases = { "remove", "rm" },
            description = "Removes a custom card with the given name (title) from yourself.",
            usage = "{signature} <card name>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean removeCardCommand( CommandContext context ) {

        if ( context.getArgs().isEmpty() ) {
            context.setHelper( "Must specify a card name!" );
            return false;
        }
        String cardTitle = context.getArgs().get( 0 );

        return runOp( context, () -> {

            if ( manager.removeCard( context.getAuthor(), cardTitle ) ) {
                context.setHelper( "Removed card '" + cardTitle + "'!" );
                return true;
            } else {
                context.setHelper( "You don't have a card titled '" + cardTitle + "'!" );
                return false;
            }

        } );

    }

    @SubCommand(
            name = CHANGE_TITLE_SUBCOMMAND,
            aliases = { "changetitle", "ct", "settitle", "sett", "edittitle", "edit" },
            description = "Changes the name (title) of the custom card with the given name to the "
                    + "given new name.\n" + "The new name must be limited to " + Card.MAX_TITLE_LENGTH + " characters.",
            usage = "{signature} <current card name> <new card name>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean changeTitleCommand( CommandContext context ) {

        if ( context.getArgs().size() < 2 ) {
            context.setHelper( "Must specify the current card name and the new card name!" );
            return false;
        }
        String curTitle = context.getArgs().get( 0 );
        String newTitle = context.getArgs().get( 1 );

        return runOp( context, () -> {

            if ( manager.setCardTitle( context.getAuthor(), curTitle, newTitle ) ) {
                context.setHelper( "Changed card name from '" + curTitle + "' to '" + newTitle + "'!" );
                return true;
            } else {
                context.setHelper( "You don't have a card titled '" + curTitle + "'!" );
                return false;
            }

        } );

    }

    @SubCommand(
            name = BUY_SLOT_SUBCOMMAND,
            aliases = { "buyslot", "buy" },
            description = "Buys an extra custom card slot for $" + UserCards.EXTRA_CARD_COST + ", with a maximum of "
                    + UserCards.MAX_CARDS + " total slots.",
            usage = "{signature}",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean buySlotCommand( CommandContext context ) {

        return runOp( context, () -> {

            if ( manager.buySlot( context.getAuthor() ) ) {
                context.setHelper( "You now have another card slot! :money_with_wings:" );
                return true;
            } else {
                context.setHelper( "You do not have enough funds to buy another slot!" );
                return false;
            }

        } );

    }

    @SubCommand(
            name = SET_FIELD_SUBCOMMAND,
            aliases = { "setfield", "setf" },
            description = "Sets the text of the field with the given name in the given card.\n"
                    + "If the card does not have a field with the given name, and does not yet "
                    + "have the maximum amount of fields (" + Card.MAX_FIELDS + "), the field is created.\n"
                    + "There is also a limitation that the field name can only have up to " + Card.MAX_FIELD_NAME_LENGTH
                    + " characters, and the field text can only have up to " + Card.MAX_FIELD_TEXT_LENGTH
                    + " characters.\n" + "NOTE: When displaying the card, the fields are ordered by the title.",
            usage = "{signature} <card name> <field name> <field text>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setFieldCommand( CommandContext context ) {

        if ( context.getArgs().size() < 3 ) {
            context.setHelper( "Must specify the card name, the field name, and the field text!" );
            return false;
        }
        String cardTitle = context.getArgs().get( 0 );
        String fieldName = context.getArgs().get( 1 );
        String fieldText = context.getArgs().get( 2 );

        return runOp( context, () -> {

            if ( manager.setField( context.getAuthor(), cardTitle, fieldName, fieldText ) ) {
                context.setHelper( "Set text of field '" + fieldName + "' to '" + fieldText + "'!" );
                return true;
            } else {
                context.setHelper( "The card '" + cardTitle + "' already has the maximum amount of fields!" );
                return false;
            }

        } );

    }

    @SubCommand(
            name = REMOVE_FIELD_SUBCOMMAND,
            aliases = { "removefield", "rmf" },
            description = "Removes the field with the given name from the given card.",
            usage = "{signature} <card name> <field name>",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean removeFieldCommand( CommandContext context ) {

        if ( context.getArgs().size() < 2 ) {
            context.setHelper( "Must specify the card name and the field name!" );
            return false;
        }
        String cardTitle = context.getArgs().get( 0 );
        String fieldName = context.getArgs().get( 1 );

        return runOp( context, () -> {
            
            if ( manager.setField( context.getAuthor(), cardTitle, fieldName, null ) ) {
                context.setHelper( "Removed field '" + fieldName + "'!" );
                return true;
            } else {
                context.setHelper( "The card '" + cardTitle + "' does not have a field named '" + fieldName + "'!" );
                return false;
            }
            
        } );

    }

    /**
     * Sets the value of a card attribute.
     * 
     * @param context
     *            The context of the command.
     * @param attributeName
     *            The name of the attribute (such as "description" or "URL"). Only
     *            used to make the error message if necessary.
     * @param setter
     *            The operation that takes the card title and the attribute value
     *            (value may be <tt>null</tt>), in this order, and returns whether
     *            it was successful.
     * @return <tt>true</tt> if set successfully. <tt>false</tt> if there was an
     *         error (the helper object of the context is set to the appropriate
     *         error message).
     * @throws IllegalArgumentException
     *             if the setter threw such exception.
     */
    private boolean setAttribute( CommandContext context, String attributeName,
            BiFunction<String, String, Boolean> setter ) throws IllegalArgumentException {

        List<String> args = context.getArgs();
        if ( args.isEmpty() ) {
            context.setHelper( "Must specify the card name!" );
            return false;
        }
        String cardTitle = args.get( 0 );
        String value = args.size() >= 2 ? args.get( 1 ) : null;

        return runOp( context, () -> {
            
            if ( setter.apply( cardTitle, value ) ) {
                context.setHelper(
                        ( value == null ? "Removed " : "Set " ) + attributeName + " of card '" + cardTitle + "'!" );
                return true;
            } else {
                context.setHelper( "You don't have a card titled '" + cardTitle + "'!" );
                return false;
            }
            
        } );

    }

    @SubCommand(
            name = SET_DESCRIPTION_SUBCOMMAND,
            aliases = { "setdescription", "setd" },
            description = "Sets the description of the given card.\n"
                    + "If no description is given, the current description is deleted.\n"
                    + "The description is limited to " + Card.MAX_DESCRIPTION_LENGTH + " characters.",
            usage = "{signature} <card name> [description]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setDescriptionCommand( CommandContext context ) {

        return setAttribute( context, "description",
                ( title, description ) -> manager.setDescription( context.getAuthor(), title, description ) );

    }

    @SubCommand(
            name = SET_URL_SUBCOMMAND,
            aliases = { "seturl" },
            description = "Sets the URL of the given card (the link in the card title).\n"
                    + "If no URL is given, the current URL is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will fail to "
                    + "be shown until the URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setUrlCommand( CommandContext context ) {

        return setAttribute( context, "URL", ( title, url ) -> manager.setUrl( context.getAuthor(), title, url ) );

    }

    @SubCommand(
            name = SET_FOOTER_SUBCOMMAND,
            aliases = { "setfooter", "setft" },
            description = "Sets the footer of the given card.\n"
                    + "If no footer is given, the current footer is deleted.\n" + "The footer is limited to "
                    + Card.MAX_FOOTER_LENGTH + " characters.",
            usage = "{signature} <card name> [footer]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setFooterCommand( CommandContext context ) {

        return setAttribute( context, "footer",
                ( title, footer ) -> manager.setFooter( context.getAuthor(), title, footer ) );

    }

    @SubCommand(
            name = SET_FOOTER_ICON_SUBCOMMAND,
            aliases = { "setfootericon", "setfti" },
            description = "Sets the icon of the footer of the given card (the image next to the footer).\n"
                    + "If no icon is given, the current icon is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will "
                    + "fail to be shown until the icon URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [icon URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setFooterIconCommand( CommandContext context ) {

        return setAttribute( context, "footer icon",
                ( title, iconUrl ) -> manager.setFooterIcon( context.getAuthor(), title, iconUrl ) );

    }

    @SubCommand(
            name = SET_IMAGE_SUBCOMMAND,
            aliases = { "setimage", "seti" },
            description = "Sets the image of the given card (the big image in the bottom).\n"
                    + "If no image is given, the current image is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will fail "
                    + "to be shown until the image URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [image URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setImageCommand( CommandContext context ) {

        return setAttribute( context, "image",
                ( title, imageUrl ) -> manager.setImage( context.getAuthor(), title, imageUrl ) );

    }

    @SubCommand(
            name = SET_THUMBNAIL_SUBCOMMAND,
            aliases = { "setthumbnail", "setth" },
            description = "Sets the thumbnail of the given card (the small image in the top right).\n"
                    + "If no image is given, the current thumbnail is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will fail "
                    + "to be shown until the thumbnail URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [image URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setThumbnailCommand( CommandContext context ) {

        return setAttribute( context, "thumbnail",
                ( title, imageUrl ) -> manager.setThumbnail( context.getAuthor(), title, imageUrl ) );

    }

    @SubCommand(
            name = SET_AUTHOR_SUBCOMMAND,
            aliases = { "setauthor", "seta" },
            description = "Sets the author of the given card (name shown above the title of the card).\n"
                    + "If no name is given, the current author is deleted.\n" + "The author name is limited to "
                    + Card.MAX_AUTHOR_LENGTH + " characters.",
            usage = "{signature} <card name> [author name]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setAuthorCommand( CommandContext context ) {

        return setAttribute( context, "author",
                ( title, footer ) -> manager.setAuthor( context.getAuthor(), title, footer ) );

    }

    @SubCommand(
            name = SET_AUTHOR_URL_SUBCOMMAND,
            aliases = { "setauthorurl", "setaurl" },
            description = "Sets the URL of the author of the given card (the link in the author name).\n"
                    + "If no URL is given, the current URL is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will "
                    + "fail to be shown until the URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setAuthorUrlCommand( CommandContext context ) {

        return setAttribute( context, "author URL",
                ( title, url ) -> manager.setAuthorUrl( context.getAuthor(), title, url ) );

    }

    @SubCommand(
            name = SET_AUTHOR_ICON_SUBCOMMAND,
            aliases = { "setauthoricon", "setai" },
            description = "Sets the icon of the author of the given card (the image next to the author name).\n"
                    + "If no icon is given, the current icon is deleted.\n"
                    + "The URL is not checked for validity, but if it is invalid, the card will "
                    + "fail to be shown until the icon URL is replaced with a valid URL (or removed).",
            usage = "{signature} <card name> [icon URL]",
            successHandler = ICommand.STANDARD_SUCCESS_HANDLER,
            failureHandler = ICommand.STANDARD_FAILURE_HANDLER )
    public boolean setAuthorIconCommand( CommandContext context ) {

        return setAttribute( context, "author icon",
                ( title, iconUrl ) -> manager.setAuthorIcon( context.getAuthor(), title, iconUrl ) );

    }

    @FailureHandler( GET_FAILURE_HANDLER )
    public void failureHandler( CommandContext context, FailureReason reason ) {

        switch ( reason ) {

            case DISCORD_ERROR: // Error with card embed, probably.
                if ( context.getCommand().getName().equals( GET_SUBCOMMAND ) && context.getHelper().isPresent() ) {
                    DiscordException e = (DiscordException) context.getHelper().get();
                    context.getReplyBuilder().withEmbed( null ); // Remove problematic embed.
                    context.getReplyBuilder().withContent( "Could not send card.\nError was `" + e.getErrorMessage()
                            + "`.\nOne of the URLs on the card is invalid, maybe?" ).build();
                    break;
                }

            default: // Delegate to standard implementation.
                ICommand.standardOnFailure( context, reason );

        }

    }

}
