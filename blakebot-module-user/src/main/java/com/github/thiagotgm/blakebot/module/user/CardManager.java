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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.Storable;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;
import com.github.thiagotgm.blakebot.common.storage.translate.ListTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.MapTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StorableTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.AsyncTools;
import com.github.thiagotgm.blakebot.common.utils.KeyedExecutorService;
import com.github.thiagotgm.blakebot.common.utils.Tree;
import com.github.thiagotgm.blakebot.common.utils.Utils;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Manages the custom card system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-08
 */
public class CardManager {
	
	private static final Logger LOG = LoggerFactory.getLogger( CardManager.class );
	private static final ThreadGroup THREADS = new ThreadGroup( "Custom Card System" );
	
	/**
	 * Executor used to perform card editing operations. Tasks are
	 * keyed by the string ID of the user to avoid race conditions.
	 */
	protected static final KeyedExecutorService EXECUTOR =
			AsyncTools.createKeyedThreadPool( THREADS, ( t, e ) -> {
		
		if ( !( e instanceof IllegalArgumentException ) ) {
			LOG.error( "Error while updating custom card.", e );
		}
		
	});
	
	private static CardManager instance;
	
	/**
	 * Retrieves the running instance of the manager.
	 * 
	 * @return The instance.
	 */
	public synchronized static CardManager getInstance() {
		
		if ( instance == null ) {
			instance = new CardManager();
		}
		return instance;
		
	}
	
	private final Tree<String,Card> cardMap;
	private final Map<String,UserCards> userMap;
	
	/**
	 * Instantiates a manager.
	 */
	private CardManager() {
		
		cardMap = Utils.synchronizedTree( DatabaseManager.getDatabase()
				.getDataTree( "CustomCards", new StringTranslator(),
						new StorableTranslator<>( () -> new Card() ) ) );
		userMap = Collections.synchronizedMap( DatabaseManager.getDatabase()
				.getDataMap( "UserCustomCards", new StringTranslator(), 
						new StorableTranslator<>( () -> new UserCards() ) ) );
		
	}
	
	/**
	 * Retrieves the card owned by the given user with the given title.
	 * 
	 * @param user The user that owns the card.
	 * @param title The title of the card.
	 * @return The card, or <tt>null</tt> if there is no card owned by
	 *         the given user with the given title.
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 */
	public Card getCard( IUser user, String title ) throws NullPointerException {
		
		if ( ( user == null ) || ( title == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		return cardMap.get( user.getStringID(), title );
		
	}
	
	/**
	 * Retrieves the given user's current custom card data.
	 * 
	 * @param user The user.
	 * @return The user's cards and allowance.
	 * @throws NullPointerException if the given user is <tt>null</tt>.
	 */
	public UserCards getUserCards( IUser user ) throws NullPointerException {
		
		if ( user == null ) {
			throw new NullPointerException( "User cannot be null." );
		}
		UserCards cards = userMap.get( user.getStringID() );
		return cards == null ? new UserCards() : cards;
		
	}
	
	/**
	 * Adds a blank card with the given title for the given user.
	 * <p>
	 * The operation is internally executed with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user across different
	 * threads. If calls to this method are parallelized, is not necessary for the caller to
	 * synchronize those calls.
	 * 
	 * @param user The user to add the card to.
	 * @param cardTitle The card title.
	 * @return <tt>true</tt> if added successfully.
	 *         <tt>false</tt> if the user is already fully using his/hers
	 *         current card allowance.
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 * @throws IllegalArgumentException if the title is longer than the
	 *               {@link Card#MAX_TITLE_LENGTH maximum title length},
	 *                                  or if the user already has a card
	 *                                  with the given title. The exception's
	 *                                  detail message will be an error message
	 *                                  that indicates the error.
	 */
	public boolean addCard( IUser user, String cardTitle )
			throws NullPointerException, IllegalArgumentException {
		
		if ( ( user == null ) || ( cardTitle == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		String userID = user.getStringID();
		try {
			return EXECUTOR.submit( userID, () -> {
				
				UserCards cards = getUserCards( user );
				Card card = new Card( cardTitle );
				if ( !cards.addCard( card ) ) {
					return false; // Reached allowance.
				}
				cardMap.set( card, userID, cardTitle );
				userMap.put( userID, cards );
				return true;
				
			}).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for card add.", e );
			return false;
		} catch ( ExecutionException e ) {
			if ( e.getCause() instanceof IllegalArgumentException ) {
				throw (IllegalArgumentException) e.getCause(); // Expected.
			} else {
				return false;
			}
		}
		
	}
	
	/**
	 * Removes the card with the given title for the given user.
	 * <p>
	 * The operation is internally executed with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user across different
	 * threads. If calls to this method are parallelized, is not necessary for the caller to
	 * synchronize those calls.
	 * 
	 * @param user The user to remove the card from.
	 * @param cardTitle The card title.
	 * @return <tt>true</tt> if removed successfully.
	 *         <tt>false</tt> if the user does not have any cards with the given title.
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 */
	public boolean removeCard( IUser user, String cardTitle ) throws NullPointerException {
		
		if ( ( user == null ) || ( cardTitle == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		String userID = user.getStringID();
		try {
			return EXECUTOR.submit( userID, () -> {
				
				Card card = cardMap.remove( userID, cardTitle );
				if ( card == null ) {
					return false; // Card doesn't exist.
				}
				UserCards cards = userMap.get( userID );
				cards.removeCard( card ); // Remove from list.
				userMap.put( userID, cards );
				return true;
				
			}).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for card remove.", e );
			return false;
		} catch ( ExecutionException e ) {
			if ( e.getCause() instanceof IllegalArgumentException ) {
				throw (IllegalArgumentException) e.getCause(); // Expected.
			} else {
				return false;
			}
		}
		
	}
	
	/**
	 * Changes the title of a card.
	 * <p>
	 * The operation is internally executed with the appropriate mechanisms
	 * to ensure no race conditions occur for multiple calls on the same user across different
	 * threads. If calls to this method are parallelized, is not necessary for the caller to
	 * synchronize those calls.
	 * 
	 * @param user The user that owns the card.
	 * @param curTitle The current title of the card.
	 * @param newTitle The title to change the card to.
	 * @return <tt>true</tt> if changed successfully.
	 *         <tt>false</tt> if the user does not have any cards with the given title
	 *         (<tt>curTitle</tt>).
	 * @throws NullPointerException if either argument is <tt>null</tt>.
	 * @throws IllegalArgumentException if the new title is longer than the
	 *               {@link Card#MAX_TITLE_LENGTH maximum title length},
	 *                                  or if the user already has a card
	 *                                  with the given new title. The exception's
	 *                                  detail message will be an error message
	 *                                  that indicates the error.
	 */
	public boolean setCardTitle( IUser user, String curTitle, String newTitle )
			throws NullPointerException, IllegalArgumentException {
		
		if ( ( user == null ) || ( curTitle == null ) || ( newTitle == null ) ) {
			throw new NullPointerException( "Arguments cannot be null." );
		}
		
		new Card( newTitle ); // Check new title is valid.
		
		String userID = user.getStringID();
		try {
			return EXECUTOR.submit( userID, () -> {
				
				if ( cardMap.containsPath( userID, newTitle ) ) { // New title already taken.
					throw new IllegalArgumentException( "A card with the new title already exists!" );
				}
				
				Card card = cardMap.remove( userID, curTitle ); // Remove from old title key.
				if ( card == null ) {
					return false; // Card doesn't exist.
				}
				UserCards cards = userMap.get( userID );
				cards.removeCard( card ); // Remove from list.
				
				card.setTitle( newTitle ); // Update title.
				cardMap.add( card, userID, newTitle ); // Insert with new title key.
				cards.addCard( card );
				userMap.put( userID, cards ); // Update list.
				return true;
				
			}).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for card remove.", e );
			return false;
		} catch ( ExecutionException e ) {
			if ( e.getCause() instanceof IllegalArgumentException ) {
				throw (IllegalArgumentException) e.getCause(); // Expected.
			} else {
				return false;
			}
		}
		
	}
	
	/**
	 * Purchases an extra card slot for the given user, using his/hers bot currency.
	 * 
	 * @param user The user to purchase a slot for.
	 * @return <tt>true</tt> if the purchase was successful.
	 *         <tt>false</tt> if the given user does not own enough currency to purchase an extra
	 *         slot.
	 * @throws NullPointerException if the user is <tt>null</tt>.
	 * @throws IllegalArgumentException if the user already has the maximum amount of card slots.
	 *                                  The exception's detail message will be an error message
	 *                                  that indicates the error.
	 */
	public boolean buySlot( IUser user ) throws NullPointerException, IllegalArgumentException {
		
		if ( user == null ) {
			throw new NullPointerException( "User cannot be null." );
		}
		
		String userID = user.getStringID();
		try {
			return EXECUTOR.submit( userID, () -> {
				
				UserCards cards = userMap.get( userID );
				if ( !cards.canIncrementCardAllowance() ) { // Check that can increment.
					throw new IllegalArgumentException( "You already have all card slots unlocked!" );
				}
				
				if ( CurrencyManager.getInstance().withdraw( user, UserCards.EXTRA_CARD_COST ) < 0 ) {
					return false; // Not enough funds (or currency error).
				}
				
				cards.incrementCardAllowance();
				userMap.put( userID, cards ); // Update user data.
				
				return true;
				
			}).get();
		} catch ( InterruptedException e ) {
			LOG.error( "Interrupted while waiting for card remove.", e );
			return false;
		} catch ( ExecutionException e ) {
			if ( e.getCause() instanceof IllegalArgumentException ) {
				throw (IllegalArgumentException) e.getCause(); // Expected.
			} else {
				return false;
			}
		}
		
	}
	
	/**
	 * A custom card that can be set up by a user and displayed
	 * as an embed.
	 * 
	 * @author ThiagoTGM
	 * @version 1.0
	 * @since 2018-09-08
	 */
	public static class Card implements Storable {
		
		/**
		 * Maximum length of the {@link #setAuthor(String) author name}, in characters.
		 */
		public static final int MAX_AUTHOR_LENGTH = 64;
		/**
		 * Maximum length of the {@link #setTitle(String) title}, in characters.
		 */
		public static final int MAX_TITLE_LENGTH = 128;
		/**
		 * Maximum length of the {@link #setDescription(String) description}, in characters.
		 */
		public static final int MAX_DESCRIPTION_LENGTH = 512;
		/**
		 * Maximum length of a {@link #setField(String,String) field}'s name, in characters.
		 */
		public static final int MAX_FIELD_NAME_LENGTH = 128;
		/**
		 * Maximum length of a {@link #setField(String,String) field}'s text, in characters.
		 */
		public static final int MAX_FIELD_TEXT_LENGTH = 512;
		/**
		 * Maximum length of the {@link #setFooter(String) footer}, in characters.
		 */
		public static final int MAX_FOOTER_LENGTH = 64;
		/**
		 * Maximum amount of {@link #getFieldCount() fields} that a card can have.
		 */
		public static final int MAX_FIELDS = 8;
		
		private String title;
		private String description;
		private String url;
		private String footer;
		private String footerIcon;
		private String image;
		private String thumbnail;
		private String author;
		private String authorURL;
		private String authorIcon;
		private final SortedMap<String,String> fields;
		
		/**
		 * Initializes a card with no attributes.
		 */
		private Card() {
			
			this.fields = new TreeMap<>();
			
		}
		
		/**
		 * Initializes a card with the given title, and no other attributes.
		 * 
		 * @param title The title of the card.
		 * @throws NullPointerException if the given title is <tt>null</tt>.
		 * @throws IllegalArgumentException if the given title is empty, only contains whitespace,
		 *                                  or is larger than the
		 *                                  {@link #MAX_TITLE_LENGTH maximum title size}.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 */
		public Card( String title ) throws NullPointerException, IllegalArgumentException {
			
			this();
			setTitle( title );
			
		}
		
		/**
		 * Retrieves the Embed that represents this card.
		 * 
		 * @return This card, as an Embed.
		 */
		public EmbedObject getEmbed() {
			
			EmbedBuilder builder = new EmbedBuilder()
					.withColor( UserModule.EMBED_COLOR )
					.withTitle( title )
					.withDescription( description )
					.withUrl( url )
					.withImage( image )
					.withThumbnail( thumbnail );
			
			if ( footer != null ) {
				builder.withFooterText( footer )
					   .withFooterIcon( footerIcon );
			}
			
			if ( author != null ) {
				builder.withAuthorName( author )
					   .withAuthorUrl( authorURL )
					   .withAuthorIcon( authorIcon );
			}
			
			for ( Map.Entry<String,String> field : fields.entrySet() ) {
				
				builder.appendField( field.getKey(), field.getValue(), false );
				
			}
			
			return builder.build();
			
		}
		
		/**
		 * Checks that the value given for an attribute is within acceptable length.
		 * If the value is longer than allowed, throws an exception with the appropriate
		 * error message as the detail message.
		 * 
		 * @param attribute The name of the attribute.
		 * @param value The value received.
		 * @param maxLength The maximum acceptable length for the attribute.
		 * @throws IllegalArgumentException if the value is longer than the maximum acceptable length.
		 */
		private static void checkLength( String attribute, String value, int maxLength )
				throws IllegalArgumentException {
			
			if ( value.length() > maxLength ) {
				throw new IllegalArgumentException( String.format( "The %s can only have up to %d "
						+ "characters. Given %s has %d characters.", attribute, maxLength, attribute,
						value.length() ) );
			}
			
		}
		
		/**
		 * Sets the title of this Card.
		 * 
		 * @param title The title to be set.
		 * @throws NullPointerException if the given title is <tt>null</tt>.
		 * @throws IllegalArgumentException if the given title is empty, only contains whitespace,
		 *                                  or is larger than the
		 *                                  {@link #MAX_TITLE_LENGTH maximum title size}.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 */
		public void setTitle( String title ) throws NullPointerException, IllegalArgumentException {
			
			if ( title == null ) {
				throw new NullPointerException( "Title cannot be null." );
			}
			if ( title.trim().isEmpty() ) {
				throw new IllegalArgumentException( "Title cannot be empty." );
			}
			
			checkLength( "title", title, MAX_TITLE_LENGTH );
			
			this.title = title;
			
		}
		
		/**
		 * Sets the description of the card.
		 * 
		 * @param description The description to be set. If <tt>null</tt>, the current description
		 *                    is deleted.
		 * @throws IllegalArgumentException if the given description is larger than the
		 *                                  {@link #MAX_DESCRIPTION_LENGTH maximum description size}.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 */
		public void setDescription( String description ) throws IllegalArgumentException {
			
			if ( description != null ) {
				checkLength( "description", description, MAX_DESCRIPTION_LENGTH );
			}
			this.description = description;
			
		}
		
		/**
		 * Sets the URL of the card (the title will be a link to the URL).
		 * 
		 * @param url The URL to be set. If <tt>null</tt>, the current URL
		 *            is deleted.
		 */
		public void setUrl( String url ) {
			
			this.url = url;
			
		}
		
		/**
		 * Sets the image of the card.
		 * 
		 * @param imageUrl The URL of the image to be set. If <tt>null</tt>, the
		 *                 current image is deleted.
		 */
		public void setImage( String imageUrl ) {
			
			this.image = imageUrl;
			
		}
		
		/**
		 * Sets the thumbnail of the card.
		 * 
		 * @param thumbnailUrl The URL of the thumbnail to be set. If <tt>null</tt>, the
		 *                     current thumbnail is deleted.
		 */
		public void setThumbnail( String thumbnailUrl ) {
			
			this.thumbnail = thumbnailUrl;
			
		}
		
		/**
		 * Sets the footer of the card (shown at the bottom of the card).
		 * 
		 * @param footer The footer to be set. If <tt>null</tt>, the current footer
		 *                    is deleted.
		 * @throws IllegalArgumentException if the given footer is larger than the
		 *                                  {@link #MAX_FOOTER_LENGTH maximum footer size}.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 */
		public void setFooter( String footer ) throws IllegalArgumentException {
			
			if ( footer != null ) {
				checkLength( "footer", footer, MAX_FOOTER_LENGTH );
			}
			this.footer = footer;
			
		}
		
		/**
		 * Sets the icon of the footer (shown next to the footer).
		 * <p>
		 * The icon only appears if the footer is present.
		 * 
		 * @param iconUrl The URL of the icon to be set. If <tt>null</tt>, the
		 *                 current icon is deleted.
		 */
		public void setFooterIcon( String iconUrl ) {
			
			this.footerIcon = iconUrl;
			
		}
		
		/**
		 * Sets the name of the author of the card (shown above the title).
		 * 
		 * @param author The author to be set. If <tt>null</tt>, the current author
		 *               is deleted.
		 * @throws IllegalArgumentException if the given author name is larger than the
		 *                                  {@link #MAX_AUTHOR_LENGTH maximum author size}.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 */
		public void setAuthor( String author ) throws IllegalArgumentException {
			
			if ( author != null ) {
				checkLength( "author", author, MAX_AUTHOR_LENGTH );
			}
			this.author = author;
			
		}
		
		/**
		 * Sets the URL of the author (the author name will be a link to this URL).
		 * 
		 * @param url The URL to be set. If <tt>null</tt>, the current URL is deleted.
		 */
		public void setAuthorUrl( String url ) {
			
			this.authorURL = url;
			
		}
		
		/**
		 * Sets the icon of the author (shown next to the author).
		 * <p>
		 * The icon only appears if the author is present.
		 * 
		 * @param iconUrl The URL of the icon to be set. If <tt>null</tt>, the
		 *                 current icon is deleted.
		 */
		public void setAuthorIcon( String iconUrl ) {
			
			this.authorIcon = iconUrl;
			
		}
		
		/**
		 * Sets the text of a field in this card.
		 * 
		 * @param fieldName The name of the field. If there is no field with this name,
		 *                  and <tt>fieldText</tt> is not <tt>null</tt>, one is created.
		 * @param fieldText The text to set to the field. If <tt>null</tt>, the field
		 *                  with the given name is deleted.
		 * @return If <tt>fieldText</tt> is not <tt>null</tt>: <tt>true</tt> if the field
		 *         set successfully, <tt>false</tt> if there was no field with the given
		 *         name and this card already has the {@link #MAX_FIELDS maximum amount of fields}.<br>
		 *         If <tt>fieldText</tt> is <tt>null</tt>: <tt>true</tt> if the field with
		 *         the given name was deleted successfully, <tt>false</tt> if there was no
		 *         field with the given name.
		 * @throws NullPointerException if <tt>fieldName</tt> is <tt>null</tt>.
		 * @throws IllegalArgumentException if <tt>fieldName</tt> or <tt>fieldText</tt> is an empty
		 *                                  string, only contains white space, or exceeds the
		 *                                  {@link #MAX_FIELD_NAME_LENGTH maximum name size} or
		 *                                  {@link #MAX_FIELD_TEXT_LENGTH maximum text size},
		 *                                  respectively.
		 *                                  The exception's detail message will be an error message
		 *                                  that indicates what the error was.
		 *                                  
		 */
		public boolean setField( String fieldName, String fieldText )
				throws NullPointerException, IllegalArgumentException {
			
			if ( fieldName == null ) {
				throw new NullPointerException( "Field name cannot be null." );
			}
			if ( fieldName.trim().isEmpty() ) {
				throw new IllegalArgumentException( "Field name cannot be empty." );
			}
			
			if ( fieldText == null ) {
				return fields.remove( fieldName ) != null;
			} else {
				if ( !fields.containsKey( fieldName ) ) { // No field with given title.
					if ( fields.size() >= MAX_FIELDS ) {
						return false; // Already hit max fields.
					}
					if ( fieldText.trim().isEmpty() ) { // Check valid title.
						throw new IllegalArgumentException( "Field text cannot be empty." );
					}
					checkLength( "field name", fieldName, MAX_FIELD_NAME_LENGTH );
				}
				checkLength( "field text", fieldText, MAX_FIELD_TEXT_LENGTH );
				
				fields.put( fieldName, fieldText );
				return true;
			}
			
		}
		
		/**
		 * Retrieves how many fields this card currently has.
		 * 
		 * @return The field count.
		 */
		public int getFieldCount() {
			
			return fields.size();
			
		}
		
		private static final String TITLE_ATTRIBUTE = "title";
		private static final String DESCRIPTION_ATTRIBUTE = "description";
		private static final String URL_ATTRIBUTE = "url";
		private static final String FOOTER_ATTRIBUTE = "footer";
		private static final String FOOTER_ICON_ATTRIBUTE = "footer_icon";
		private static final String IMAGE_ATTRIBUTE = "image";
		private static final String THUMBNAIL_ATTRIBUTE = "thumbnail";
		private static final String AUTHOR_ATTRIBUTE = "author";
		private static final String AUTHOR_URL_ATTRIBUTE = "author_url";
		private static final String AUTHOR_ICON_ATTRIBUTE = "author_icon";
		private static final String FIELDS_ATTRIBUTE = "fields";
		
		private static final Translator<Map<String,String>> FIELDS_TRANSLATOR =
				new MapTranslator<>( new StringTranslator(), new StringTranslator() );

		/**
		 * Stores this card into a Data.
		 */
		@Override
		public Data toData() {
			
			Map<String,Data> map = new HashMap<>();
			
			map.put( TITLE_ATTRIBUTE, Data.stringData( title ) );
			if ( description != null ) {
				map.put( DESCRIPTION_ATTRIBUTE, Data.stringData( description ) );
			}
			if ( url != null ) {
				map.put( URL_ATTRIBUTE, Data.stringData( url ) );
			}
			if ( footer != null ) {
				map.put( FOOTER_ATTRIBUTE, Data.stringData( footer ) );
			}
			if ( footerIcon != null ) {
				map.put( FOOTER_ICON_ATTRIBUTE, Data.stringData( footerIcon ) );
			}
			if ( image != null ) {
				map.put( IMAGE_ATTRIBUTE, Data.stringData( image ) );
			}
			if ( thumbnail != null ) {
				map.put( THUMBNAIL_ATTRIBUTE, Data.stringData( thumbnail ) );
			}
			if ( author != null ) {
				map.put( AUTHOR_ATTRIBUTE, Data.stringData( author ) );
			}
			if ( authorURL != null ) {
				map.put( AUTHOR_URL_ATTRIBUTE, Data.stringData( authorURL ) );
			}
			if ( authorIcon != null ) {
				map.put( AUTHOR_ICON_ATTRIBUTE, Data.stringData( authorIcon ) );
			}
			if ( !fields.isEmpty() ) {
				map.put( FIELDS_ATTRIBUTE, FIELDS_TRANSLATOR.toData( fields ) );
			}

			return Data.mapData( map );
			
		}

		/**
		 * Loads the card from the given Data. Any current attributes are
		 * replaced by the ones specified in the given data.
		 * <p>
		 * The loaded data is not checked for character limits.
		 */
		@Override
		public void fromData( Data data ) throws TranslationException {
			
			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
			Data titleData = map.get( TITLE_ATTRIBUTE );
			if ( titleData == null ) { // Missing title.
				throw new TranslationException( "Missing title attribute." );
			}
			if ( !titleData.isString() ) {
				throw new TranslationException( "Title attribute is not a String." );
			}
			title = titleData.getString();
			
			Data descriptionData = map.get( DESCRIPTION_ATTRIBUTE );
			if ( descriptionData != null ) { // Has description.
				if ( !descriptionData.isString() ) {
					throw new TranslationException( "Description attribute is not a String." );
				}
				description = descriptionData.getString();
			}
			
			Data urlData = map.get( URL_ATTRIBUTE );
			if ( urlData != null ) { // Has URL.
				if ( !urlData.isString() ) {
					throw new TranslationException( "URL attribute is not a String." );
				}
				url = urlData.getString();
			}
			
			Data footerData = map.get( FOOTER_ATTRIBUTE );
			if ( footerData != null ) { // Has footer.
				if ( !footerData.isString() ) {
					throw new TranslationException( "Footer attribute is not a String." );
				}
				footer = footerData.getString();
			}
			
			Data footerIconData = map.get( FOOTER_ICON_ATTRIBUTE );
			if ( footerIconData != null ) { // Has footer icon.
				if ( !footerIconData.isString() ) {
					throw new TranslationException( "Footer icon attribute is not a String." );
				}
				footerIcon = footerIconData.getString();
			}
			
			Data imageData = map.get( IMAGE_ATTRIBUTE );
			if ( imageData != null ) { // Has image.
				if ( !imageData.isString() ) {
					throw new TranslationException( "Image attribute is not a String." );
				}
				image = imageData.getString();
			}
			
			Data thumbnailData = map.get( THUMBNAIL_ATTRIBUTE );
			if ( thumbnailData != null ) { // Has thumbnail.
				if ( !thumbnailData.isString() ) {
					throw new TranslationException( "Thumbnail attribute is not a String." );
				}
				thumbnail = thumbnailData.getString();
			}
			
			Data authorData = map.get( AUTHOR_ATTRIBUTE );
			if ( authorData != null ) { // Has author.
				if ( !authorData.isString() ) {
					throw new TranslationException( "Author attribute is not a String." );
				}
				author = authorData.getString();
			}
			
			Data authorUrlData = map.get( AUTHOR_URL_ATTRIBUTE );
			if ( authorUrlData != null ) { // Has author URL.
				if ( !authorUrlData.isString() ) {
					throw new TranslationException( "Author URL attribute is not a String." );
				}
				authorURL = authorUrlData.getString();
			}
			
			Data authorIconData = map.get( AUTHOR_ICON_ATTRIBUTE );
			if ( authorIconData != null ) { // Has author icon.
				if ( !authorIconData.isString() ) {
					throw new TranslationException( "Author icon attribute is not a String." );
				}
				authorIcon = authorIconData.getString();
			}
			
			Data fieldsData = map.get( FIELDS_ATTRIBUTE );
			if ( fieldsData != null ) { // Has fields.
				fields.clear(); // Delete any current fields.
				fields.putAll( FIELDS_TRANSLATOR.fromData( fieldsData ) );
			}
			
		}
		
	} // End of class Card.
	
	/**
	 * An entry that represents a card.
	 * 
	 * @author ThiagoTGM
	 * @version 1.0
	 * @since 2018-09-09
	 */
	public static class CardEntry implements Storable {
		
		private static final String TITLE_ATTRIBUTE = "title";
		private static final String FIELD_COUNT_ATTRIBUTE = "fieldCount";
		
		private String title;
		private int fieldCount;
		
		/**
		 * Initializes a blank entry.
		 */
		private CardEntry() {
			
			// No initialization.
			
		}
		
		/**
		 * Initializes an entry with the given title.
		 * 
		 * @param title The title.
		 */
		private CardEntry( String title ) {
			
			this.title = title;
			
		}
		
		/**
		 * Initializes an entry for the given card.
		 * 
		 * @param card The card to make an entry for.
		 */
		public CardEntry( Card card ) {
			
			this( card.title );
			fieldCount = card.getFieldCount();
			
		}
		
		/**
		 * Retrieves the title of the card that this entry represents.
		 * 
		 * @return The card title.
		 */
		public String getTitle() {
			
			return title;
			
		}
		
		/**
		 * Retrieves the amount of fields in the card that this entry represents.
		 * 
		 * @return The amount of fields.
		 */
		public int getFieldCount() {
			
			return fieldCount;
			
		}

		@Override
		public Data toData() {

			Map<String,Data> map = new HashMap<>();
			map.put( TITLE_ATTRIBUTE, Data.stringData( title ) );
			map.put( FIELD_COUNT_ATTRIBUTE, Data.numberData( fieldCount ) );
			return Data.mapData( map );
			
		}

		@Override
		public void fromData( Data data ) throws TranslationException {

			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
			Data titleData = map.get( TITLE_ATTRIBUTE );
			if ( titleData == null ) {
				throw new TranslationException( "Missing title data." );
			}
			if ( !titleData.isString() ) {
				throw new TranslationException( "Title data is not a string." );
			}
			title = titleData.getString();
			
			Data fieldCountData = map.get( FIELD_COUNT_ATTRIBUTE );
			if ( fieldCountData == null ) {
				throw new TranslationException( "Missing field count data." );
			}
			if ( !fieldCountData.isNumber() ) {
				throw new TranslationException( "Field count data is not a number." );
			}
			fieldCount = (int) fieldCountData.getNumberInteger();
			
		}
		
		/**
		 * Matches an entry with the same title. Having a different entry count
		 * does not affect the match.
		 */
		@Override
		public boolean equals( Object o ) {
			
			if ( !( o instanceof CardEntry ) ) {
				return false; // Wrong type.
			}
			CardEntry other = (CardEntry) o;
			
			return title.equals( other.title );
			
		}
		
		@Override
		public int hashCode() {
			
			return title.hashCode();
			
		}
		
	} // End of class CardEntry.
	
	/**
	 * Represents a user's data in the card system.
	 * 
	 * @author ThiagoTGM
	 * @version 1.0
	 * @since 2018-09-09
	 */
	public static class UserCards implements Storable {
		
		/**
		 * How many custom cards a user can have initially.
		 */
		public static final int STARTING_CARDS = 1;
		/**
		 * The maximum amount of custom cards that a user can have.
		 */
		public static final int MAX_CARDS = 5;
		/**
		 * How much currency a user must spend to expand the card allowance by 1.
		 */
		public static final long EXTRA_CARD_COST = 5000L;
		
		private static final Translator<List<CardEntry>> CARDS_TRANSLATOR =
				new ListTranslator<>( new StorableTranslator<>( () -> new CardEntry() ) );
		
		private int cardAllowance;
		private final List<CardEntry> cards;
		
		/**
		 * Creates an instance with no cards and the 
		 * {@link #STARTING_CARDS initial allowance} of cards.
		 */
		private UserCards() {
			
			cardAllowance = STARTING_CARDS;
			cards = new LinkedList<>();
			
		}
		
		/**
		 * Retrieves the user's card allowance (how many cards the user may have at
		 * this time).
		 * 
		 * @return The card allowance.
		 */
		public int getCardAllowance() {
			
			return cardAllowance;
			
		}
		
		/**
		 * Check if the user can still have his/her card allowance increased.
		 * 
		 * @return <tt>true</tt> if can still increase.
		 *         <tt>false</tt> if the user already has the maximum allowance.
		 */
		public boolean canIncrementCardAllowance() {
			
			return cardAllowance < MAX_CARDS;
			
		}
		
		/**
		 * Increments the card allowance by 1, if the user does not yet have the
		 * {@link #MAX_CARDS maximum allowance}.
		 * 
		 * @return <tt>true</tt> if incremented successfully.
		 *         <tt>false</tt> if the user already has the maximum allowance.
		 */
		private boolean incrementCardAllowance() {
			
			if ( canIncrementCardAllowance() ) {
				cardAllowance++;
				return true;
			} else {
				return false;
			}
			
		}
		
		/**
		 * Retrieves the number of cards that the user currently has.
		 * 
		 * @return The card count.
		 */
		public int getCardCount() {
			
			return cards.size();
			
		}
		
		/**
		 * Retrieves the user's cards.
		 * 
		 * @return The cards.
		 */
		public List<CardEntry> getCards() {
			
			return new ArrayList<>( cards );
			
		}
		
		/**
		 * Determines whether this user has a card with the given title.
		 * 
		 * @param title The title of the card.
		 * @return <tt>true</tt> if the user has a card with the given title.
		 *         <tt>false</tt> otherwise.
		 */
		public boolean hasCard( String title ) {
			
			return cards.contains( new CardEntry( title ) );
			
		}
		
		/**
		 * Determines whether this user has the given card (as in a
		 * card with the same title).
		 * 
		 * @param card The card.
		 * @return <tt>true</tt> if the user has the given card.
		 *         <tt>false</tt> otherwise.
		 */
		public boolean hasCard( Card card ) {
			
			return cards.contains( new CardEntry( card ) );
			
		}
		
		/**
		 * Registers a new card for this user, if the user has not yet
		 * reached his card allowance.
		 * 
		 * @param card The card to add.
		 * @return <tt>true</tt> if added successfully.
		 *         <tt>false</tt> if the user is already using his full
		 *         card allowance.
		 * @throws IllegalArgumentException if the user already has a card with the same
		 *                                  title. The exception's detail message will
		 *                                  indicate this.
		 */
		private boolean addCard( Card card ) throws IllegalArgumentException {
			
			CardEntry c = new CardEntry( card );
			if ( cards.contains( c ) ) {
				throw new IllegalArgumentException( "A card with this title already exists!" );
			}
			
			if ( getCardCount() < cardAllowance ) {
				cards.add( c );
				return true;
			} else {
				return false; // Already have filled allowance.
			}
			
		}
		
		/**
		 * Deregisters the given card from this user's list (as in a
		 * card with the same title).
		 * 
		 * @param card The card to remove.
		 * @return <tt>true</tt> if removed successfully.
		 *         <tt>false</tt> if the user does not have the given
		 *         card.
		 */
		private boolean removeCard( Card card ) {
			
			return cards.remove( new CardEntry( card ) );
			
		}
		
		/**
		 * Updates the registration of the given card in this user's
		 * list (e.g. updates the field count of the card with the same
		 * title).
		 * 
		 * @param card The card to update.
		 * @return <tt>true</tt> if updated successfully.
		 *         <tt>false</tt> if the user does not have the given
		 *         card.
		 */
		private boolean updateCard( Card card ) {
			
			CardEntry entry = new CardEntry( card );
			return cards.remove( entry ) && cards.add( entry );
			
		}
		
		private static final String CARD_ALLOWANCE_ATTRIBUTE = "cardAllowance";
		private static final String CARDS_ATTRIBUTE = "cards";

		@Override
		public Data toData() {

			Map<String,Data> map = new HashMap<>();
			map.put( CARD_ALLOWANCE_ATTRIBUTE, Data.numberData( cardAllowance ) );
			map.put( CARDS_ATTRIBUTE, CARDS_TRANSLATOR.toData( cards ) );
			return Data.mapData( map );
			
		}

		@Override
		public void fromData( Data data ) throws TranslationException {

			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
			Data allowanceData = map.get( CARD_ALLOWANCE_ATTRIBUTE );
			if ( allowanceData == null ) {
				throw new TranslationException( "Missing card allowance attribute." );
			}
			if ( !allowanceData.isNumber() ) {
				throw new TranslationException( "Card allowance attribute is not a number." );
			}
			cardAllowance = (int) allowanceData.getNumberInteger();
			
			Data cardsData = map.get( CARDS_ATTRIBUTE );
			if ( cardsData == null ) {
				throw new TranslationException( "Missing cards attribute." );
			}
			List<CardEntry> cards = CARDS_TRANSLATOR.fromData( cardsData );
			this.cards.clear();
			this.cards.addAll( cards );
			
		}
		
	}

}
