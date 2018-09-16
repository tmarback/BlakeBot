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

package com.github.thiagotgm.blakebot.module.info;

import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.thiagotgm.bot_utils.Settings;

/**
 * Class that processes information text from an input stream.
 * <p>
 * The inputs given to {@link #process(InputStream)} are expected to be encoded using
 * {@value #ENCODING}.
 * <p>
 * Any placeholders present in the input are replaced with their values.<br>
 * Supported placeholders are:
 * 
 * <ul>
 *  <li><tt>$[setting]</tt> - The value of the {@link Settings setting} named <tt>setting</tt>.
 * </ul>
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-16
 */
public class InfoProcessor {
    
    /**
     * Encoding that info files are expected to use.
     */
    public static final String ENCODING = "UTF-8";
    
    private static final Pattern SETTING_PLACEHOLDER = Pattern.compile( "\\$\\[(.*?)]" );
    private static final Function<Matcher,String> SETTING_REPLACER = matcher -> {
        
        String setting = matcher.group( 1 );
        return Settings.hasSetting( setting ) ? Settings.getStringSetting( setting ) : "";
        
    };
    
    /**
     * Processes an input into a string, replacing supported placeholders.
     * 
     * @param input The input to be processed.
     * @return The processed content of the input.
     */
    public static String process( InputStream input ) {
        
        Scanner scan = new Scanner( input, ENCODING );
        scan.useDelimiter( "\\A" ); // Get file blocks.
        String content = scan.next().trim();
        scan.close();
        
        content = process( content, SETTING_PLACEHOLDER, SETTING_REPLACER );
        
        return content;
        
    }
    
    /**
     * Processes the content in a string, using the given pattern to identify what needs to
     * be replaced and replacing it with the result of the given replace operation.
     * 
     * @param content The content to be processed.
     * @param pattern The pattern that identifies what needs to be replaced.
     * @param replaceOp The operation that gives the replacement for each pattern match.<br>
     *                  The matcher argument is guaranteed to have just performed a successful
     *                  <tt>find()</tt> operation.
     * @return The processed content string.
     */
    private static String process( String content, Pattern pattern,
            Function<Matcher,String> replaceOp ) {
        
        Matcher matcher = pattern.matcher( content );
        StringBuilder processed = new StringBuilder();
        int firstUnmatched = 0;
        while ( matcher.find() ) { // Replace each match.
            // Append content between last match and this one, if any.
            processed.append( content.substring( firstUnmatched, matcher.start() ) );
            processed.append( replaceOp.apply( matcher ) ); // Apply replace op.
            firstUnmatched = matcher.end();
            
        } // Append what is after last match.
        processed.append( content.substring( firstUnmatched ) );
        return processed.toString();
        
    }

}
