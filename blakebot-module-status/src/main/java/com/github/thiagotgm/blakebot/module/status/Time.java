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

package com.github.thiagotgm.blakebot.module.status;

import java.util.concurrent.TimeUnit;

/**
 * Class that encapsulates an amount of time.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-01-31
 */
public class Time {
    
    private final long time;
    
    private boolean trimZeroes;
    private boolean includeSeconds;
    private boolean includeMillis;
    
    private static final int DAYS = 0;
    private static final int HOURS = 1;
    private static final int MINUTES = 2;
    private static final int SECONDS = 3;
    private static final int MILLIS = 4;
    private static final int FIELDS = 5;
    private static final String[] LABELS_LONG = { " days", " hours",
            " minutes", " seconds ", " milliseconds" };
    private static final String SEPARATOR_LONG = ", ";
    private static final String[] LABELS_SHORT = { "d", "h", "m", "s", "ms" };
    private static final String SEPARATOR_SHORT = " | ";
    
    /**
     * Creates a new instance that represents a certain time amount.
     *
     * @param time The time to be represented by the instance.
     */
    public Time( long time ) {
        
        this.time = time;
        
        this.trimZeroes = false;
        this.includeSeconds = false;
        this.includeMillis = false;
        
    }
    
    /**
     * Creates a new instance that is a copy of a given instance.
     * String conversion settings are preserved.
     *
     * @param o Time object to be copied.
     */
    public Time( Time o ) {
        
        this.time = o.time;
        
        this.trimZeroes = o.trimZeroes;
        this.includeSeconds = o.includeSeconds;
        this.includeMillis = o.includeMillis;
        
    }
    
    /**
     * Parses the time value into days, hours, minutes, seconds, milliseconds.
     * 
     * @return An array with each parsed value, in the order defined above.
     */
    private long[] parseTime() {
        
        long remaining = time;
        long[] parsed = new long[FIELDS];
        
        parsed[DAYS] = TimeUnit.MILLISECONDS.toDays( remaining );
        remaining -= TimeUnit.DAYS.toMillis( parsed[DAYS] );
        parsed[HOURS] = TimeUnit.MILLISECONDS.toHours( remaining );
        remaining -= TimeUnit.HOURS.toMillis( parsed[HOURS] );
        parsed[MINUTES] = TimeUnit.MILLISECONDS.toMinutes( remaining );
        remaining -= TimeUnit.MINUTES.toMillis( parsed[MINUTES] );
        parsed[SECONDS] = TimeUnit.MILLISECONDS.toSeconds( remaining );
        remaining -= TimeUnit.SECONDS.toMillis( parsed[SECONDS] );
        parsed[MILLIS] = remaining;
        
        return parsed;
        
    }
    
    /**
     * Returns the amount of time represented by this in milliseconds.
     * 
     * @return The time represented, in milliseconds.
     */
    public long getTotalTime() {
        
        return time;
        
    }
    
    /**
     * Returns the amount of time represented by this, as an array with parsed
     * values.
     * 
     * @return The parsed time value.<br>
     *         Is returned in the form of an array with 5 values:<p>
     *         index 0 - Days;<br>
     *         index 1 - Hours;<br>
     *         index 2 - Minutes;<br>
     *         index 3 - Seconds;<br>
     *         index 4 - Milliseconds.
     */
    public long[] getTime() {
        
        return parseTime();
        
    }
    
    /**
     * Sets whether to cut off leading zero values when converting to string.
     * 
     * @param trim Whether to trim leading zeroes.
     * @see #toString( boolean )
     */
    public void trimZeroes( boolean trim ) {
        
        this.trimZeroes = trim;
        
    }
    
    /**
     * Sets whether to include seconds in the string value.
     * 
     * @param include Whether to include seconds.
     * @see #toString( boolean )
     */
    public void includeSeconds( boolean include ) {
        
        this.includeSeconds = include;
        
    }
    
    /**
     * Sets whether to include milliseconds in the string value.
     * OBS: If seconds are set to not be included, milliseconds will not
     * be included either.
     * 
     * @param include Whether to include milliseconds.
     * @see #toString( boolean )
     */
    public void includeMillis( boolean include ) {

        this.includeMillis = include;
        
    }
    
    /**
     * Obtains the string that represents this time value, in long format.<br>
     * Same as calling toString( true ).
     *
     * @return The string that represents this time value.
     * @see #toString( boolean )
     */
    public String toString() {
        
        return toString( true );
        
    }
    
    /**
     * Obtains the string that represents this time value.
     * <p>
     * Will use the long (DD days, HH hours, MM minutes, SS seconds,
     * MS milliseconds) or short (DDd | HHh | MMm | SSs | MSms) format depending
     * on the value of the argument received.
     * <p>
     * If includeMillis is set to false, will not include the amount of milliseconds.<br>
     * If includeSeconds is set to false, will not include the amount of seconds or
     * milliseconds.<br>
     * If trimZeroes is set to true, any leftmost unit that has value 0 will be ignored
     * (for example, 0 days 0 hours 20 minutes will be returned as 20 minutes). If it is
     * set to false, all values will be printed. If set to true but all values (that are
     * set to be included are 0, returns a 0 value of the leftmost unit.
     * <p>
     * By default, trimZeroes, includeSeconds, and includeMillis are set to false.
     *
     * @param longVersion If true, uses the long format. Else, uses the short format.
     * @return The string that represents this time value.
     * @see #trimZeroes( boolean )
     * @see #includeSeconds( boolean )
     * @see #includeMillis( boolean )
     */
    public String toString( boolean longVersion ) {
        
        long[] time = parseTime();
        
        // Remove low units if necessary.
        int last;
        if ( !includeSeconds ) {
            last = MINUTES;
        } else if ( !includeMillis ) {
            last = SECONDS;
        } else {
            last = MILLIS;
        }
        
        // Trims leading zeroes if necessary.
        int first = DAYS;
        if ( trimZeroes ) {
            while ( ( first < last ) && ( time[first] == 0 ) ) {
                
                first++;
                
            }
        }
        
        // Chooses long or short format.
        String[] labels;
        String separator;
        if ( longVersion ) {
            labels = LABELS_LONG;
            separator = SEPARATOR_LONG;
        } else {
            labels = LABELS_SHORT;
            separator = SEPARATOR_SHORT;
        }
        
        // Builds the string.
        String value = "";
        for ( int i = first; i < last; i++ ) {
            
            value += time[i] + labels[i] + separator;
            
        }
        value += time[last] + labels[last];
        
        return value;
        
    }

}
