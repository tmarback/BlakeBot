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
import java.util.SortedMap;
import java.util.TreeMap;

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
	public static class Card {
		
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
		private SortedMap<String,String> fields;
		
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
		
	}

}
