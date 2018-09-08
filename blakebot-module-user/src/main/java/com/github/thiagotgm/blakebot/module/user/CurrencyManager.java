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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.translate.LongTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.AsyncTools;
import com.github.thiagotgm.blakebot.common.utils.KeyedExecutorService;

import sx.blah.discord.handle.obj.IUser;

/**
 * Manages the currency system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-06
 */
public class CurrencyManager {
	
	private static final Logger LOG = LoggerFactory.getLogger( CurrencyManager.class );
	private static final ThreadGroup THREADS = new ThreadGroup( "Currency System" );
	
	/**
	 * Executor used to perform currency-changing operations. Tasks are
	 * keyed by the string ID of the user to avoid race conditions.
	 */
	protected static final KeyedExecutorService EXECUTOR =
			AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {
		
		LOG.error( "Error while updating currency amount.", e );
		
	});
	
	/**
	 * Value returned by {@link #deposit(IUser,long)} and {@link #withdraw(IUser, long)}
	 * when an error occurs while executing the operation.
	 */
	public static final long ERROR = -2;
	/**
	 * Value returned by {@link #withdraw(IUser, long)} when the user does not have
	 * enough funds to perform the operation.
	 */
	public static final long NOT_ENOUGH_FUNDS = -1;
	
	private static CurrencyManager instance;
	
	/**
	 * Retrieves the running instance of the manager.
	 * 
	 * @return The instance.
	 */
	public synchronized static CurrencyManager getInstance() {
		
		if ( instance == null ) {
			instance = new CurrencyManager();
		}
		return instance;
		
	}
	
	private static final String SYMBOL_SETTING = "Currency Symbol";
	/**
	 * Symbol (prefix) to use to represent the currency.
	 */
	public static final String SYMBOL = Settings.getStringSetting( SYMBOL_SETTING );
	
	/**
	 * Formats the given amount of currency into a string, including the correct
	 * prefix.
	 * 
	 * @param amount The amount of currency.
	 * @return The formatted value.
	 */
	public static String format( long amount ) {
		
		return String.format( "%s%,d", SYMBOL, amount );
		
	}
	
	private final Map<String,Long> currencyMap;
	
	/**
	 * Instantiates a manager.
	 */
	private CurrencyManager() {
		
		currencyMap = DatabaseManager.getDatabase().getDataMap( "CurrencySystem",
				new StringTranslator(), new LongTranslator() );
		
	}
	
	/**
	 * Submits a request to add the given amount to the given user's held currency.
	 * 
	 * @param user The user to add currency to.
	 * @param amount The amount of currency to add.
	 * @return The Future representing the request.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	private Future<Long> requestDeposit( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		if ( user == null ) {
			throw new NullPointerException( "User cannot be null." );
		}
		if ( amount < 0 ) {
			throw new IllegalArgumentException( "Amount cannot be negative." );
		}
		
		String userID = user.getStringID();
		return EXECUTOR.submit( userID, () -> {
			
			Long curAmount = currencyMap.get( userID );
			long newAmount = ( curAmount == null ? 0 : curAmount ) + amount;
			currencyMap.put( userID, newAmount );
			return newAmount;
			
		});
		
	}
	
	/**
	 * Adds the given amount to the given user's held currency.
	 * <p>
	 * The operation is internally executed with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user across different
	 * threads. If calls to this method are parallelized, is not necessary for the caller to
	 * synchronize those calls.
	 * 
	 * @param user The user to add currency to.
	 * @param amount The amount of currency to add.
	 * @return The amount of currency held by the given user after adding the given amount.<br>
	 *         If there was an error while adding, returns {@value #ERROR}.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	public long deposit( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		try {
			return requestDeposit( user, amount ).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for updated currency value." );
			return ERROR;
		} catch ( ExecutionException e ) {
			LOG.error( "Error while updating currency." );
			return ERROR;
		}
		
	}
	
	/**
	 * Adds the given amount to the given user's held currency asynchronously
	 * <p>
	 * The operation is internally executed asynchronously with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user. It is not
	 * necessary for the caller to synchronize calls to this method.
	 * 
	 * @param user The user to add currency to.
	 * @param amount The amount of currency to add.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	public void depositAsync( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		requestDeposit( user, amount );
		
	}
	
	/**
	 * Submits a request to subtract the given amount from the given user's held currency.
	 * <p>
	 * The request will be refused if the amount is greater than what the user has.
	 * 
	 * @param user The user to subtract currency from.
	 * @param amount The amount of currency to subtract.
	 * @return The Future representing the request.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	private Future<Long> requestWithdrawal( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		if ( user == null ) {
			throw new NullPointerException( "User cannot be null." );
		}
		if ( amount < 0 ) {
			throw new IllegalArgumentException( "Amount cannot be negative." );
		}
		
		String userID = user.getStringID();
		return EXECUTOR.submit( userID, () -> {
			
			Long curAmount = currencyMap.get( userID );
			long newAmount = ( curAmount == null ? 0 : curAmount ) - amount;
			if ( newAmount < 0 ) { // Not enough stored.
				return NOT_ENOUGH_FUNDS;
			} else { // Save new amount.
				currencyMap.put( userID, newAmount );
				return newAmount;
			}
			
		});
		
	}
	
	/**
	 * Subtracts the given amount from the given user's held currency.
	 * <p>
	 * The operation is internally executed with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user across different
	 * threads. If calls to this method are parallelized, is not necessary for the caller to
	 * synchronize those calls.
	 * 
	 * @param user The user to subtract currency from.
	 * @param amount The amount of currency to subtract.
	 * @return The amount of currency held by the given user after adding the given amount.<br>
	 *         If there was an error while adding, returns {@value #ERROR}.<br>
	 *         If the given amount is greater than what the given user currently holds, returns
	 *         {@value #NOT_ENOUGH_FUNDS} (the amount held by the user is not changed).
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	public long withdraw( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		try {
			return requestWithdrawal( user, amount ).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for updated currency value." );
			return ERROR;
		} catch ( ExecutionException e ) {
			LOG.error( "Error while updating currency." );
			return ERROR;
		}
		
	}
	
	/**
	 * Subtracts the given amount from the given user's held currency.
	 * <p>
	 * The operation is internally executed asynchronously with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user. It is not
	 * necessary for the caller to synchronize calls to this method.
	 * 
	 * @param user The user to subtract currency from.
	 * @param amount The amount of currency to subtract.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the given amount is negative.
	 */
	public void withdrawAsync( IUser user, long amount )
			throws NullPointerException, IllegalArgumentException {
		
		requestWithdrawal( user, amount );
		
	}
	
	/**
	 * Retrieves the amount of currency currently held by the given user.
	 * 
	 * @param user The user to get the held currency for.
	 * @return The amount of currency held by the given user.
	 */
	public long getCurrency( IUser user ) {
		
		Long amount = currencyMap.get( user.getStringID() );
		return amount == null ? 0 : amount;
		
	}

}
