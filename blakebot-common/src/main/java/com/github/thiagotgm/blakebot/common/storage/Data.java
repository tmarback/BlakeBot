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

package com.github.thiagotgm.blakebot.common.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.thiagotgm.blakebot.common.storage.translate.DataTranslator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Data that can be stored in a backend database.
 * <p>
 * An instance of this class will only contain one type of data.
 * The is* methods should be used to determine the appropriate type.<br>
 * Calling a get* method that does not match the internal data type will obtain <tt>null</tt>
 * if the method returns an object (so these methods may be used to check for <tt>null</tt> values
 * if the type is known beforehand), but primitive-returning versions will have undefined behavior
 * and may throw an exception.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-29
 */
public class Data {
	
	/**
	 * Types that an instance of Data can store.
	 *
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-30
	 */
	public enum Type { 
		
		/**
		 * A String literal.
		 */
		STRING,
		
		/**
		 * A numerical value (can be obtained as either a {@link Data#getNumber() String},
		 * {@link Data#getNumberInteger() integer}, or {@link Data#getNumberInteger() float}).
		 */
		NUMBER,
		
		/**
		 * A boolean value.
		 */
		BOOLEAN,
		
		/**
		 * The value <tt>null</tt>.
		 */
		NULL,
		
		/**
		 * A list of Data.
		 */
		LIST,
		
		/**
		 * A map of Strings to Data.
		 */
		MAP
		
	};
	
	private final String string;
	private final String number;
	private final boolean bool;
	private final List<Data> list;
	private final Map<String,Data> map;
	private final Type type;
	
	/**
	 * Creates a new instance.
	 * <p>
	 * Exactly one of the arguments must be filled (<tt>true</tt> or non-<tt>null</tt>). The
	 * remaining arguments must be either <tt>false</tt> or <tt>null</tt>. If no arguments
	 * are filled or more than one is, an exception is thrown.<br>
	 * (<tt>bool</tt> is an exception to this, as it only provides the boolean value in the case
	 * that <tt>isBool</tt> is <tt>true</tt>)
	 * 
	 * @param string String data.
	 * @param number Number data.
	 * @param bool Boolean data.
	 * @param isBool Whether this data is boolean data.
	 * @param NULL Whether this data is the value NULL.
	 * @param list List data.
	 * @param map Map data.
	 * @throws IllegalArgumentException if none of the arguments is filled, or more than one is.
	 * @throws NullPointerException if a list or map is given that contains the value <tt>null</tt>
	 *                              (as either a key or value, in the case of a map).
	 */
	private Data( String string, String number, boolean bool, boolean isBool,
			boolean NULL, List<Data> list, Map<String,Data> map )
					throws IllegalArgumentException, NullPointerException {
		
		EnumSet<Type> types = EnumSet.noneOf( Type.class ); // Determine data type.
		if ( string != null ) {
			types.add( Type.STRING );
		}
		if ( number != null ) {
			types.add( Type.NUMBER );
		}
		if ( isBool ) {
			types.add( Type.BOOLEAN );
		}
		if ( NULL ) {
			types.add( Type.NULL );
		}
		if ( list != null ) {
			types.add( Type.LIST );
		}
		if ( map != null ) {
			types.add( Type.MAP );
		}
		if ( types.isEmpty() ) { // Check exactly one type.
			throw new IllegalArgumentException( "Missing data value." );
		} else if ( types.size() > 1 ) {
			throw new IllegalArgumentException( "Multiple data values." );
		}
		
		this.type = types.iterator().next();
		this.string = string;
		this.number = number;
		this.bool = bool;
		if ( list == null ) {
			this.list = null;
		} else {
			this.list = Collections.unmodifiableList( new ArrayList<>( list ) );
			if ( this.list.contains( null ) ) { // Ensure no null elements.
				throw new NullPointerException( "Given list contains null." );
			}
		}
		if ( map == null ) {
			this.map = null;
		} else {
			this.map = Collections.unmodifiableMap( new HashMap<>( map ) );
			if ( this.map.containsKey( null ) ) { // Ensure no null keys.
				throw new NullPointerException( "Given map contains null key." );
			}
			if ( this.map.containsValue( null ) ) { // Ensure no null values.
				throw new NullPointerException( "Given map contains null value." );
			}
		}
		
	}
	
	/**
	 * Retrieves the type of data that is stored in this instance.
	 * 
	 * @return The data type.
	 */
	public Type getType() {
		
		return type;
		
	}
	
	/**
	 * Determines whether this data is a String.
	 * 
	 * @return Whether this data is a String.
	 */
	public boolean isString() {
		
		return type == Type.STRING;
		
	}
	
	/**
	 * Retrieves the data if this is a String.
	 * 
	 * @return If this is String data, the data, <tt>null</tt> otherwise.
	 */
	public String getString() {
		
		return string;
		
	}
	
	/**
	 * Determines whether this data is a number.
	 * 
	 * @return Whether this data is a number.
	 */
	public boolean isNumber() {
		
		return type == Type.NUMBER;
		
	}
	
	/**
	 * Determines whether this data is a <i>floating-point</i> number.
	 * 
	 * @return Whether this data is a <i>floating-point</i> number.
	 */
	public boolean isFloat() {
		
		return isNumber() && ( number.contains( "." ) ||
				               number.equals( String.valueOf( Double.NaN ) ) ||
				               number.equals( String.valueOf( Double.NEGATIVE_INFINITY ) ) ||
				               number.equals( String.valueOf( Double.POSITIVE_INFINITY ) ) );
		
	}
	
	/**
	 * Retrieves the data if this is a number.
	 * 
	 * @return If this is number data, the data, <tt>null</tt> otherwise.
	 */
	public String getNumber() {
		
		return number;
		
	}
	
	/**
	 * Retrieves the data if this is a number, as a floating-point number.
	 * 
	 * @return If this is number data, the data, 0 otherwise.
	 */
	public double getNumberFloat() {
		
		return isNumber() ? Double.parseDouble( number ) : 0;
		
	}
	
	/**
	 * Retrieves the data if this is a number, as an integer number.
	 * <p>
	 * If the number data is actually a decimal number, the return of this
	 * method is the truncated number.
	 * 
	 * @return If this is number data, the data, 0 otherwise.
	 */
	public long getNumberInteger() {
		
		return isNumber() ? ( isFloat() ? (long) getNumberFloat() : Long.parseLong( number ) ) : 0;
		
	}
	
	/**
	 * Determines whether this data is a boolean.
	 * 
	 * @return Whether this data is a boolean.
	 */
	public boolean isBoolean() {
		
		return type == Type.BOOLEAN;
		
	}
	
	/**
	 * Retrieves the data if this is a boolean.
	 * 
	 * @return If this is boolean data, the data, otherwise it is
	 *         undefined.
	 */
	public boolean getBoolean() {
		
		return bool;
		
	}
	
	/**
	 * Determines whether this data is <tt>null</tt>.
	 * 
	 * @return Whether this data is <tt>null</tt>.
	 */
	public boolean isNull() {
		
		return type == Type.NULL;
		
	}
	
	/**
	 * Retrieves the data if this is <tt>null</tt>.
	 * 
	 * @return If this is <tt>null</tt> data, the data, <tt>false</tt> otherwise.
	 */
	public boolean getNull() {
		
		return isNull();
		
	}
	
	/**
	 * Determines whether this data is a list.
	 * 
	 * @return Whether this data is a list.
	 */
	public boolean isList() {
		
		return type == Type.LIST;
		
	}
	
	/**
	 * Retrieves the data if this is a List.
	 * <p>
	 * The List returned, if any, is unmodifiable.
	 * 
	 * @return If this is List data, the data, <tt>null</tt> otherwise.
	 */
	public List<Data> getList() {
		
		return list;
		
	}
	
	/**
	 * Determines whether this data is a map.
	 * 
	 * @return Whether this data is a map.
	 */
	public boolean isMap() {
		
		return type == Type.MAP;
		
	}
	
	/**
	 * Retrieves the data if this is a Map.
	 * <p>
	 * The Map returned, if any, is unmodifiable.
	 * 
	 * @return If this is Map data, the data, <tt>null</tt> otherwise.
	 */
	public Map<String,Data> getMap() {
		
		return map;
		
	}
	
	@Override
	public boolean equals( Object o ) {
		
		if ( !( o instanceof Data ) ) {
			return false;
		}
		
		Data other = (Data) o;
		if ( other.getType() != getType() ) {
			return false;
		}
		
		switch ( getType() ) {
		
			case STRING:
				return getString().equals( other.getString() );
				
			case NUMBER:
				return getNumber().equals( other.getNumber() );
				
			case BOOLEAN:
				return getBoolean() == other.getBoolean();
				
			case NULL:
				return true;
				
			case LIST:
				return getList().equals( other.getList() );
				
			case MAP:
				return getMap().equals( other.getMap() );
		
		}
		
		return false; // Will never get here.
		
	}
	
	@Override
	public int hashCode() {
		
		switch ( getType() ) {
		
			case STRING:
				return getString().hashCode();
				
			case NUMBER:
				return getNumber().hashCode();
				
			case BOOLEAN:
				return getBoolean() ? 1 : 0;
				
			default:
			case NULL:
				return 0;
				
			case LIST:
				return getList().hashCode();
				
			case MAP:
				return getMap().hashCode();
		
		}
		
	}
	
	@Override
	public String toString() {
		
		return new DataTranslator().encode( this );
		
	}
	
	/**
	 * Creates a String-valued instance.
	 * <p>
	 * If the given string is <tt>null</tt>, the returned instance will
	 * be of type {@link Type#NULL NULL}.
	 * 
	 * @param string The value.
	 * @return The instance with the given value.
	 */
	public static Data stringData( String string ) {
		
		if ( string == null ) {
			return nullData();
		} else {
			return new Data( string, null, false, false, false, null, null );
		}
		
	}
	
	/**
	 * Creates a number-valued instance.
	 * <p>
	 * If the given number is <tt>null</tt>, the returned instance will
	 * be of type {@link Type#NULL NULL}.
	 * 
	 * @param number The value.
	 * @return The instance with the given value.
	 * @throws NumberFormatException if the string given is not a valid number.
	 */
	public static Data numberData( String number ) throws NumberFormatException {
		
		if ( number == null ) {
			return nullData();
		} else {
			Double.valueOf( number ); // Check valid string.
			return new Data( null, number, false, false, false, null, null );
		}
		
	}
	
	/**
	 * Creates a number-valued instance.
	 * 
	 * @param number The value.
	 * @return The instance with the given value.
	 */
	public static Data numberData( double number ) {
		
		return numberData( String.valueOf( number ) );
		
	}
	
	/**
	 * Creates a number-valued instance.
	 * 
	 * @param number The value.
	 * @return The instance with the given value.
	 */
	public static Data numberData( long number ) {
		
		return numberData( String.valueOf( number ) );
		
	}
	
	/**
	 * Creates a boolean-valued instance.
	 * 
	 * @param bool The value.
	 * @return The instance with the given value.
	 */
	public static Data booleanData( boolean bool ) {
		
		return new Data( null, null, bool, true, false, null, null );
		
	}
	
	/**
	 * Creates a <tt>null</tt>-valued instance.
	 * 
	 * @return The instance with <tt>null</tt> value.
	 */
	public static Data nullData() {
		
		return new Data( null, null, false, false, true, null, null );
		
	}
	
	/**
	 * Creates a List-valued instance, with the value being the list
	 * made of the given elements in the given order.
	 * 
	 * @param elements The list elements.
	 * @return The instance with the given value.
	 */
	public static Data listData( Data... elements ) {
		
		return listData( Arrays.asList( elements ) );
		
	}
	
	/**
	 * Creates a List-valued instance.
	 * <p>
	 * If the given list is <tt>null</tt>, the returned instance will
	 * be of type {@link Type#NULL NULL}.
	 * 
	 * @param list The value.
	 * @return The instance with the given value.
	 * @throws NullPointerException if the given list contains <tt>null</tt> (in order
	 *                              to represent a <tt>null</tt> element, use a
	 *                              {@link #nullData() NULL-valued} Data).
	 */
	public static Data listData( List<Data> list ) throws NullPointerException {
		
		if ( list == null ) {
			return nullData();
		} else {
			return new Data( null, null, false, false, false, list, null );
		}
		
	}
	
	/**
	 * Creates a Map-valued instance.
	 * <p>
	 * If the given map is <tt>null</tt>, the returned instance will
	 * be of type {@link Type#NULL NULL}.
	 * 
	 * @param map The value.
	 * @return The instance with the given value.
	 * @throws NullPointerException if the given map contains a <tt>null</tt> key or value (in order
	 *                              to represent a <tt>null</tt> value, use a 
	 *                              {@link #nullData() NULL-valued} Data).
	 */
	public static Data mapData( Map<String,Data> map ) throws NullPointerException {
		
		if ( map == null ) {
			return nullData();
		} else {
			return new Data( null, null, false, false, false, null, map );
		}
		
	}
	
	/**
	 * Adapter that converts Data instances to JSON format.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-29
	 */
	public static class DataAdapter extends TypeAdapter<Data> {

		@Override
		public Data read( JsonReader in ) throws IOException {

			switch ( in.peek() ) {
			
				case STRING:
					return stringData( in.nextString() );
					
				case NUMBER:
					return numberData( in.nextString() );
					
				case BOOLEAN:
					return booleanData( in.nextBoolean() );
					
				case NULL:
					in.nextNull(); // Consume anyway.
					return nullData();
					
				case BEGIN_ARRAY: // List data.
					in.beginArray();
					
					List<Data> list = new LinkedList<>();
					while ( in.hasNext() ) {
						
						list.add( read( in ) ); // Read each element.
						
					}
					
					in.endArray();
					return listData( list );
					
				case BEGIN_OBJECT: // Map data.
					in.beginObject();
					
					Map<String,Data> map = new HashMap<>();
					while ( in.hasNext() ) {
						
						String key = in.nextName(); // Read each mapping.
						map.put( key, read( in ) );
						
					}
					
					in.endObject();
					return mapData( map );
					
				default:
					throw new IOException( "Unexpected element." );
			
			}
			
		}

		@Override
		public void write( JsonWriter out, Data value ) throws IOException {

			switch ( value.getType() ) {
			
				case STRING:
					out.value( value.getString() );
					break;
					
				case NUMBER:
					if ( value.isFloat() ) {
						out.value( value.getNumberFloat() );
					} else {
						out.value( value.getNumberInteger() );
					}
					break;
					
				case BOOLEAN:
					out.value( value.getBoolean() );
					break;
					
				case NULL:
					out.nullValue();
					break;
					
				case LIST: // Data is a list.
					out.beginArray();
					
					for ( Data elem : value.getList() ) {
						
						write( out, elem ); // Write each element.
						
					}
					
					out.endArray();
					break;
					
				case MAP: // Data is a map.
					out.beginObject();
					
					for ( Map.Entry<String,Data> entry : value.getMap().entrySet() ) {
						
						out.name( entry.getKey() ); // Write each mapping.
						write( out, entry.getValue() );
						
					}
					
					out.endObject();
					break;
					
				default:
					throw new IOException( "Invalid data type." );
					
			}
			
		}
		
	}

}
