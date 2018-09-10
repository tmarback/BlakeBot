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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Storable;
import com.github.thiagotgm.blakebot.common.storage.Translator;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;
import com.github.thiagotgm.blakebot.common.storage.translate.MapTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Manages the custom card system.
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2018-09-08
 */
public class CardManager {
	
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
			
			setTitle( title );
			this.fields = new TreeMap<>();
			
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
		 * Stores this card into a Data. The title is not included in the data.
		 */
		@Override
		public Data toData() {
			
			Map<String,Data> map = new HashMap<>();
			
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
		 * replaced by the ones specified in the given data. The exception
		 * is the title, which is not loaded from the data.
		 */
		@Override
		public void fromData( Data data ) throws TranslationException {
			
			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
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
		
	}

}
