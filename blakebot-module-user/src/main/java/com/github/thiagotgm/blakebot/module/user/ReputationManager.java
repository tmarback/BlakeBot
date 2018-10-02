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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.bot_utils.storage.Data;
import com.github.thiagotgm.bot_utils.storage.DatabaseManager;
import com.github.thiagotgm.bot_utils.storage.Storable;
import com.github.thiagotgm.bot_utils.storage.TranslationException;
import com.github.thiagotgm.bot_utils.storage.Translator;
import com.github.thiagotgm.bot_utils.storage.translate.StorableTranslator;
import com.github.thiagotgm.bot_utils.storage.translate.StringTranslator;
import com.github.thiagotgm.bot_utils.utils.AsyncTools;
import com.github.thiagotgm.bot_utils.utils.KeyedExecutorService;
import com.github.thiagotgm.bot_utils.utils.graph.Graphs;
import com.github.thiagotgm.bot_utils.utils.graph.Tree;

import sx.blah.discord.handle.obj.IUser;

/**
 * Manages the reputation system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-07
 */
public class ReputationManager {

    /**
     * Possible votes a user can give to another user.
     * 
     * @author ThiagoTGM
     * @version 1.0
     * @since 2018-09-07
     */
    public enum Vote {
        
        /**
         * A positive vote towards a user's reputation.
         */
        UPVOTE,
        
        /**
        * No vote towards a user's reputation.
        */
        NO_VOTE,
        
        /**
        * A negative vote towards a user's reputation.
        */
        DOWNVOTE
    
    };

    private static final Logger LOG = LoggerFactory.getLogger( ReputationManager.class );
    private static final ThreadGroup THREADS = new ThreadGroup( "Reputation System" );

    /**
     * Executor used to perform reputation-changing operations. Tasks are keyed by
     * the string ID of the user to avoid race conditions.
     */
    protected static final KeyedExecutorService EXECUTOR = AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {

        LOG.error( "Error while updating reputation.", e );

    } );

    private static ReputationManager instance;

    /**
     * Retrieves the running instance of the manager.
     * 
     * @return The instance.
     */
    public synchronized static ReputationManager getInstance() {

        if ( instance == null ) {
            instance = new ReputationManager();
        }
        return instance;

    }

    private final Map<String, Reputation> reputationMap;
    private final Tree<String, Vote> voteMap;

    /**
     * Instantiates a manager.
     */
    private ReputationManager() {

        reputationMap = Collections.synchronizedMap( DatabaseManager.getDatabase().getDataMap( "ReputationSystem",
                new StringTranslator(), new StorableTranslator<>( () -> new Reputation() ) ) );
        voteMap = Graphs.synchronizedTree( DatabaseManager.getDatabase().getDataTree( "ReputationVotes",
                new StringTranslator(), new VoteTranslator() ) );

    }

    /**
     * Retrieves the reputation of the given user.
     * 
     * @param user
     *            The user to get the reputation for.
     * @return The given user's reputation.
     * @throws NullPointerException
     *             if the given user is <tt>null</tt>.
     */
    public Reputation getReputation( IUser user ) throws NullPointerException {

        if ( user == null ) {
            throw new NullPointerException( "User argument cannot be null." );
        }

        Reputation rep = reputationMap.get( user.getStringID() );
        return rep == null ? new Reputation() : rep;

    }

    /**
     * Sets a vote from the given voter user towards the reputation of the given
     * target user. If the voter has placed a vote towards the target before, the
     * previous vote is discarded if different from the given vote. If the given
     * vote is the same as the previous vote, nothing is changed.
     * <p>
     * The operation is internally executed with the appropriate mechanisms to
     * ensure no race conditions occur for multiple calls on the same user (either
     * voter or target) across different threads. If calls to this method are
     * parallelized, is not necessary for the caller to synchronize those calls.
     * 
     * @param voter
     *            The user casting the vote.
     * @param target
     *            The user to whose reputation the vote goes to.
     * @param vote
     *            The vote.
     * @return <tt>true</tt> if the voter's vote towards the target was registered
     *         (either didn't vote on the target before, or had a different
     *         vote).<br>
     *         <tt>false</tt> if the voter has already voted on the target, and that
     *         previous vote was the same as the given vote.
     * @throws NullPointerException
     *             if any of the arguments is <tt>null</tt>.
     * @throws IllegalArgumentException
     *             if the given voter and target are the same user (same ID).
     */
    public boolean vote( IUser voter, IUser target, Vote vote ) throws NullPointerException, IllegalArgumentException {

        if ( ( voter == null ) || ( target == null ) || ( vote == null ) ) {
            throw new NullPointerException( "Arguments cannot be null." );
        }

        if ( voter.getLongID() == target.getLongID() ) {
            throw new IllegalArgumentException( "A user cannot vote on him/herself." );
        }

        final String voterID = voter.getStringID();
        final String targetID = target.getStringID();

        try {
            return EXECUTOR.submit( voterID, () -> {

                Vote curVote = voteMap.get( voterID, targetID );
                if ( curVote == null ) { // No vote in the system.
                    curVote = Vote.NO_VOTE;
                }

                if ( vote == curVote ) {
                    return false; // Same vote as current.
                }

                LOG.debug( "Switching vote from {} towards {}, from {} to {}.", voter.getName(), target.getName(),
                        curVote, vote );
                final Vote oldVote = curVote;
                EXECUTOR.execute( targetID, () -> {

                    Reputation rep = reputationMap.get( targetID ); // Get current rep.
                    if ( rep == null ) { // No reputation yet.
                        rep = new Reputation();
                    }
                    rep.changeVote( oldVote, vote ); // Change vote.
                    reputationMap.put( targetID, rep ); // Update rep.

                } );

                if ( vote == Vote.NO_VOTE ) { // Just remove the vote.
                    voteMap.remove( voterID, targetID );
                } else { // Update the vote.
                    voteMap.put( vote, voterID, targetID );
                }
                return true;

            } ).get();
        } catch ( InterruptedException | ExecutionException e ) {
            LOG.error( "Error while submitting vote.", e );
            return false;
        }

    }

    /**
     * Represents the reputation of a user in the system.
     * <p>
     * A user's reputation consists of upvotes and downvotes submitted by other
     * users.
     * 
     * @author ThiagoTGM
     * @version 1.0
     * @since 2018-09-07
     */
    public static class Reputation implements Storable {

        private static final String UPVOTES_ATTRIBUTE = "upvotes";
        private static final String DOWNVOTES_ATTRIBUTE = "downvotes";

        private long upvotes;
        private long downvotes;

        /**
         * Construct a new instance with no votes.
         */
        public Reputation() {

            upvotes = 0;
            downvotes = 0;

        }

        /**
         * Returns the overall reputation, that is, <tt>upvotes - downvotes</tt>.
         * 
         * @return The overall reputation.
         */
        public long getOverall() {

            return upvotes - downvotes;

        }

        /**
         * Returns the total amount of votes cast, that is,
         * <tt>upvotes + downvotes</tt>.
         * 
         * @return The total amount of votes.
         */
        public long getTotalVotes() {

            return upvotes + downvotes;

        }

        /**
         * Returns the percentage of votes that are positive (upvotes).
         * 
         * @return The percentage (%) of positive votes. If there are no votes, returns
         *         0.
         */
        public double getPositivePercentage() {

            long total = getTotalVotes();
            return total == 0 ? 0.0 : ( upvotes * 100.0 ) / total;

        }

        /**
         * Returns the percentage of votes that are negative (downvotes).
         * 
         * @return The percentage (%) of negative votes. If there are no votes, returns
         *         0.
         */
        public double getNegativePercentage() {

            long total = getTotalVotes();
            return total == 0 ? 0.0 : ( downvotes * 100.0 ) / total;

        }

        /**
         * Changes the value of a vote towards this reputation.
         * <p>
         * e.g. removes a vote of the type given by oldVote, and adds a vote of the type
         * given by newVote.
         * 
         * @param oldVote
         *            The previous value of the vote.
         * @param newVote
         *            The new value of the vote.
         */
        protected void changeVote( Vote oldVote, Vote newVote ) {

            switch ( oldVote ) { // Remove effect of old vote.

                case UPVOTE:
                    upvotes -= 1; // Remove the upvote
                    break;

                case DOWNVOTE:
                    downvotes -= 1; // Remove the downvote.
                    break;

                case NO_VOTE:
                    // Do nothing.
                    break;

            }

            switch ( newVote ) {

                case UPVOTE:
                    upvotes += 1; // Add the upvote
                    break;

                case DOWNVOTE:
                    downvotes += 1; // Add the downvote.
                    break;

                case NO_VOTE:
                    // Do nothing.
                    break;

            }

        }

        @Override
        public Data toData() {

            Map<String, Data> map = new HashMap<>();

            map.put( UPVOTES_ATTRIBUTE, Data.numberData( upvotes ) );
            map.put( DOWNVOTES_ATTRIBUTE, Data.numberData( downvotes ) );

            return Data.mapData( map );

        }

        @Override
        public void fromData( Data data ) throws TranslationException {

            if ( !data.isMap() ) {
                throw new TranslationException( "Given data is not a map." );
            }
            Map<String, Data> map = data.getMap();

            Data upvoteData = map.get( UPVOTES_ATTRIBUTE );
            if ( !upvoteData.isNumber() ) {
                throw new TranslationException( "Upvotes attribute is not a number." );
            }
            upvotes = upvoteData.getNumberInteger();

            Data downvoteData = map.get( DOWNVOTES_ATTRIBUTE );
            if ( !downvoteData.isNumber() ) {
                throw new TranslationException( "Downvotes attribute is not a number." );
            }
            downvotes = downvoteData.getNumberInteger();

        }

    }

    /* Translator for the enum */

    /**
     * Translator for Vote values.
     * 
     * @author ThiagoTGM
     * @version 1.0
     * @since 2018-09-07
     */
    private class VoteTranslator implements Translator<Vote> {

        @Override
        public Data toData( Vote obj ) throws TranslationException {

            return Data.stringData( obj.toString() );

        }

        @Override
        public Vote fromData( Data data ) throws TranslationException {

            if ( !data.isString() ) {
                throw new TranslationException( "Given data is not a string." );
            }
            try {
                return Vote.valueOf( data.getString() );
            } catch ( IllegalArgumentException e ) {
                throw new TranslationException( "String data is not a valid vote." );
            }

        }

    }

}
