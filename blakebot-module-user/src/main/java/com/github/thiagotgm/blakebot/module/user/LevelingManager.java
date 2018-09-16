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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.bot_utils.Settings;
import com.github.thiagotgm.bot_utils.storage.Data;
import com.github.thiagotgm.bot_utils.storage.DatabaseManager;
import com.github.thiagotgm.bot_utils.storage.Storable;
import com.github.thiagotgm.bot_utils.storage.TranslationException;
import com.github.thiagotgm.bot_utils.storage.translate.StorableTranslator;
import com.github.thiagotgm.bot_utils.storage.translate.StringTranslator;
import com.github.thiagotgm.bot_utils.utils.AsyncTools;
import com.github.thiagotgm.bot_utils.utils.KeyedExecutorService;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

/**
 * Manages the user leveling system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-05
 */
public class LevelingManager {

    private static final Logger LOG = LoggerFactory.getLogger( LevelingManager.class );
    private static final ThreadGroup THREADS = new ThreadGroup( "Leveling System" );
    private static final Clock CLOCK = Clock.systemDefaultZone();

    /**
     * Executor used to perform EXP-giving operations (including leveling). Tasks
     * are keyed by the string ID of the user to avoid race conditions.
     */
    protected static final KeyedExecutorService EXECUTOR = AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {

        LOG.error( "Error while giving experience.", e );

    } );

    /**
     * Setting that defines {@link #COOLDOWN}.
     */
    public static final String COOLDOWN_SETTING = "EXP Cooldown";
    /**
     * How long after EXP is given to a user that the user may get EXP again, in
     * seconds.
     */
    public static final int COOLDOWN = Settings.getIntSetting( COOLDOWN_SETTING );

    private static LevelingManager instance;

    /**
     * Retrieves the running instance of the manager.
     * 
     * @return The instance.
     */
    public synchronized static LevelingManager getInstance() {

        if ( instance == null ) {
            instance = new LevelingManager();
        }
        return instance;

    }

    /**
     * Determines the amount of currency that should be gifted to a user when that
     * user levels up.
     * 
     * @param newLevel
     *            The level that the user leveled up to.
     * @return The amount of currency that should be gifted.
     */
    private static long getCurrencyGift( int newLevel ) {

        return newLevel * 10;

    }

    private final Map<String, LevelState> stateMap;
    private final Set<String> cooldownUsers;
    private final ScheduledExecutorService cooldownRemover;

    /**
     * Instantiates a manager.
     */
    private LevelingManager() {

        stateMap = Collections.synchronizedMap( DatabaseManager.getDatabase().getDataMap( "LevelSystem",
                new StringTranslator(), new StorableTranslator<>( () -> new LevelState() ) ) );
        cooldownUsers = Collections.synchronizedSet( new HashSet<>() );
        cooldownRemover = Executors.newSingleThreadScheduledExecutor( t -> new Thread( t, "EXP Cooldown Remover" ) );

        LOG.debug( "EXP cooldown is {} seconds.", COOLDOWN );

    }

    /**
     * Retrieves the state of the given user in the leveling system.
     * 
     * @param user
     *            The user to get the state of.
     * @return The leveling state of the user.
     * @throws NullPointerException
     *             if the user is <tt>null</tt>.
     */
    public LevelState getLevelState( IUser user ) throws NullPointerException {

        if ( user == null ) {
            throw new NullPointerException( "User cannot be null." );
        }

        LevelState state = stateMap.get( user.getStringID() );
        return state == null ? new LevelState() : state;

    }

    /**
     * Grants the given user a random amount of EXP in the range
     * [{@value LevelState#MIN_EXP},{@value LevelState#MAX_EXP}]. If the gained EXP
     * is enough to level up, the level is updated (any excess EXP is put towards
     * the next levelup).
     * 
     * @param user
     *            The user to give EXP to.
     * @return If the gained EXP was enough for the user to level up, returns the
     *         new level. Else, returns 0.
     * @throws NullPointerException
     *             if the user is <tt>null</tt>.
     */
    protected int gainExp( IUser user ) throws NullPointerException {

        if ( user == null ) {
            throw new NullPointerException( "User cannot be null." );
        }

        LOG.trace( "Giving EXP to {}#{}.", user.getName(), user.getDiscriminator() );

        LevelState level = stateMap.get( user.getStringID() );
        if ( level == null ) { // User not in the system yet.
            level = new LevelState();
        }
        boolean result = level.gainExp(); // Give EXP.
        stateMap.put( user.getStringID(), level ); // Store updated state.

        return result ? level.getLevel() : 0; // Return whether leveled up.

    }

    /**
     * Handles a received message.
     * <p>
     * If the author is not currently on cooldown, gives EXP to the author of the
     * message. If the author leveled up with the given EXP and the author is not a
     * bot, sends a message to indicate the levelup.
     * <p>
     * The experience gain is internally executed asynchronously with the
     * appropriate mechanisms to ensure no race conditions occur for multiple calls
     * on the same user. It is not necessary for the caller to parallelize (or
     * synchronize) multiple calls to this method.
     * 
     * @param e
     *            The event triggered by the message.
     */
    @EventSubscriber
    public void handleMessage( MessageReceivedEvent e ) {

        IUser user = e.getAuthor();
        EXECUTOR.execute( user.getStringID(), () -> {

            if ( !cooldownUsers.contains( user.getStringID() ) ) { // Not on cooldown.
                int newLevel = gainExp( user ); // Give EXP.
                if ( newLevel != 0 ) {
                    long currencyGift = getCurrencyGift( newLevel );
                    long newAmount = CurrencyManager.getInstance().deposit( user, currencyGift );
                    if ( !user.isBot() ) { // Send message only to non-bots.
                        new MessageBuilder( e.getClient() ).withChannel( e.getChannel() )
                                .withEmbed( new EmbedBuilder().withTimestamp( CLOCK.instant() )
                                        .withColor( UserModule.EMBED_COLOR )
                                        .withTitle( user.getName() + " has leveled up! :tada:" )
                                        .withDesc( "Hooray! You got **" + CurrencyManager.format( currencyGift )
                                                + "** as a gift! :moneybag:" )
                                        .appendField( "Level", String.valueOf( newLevel ), true )
                                        .appendField( "Money", CurrencyManager.format( newAmount ), true ).build() )
                                .build(); // Leveled up!
                    }
                }
                cooldownUsers.add( user.getStringID() ); // Add to cooldown set.
                cooldownRemover.schedule( () -> {
                    cooldownUsers.remove( user.getStringID() );
                }, COOLDOWN, TimeUnit.SECONDS ); // Auto-removed after cooldown elapsed.
            }

        } );

    }

    /**
     * Represents the state of a user in the leveling system at a certain moment.
     * 
     * @author ThiagoTGM
     * @version 1.0
     * @since 2018-09-05
     */
    public static class LevelState implements Storable {

        private static final String LEVEL_ATTRIBUTE = "level";
        private static final String EXP_ATTRIBUTE = "exp";

        /**
         * Minimum amount of EXP that can be gained by calling {@link #gainExp()},
         * inclusive.
         */
        public static final int MIN_EXP = 10;
        /**
         * Maximum amount of EXP that can be gained by calling {@link #gainExp()},
         * inclusive.
         */
        public static final int MAX_EXP = 20;
        private static final int MAX_EXP_BOUND = MAX_EXP + 1;

        private int level;
        private long exp;

        /**
         * Instantiates a LevelState with level 1 and no exp.
         */
        protected LevelState() {

            this.level = 1;
            this.exp = 0;

        }

        /**
         * Retrieves the level.
         * 
         * @return The level.
         */
        public int getLevel() {

            return level;

        }

        /**
         * Retrieves the amount of EXP.
         * 
         * @return The EXP amount.
         */
        public long getExp() {

            return exp;

        }

        /**
         * Determines how much EXP is necessary to reach the next level.
         * 
         * @return The amount of EXP for the next level. This value is always greater
         *         than 0.
         */
        public long getExpToNextLevel() {

            return level * 130 + 70;

        }

        /**
         * Gains a random amount of EXP in the range
         * [{@value #MIN_EXP},{@value #MAX_EXP}]. If the gained EXP is enough to level
         * up, the level is updated (any excess EXP is put towards the next levelup).
         * 
         * @return <tt>true</tt> if the gained EXP was enough to level up.
         *         <tt>false</tt> otherwise.
         */
        protected boolean gainExp() {

            int expGain = ThreadLocalRandom.current().nextInt( MIN_EXP, MAX_EXP_BOUND );
            LOG.trace( "Got {} EXP.", expGain );
            exp += expGain; // Add EXP.

            long toNext = getExpToNextLevel();
            if ( exp >= toNext ) {
                level++; // Leveled up.
                exp -= toNext; // Spend EXP to level up.
                return true;
            } else {
                return false;
            }

        }

        @Override
        public Data toData() {

            Map<String, Data> map = new HashMap<>();
            map.put( LEVEL_ATTRIBUTE, Data.numberData( level ) );
            map.put( EXP_ATTRIBUTE, Data.numberData( exp ) );
            return Data.mapData( map );

        }

        @Override
        public void fromData( Data data ) throws TranslationException {

            if ( !data.isMap() ) {
                throw new TranslationException( "Given data is not a map." );
            }
            Map<String, Data> map = data.getMap();

            Data levelData = map.get( LEVEL_ATTRIBUTE );
            if ( levelData == null ) {
                throw new TranslationException( "Missing level attribute." );
            }
            if ( !levelData.isNumber() ) {
                throw new TranslationException( "Level attribute isn't a number." );
            }
            level = (int) levelData.getNumberInteger();

            Data expData = map.get( EXP_ATTRIBUTE );
            if ( expData == null ) {
                throw new TranslationException( "Missing exp attribute." );
            }
            if ( !expData.isNumber() ) {
                throw new TranslationException( "Exp attribute isn't a number." );
            }
            exp = expData.getNumberInteger();

        }

    }

}
