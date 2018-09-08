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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.Storable;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;
import com.github.thiagotgm.blakebot.common.storage.translate.StorableTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.AsyncTools;
import com.github.thiagotgm.blakebot.common.utils.KeyedExecutorService;

import sx.blah.discord.handle.obj.IUser;

/**
 * Manages the reputation system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-07
 */
public class ReputationManager {
	
	private static final Logger LOG = LoggerFactory.getLogger( ReputationManager.class );
	private static final ThreadGroup THREADS = new ThreadGroup( "Reputation System" );
	
	/**
	 * Executor used to perform reputation-changing operations. Tasks are
	 * keyed by the string ID of the user to avoid race conditions.
	 */
	protected static final KeyedExecutorService EXECUTOR =
			AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {
		
		LOG.error( "Error while updating reputation.", e );
		
	});
	
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
	
	private final Map<String,Reputation> reputationMap;
	
	/**
	 * Instantiates a manager.
	 */
	private ReputationManager() {
		
		reputationMap = Collections.synchronizedMap( DatabaseManager.getDatabase().getDataMap(
				"ReputationSystem", new StringTranslator(),
				new StorableTranslator<>( () -> new Reputation() ) ) );
		
	}
	
	/**
	 * Retrieves the reputation of the given user.
	 * 
	 * @param user The user to get the reputation for.
	 * @return The given user's reputation.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 */
	public Reputation getReputation( IUser user ) throws NullPointerException {
		
		if ( user == null ) {
			throw new NullPointerException( "User argument cannot be null." );
		}
		
		Reputation rep = reputationMap.get( user.getStringID() );
		return rep == null ? new Reputation() : rep;
		
	}
	
	/**
	 * Represents the reputation of a user in the system.
	 * <p>
	 * A user's reputation consists of upvotes and downvotes submitted
	 * by other users.
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
		 * Returns the overall reputation, that is, 
		 * <tt>upvotes - downvotes</tt>.
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
		 * Returns the percentage of votes that are positive
		 * (upvotes).
		 * 
		 * @return The percentage (%) of positive votes. If there
		 *         are no votes, returns 0.
		 */
		public double getPositivePercentage() {
			
			long total = getTotalVotes();
			return total == 0 ? 0.0 : ( upvotes * 100.0 ) / total;
			
		}
		
		/**
		 * Returns the percentage of votes that are negative
		 * (downvotes).
		 * 
		 * @return The percentage (%) of negative votes. If there
		 *         are no votes, returns 0.
		 */
		public double getNegativePercentage() {
			
			long total = getTotalVotes();
			return total == 0 ? 0.0 : ( downvotes * 100.0 ) / total;
			
		}

		@Override
		public Data toData() {

			Map<String,Data> map = new HashMap<>();
			
			map.put( UPVOTES_ATTRIBUTE, Data.numberData( upvotes ) );
			map.put( DOWNVOTES_ATTRIBUTE, Data.numberData( downvotes ) );
			
			return Data.mapData( map );
			
		}

		@Override
		public void fromData( Data data ) throws TranslationException {

			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
			Data upvoteData = map.get( UPVOTES_ATTRIBUTE );
			if ( !upvoteData.isNumber() ) {
				throw new TranslationException( "Upvotes attribute is not a number." );
			}
			upvotes = upvoteData.getNumberInteger();
			
			Data downvoteData = map.get( DOWNVOTES_ATTRIBUTE );
			if ( !downvoteData.isNumber() ) {
				throw new TranslationException( "Downvotes attribute is not a number." );
			}
			upvotes = downvoteData.getNumberInteger();
			
		}
		
	}

}
