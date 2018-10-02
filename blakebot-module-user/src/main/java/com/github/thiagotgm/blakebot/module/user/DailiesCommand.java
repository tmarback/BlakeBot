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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.bot_utils.Settings;
import com.github.thiagotgm.bot_utils.storage.Data;
import com.github.thiagotgm.bot_utils.storage.DatabaseManager;
import com.github.thiagotgm.bot_utils.storage.TranslationException;
import com.github.thiagotgm.bot_utils.storage.Translator;
import com.github.thiagotgm.bot_utils.storage.translate.StringTranslator;
import com.github.thiagotgm.bot_utils.utils.AsyncTools;
import com.github.thiagotgm.bot_utils.utils.KeyedExecutorService;
import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Command that gives currency to the calling user, but only once per day.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-06
 */
@SuppressWarnings( "javadoc" )
public class DailiesCommand {

    private static final Logger LOG = LoggerFactory.getLogger( DailiesCommand.class );
    private static final ThreadGroup THREADS = new ThreadGroup( "Daily Currency" );

    /**
     * Executor used to perform currency-changing operations. Tasks are keyed by the
     * string ID of the user to avoid race conditions.
     */
    protected static final KeyedExecutorService EXECUTOR = AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {

        LOG.error( "Error while giving daily currency.", e );

    } );

    /**
     * Setting that defines {@link #DAILY_AMOUNT}.
     */
    public static final String DAILY_AMOUNT_SETTING = "Daily currency amount";
    /**
     * How much currency a user gets in a daily.
     */
    public static final long DAILY_AMOUNT = Settings.getLongSetting( DAILY_AMOUNT_SETTING );

    private final Map<String, ZonedDateTime> cooldownMap;

    /**
     * Creates a new instance.
     */
    public DailiesCommand() {

        cooldownMap = Collections.synchronizedMap( DatabaseManager.getDatabase().getDataMap( "DailyCurrency",
                new StringTranslator(), new ZonedDateTimeTranslator() ) );

    }

    /**
     * Determines whether the given user is ready to obtain his daily money.
     * 
     * @param user
     *            The user.
     * @return <tt>true</tt> if the user has not yet obtained his daily currency
     *         today. <tt>false</tt> if already obtained.
     * @throws NullPointerException
     *             if the given user is <tt>null</tt>.
     */
    protected boolean isAvailable( IUser user ) throws NullPointerException {

        if ( user == null ) {
            throw new NullPointerException( "User cannot be null." );
        }

        ZonedDateTime time = cooldownMap.get( user.getStringID() );

        return time == null ? true
                : LocalDate.now().isAfter( time.withZoneSameInstant( ZoneId.systemDefault() ).toLocalDate() );

    }

    @MainCommand(
            name = "Daily Currency",
            aliases = { "dailies", "daily", "gib" },
            description = "Gets free currency. Can only be called once per day!",
            usage = "{signature}" )
    public void dailiesCommand( CommandContext context ) {

        final IUser user = context.getAuthor();

        EXECUTOR.execute( user.getStringID(), () -> {

            if ( !isAvailable( user ) ) { // Was already called today.
                context.getReplyBuilder()
                        .withContent(
                                "You already got you dailies today, " + user.getName() + "!\nTry again tomorrow." )
                        .build();
                return;
            }

            long newValue = CurrencyManager.getInstance().deposit( user, DAILY_AMOUNT );
            cooldownMap.put( user.getStringID(), ZonedDateTime.now() ); // Register time called.
            context.getReplyBuilder().withEmbed( new EmbedBuilder().withTitle( "Dailies :moneybag:" )
                    .withColor( UserModule.EMBED_COLOR )
                    .withDesc( user.getName() + ", you got **" + CurrencyManager.format( DAILY_AMOUNT )
                            + "** from your daily!\nYou now have: **" + CurrencyManager.format( newValue ) + "**" )
                    .build() ).build();

        } );

    }

    /* Translator for the timestamp */

    /**
     * Translator for the ZonedDateTime class.
     * 
     * @author ThiagoTGM
     * @version 1.0
     * @since 2018-09-06
     */
    private class ZonedDateTimeTranslator implements Translator<ZonedDateTime> {

        @Override
        public Data toData( ZonedDateTime obj ) throws TranslationException {

            return Data.stringData( obj.toString() );

        }

        @Override
        public ZonedDateTime fromData( Data data ) throws TranslationException {

            if ( !data.isString() ) {
                throw new TranslationException( "Given data is not a string." );
            }

            try {
                return ZonedDateTime.parse( data.getString() );
            } catch ( DateTimeParseException e ) {
                throw new TranslationException( "Could not parse timestamp." );
            }

        }

    }

}
