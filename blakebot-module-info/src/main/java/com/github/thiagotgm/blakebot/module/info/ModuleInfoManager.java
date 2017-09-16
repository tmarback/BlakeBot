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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that aggregates information on installed modules.
 * <p>
 * Module information is obtained by reading all resource files named
 * {@value #FILE_NAME} in root resource directories (eg the root folder of a jar).
 * This implies that only one per jar-based module can be provided.
 * <p>
 * Info files are expected to follow the following format:
 * 
 * <ul>
 *  <li>Files are split in <i>blocks</i>. Blocks are separated by a line containing
 *      only the {@value #BLOCK_DELIMITER} delimiter. Trailing blank space in a
 *      block is removed, but leading space is not.</li>
 *  <li>The first block is the <i>header</i>. It should contain the following basic
 *      module information, one per line (in this order):
 *      <ul>
 *       <li>The alias that module information should be accessed with;</li>
 *       <li>The name of the module;</li>
 *       <li>The version of the module;</li>
 *       <li>(optional) The syntax highlighting that should be used to display the
 *           info blocks (described later) using Discord's code block syntax
 *           highlighting.</li>
 *      </ul></li>
 *  <li>The second block is the <i>description</i>. It should shortly describe what
 *      the module does, and cannot be longer than {@link ModuleInfo#MAX_DESCRIPTION_LENGTH}
 *      characters. It is displayed with the alias and version in the module list.</li>
 *  <li>Any further blocks are <i>information blocks</i>. They are displayed along with
 *      the header info and description when information on a specific module is
 *      requested. Each block must fit in a single Discord message.</li>
 * </ul>
 * <p>
 * The following is an example of properly formatted info file:
 * 
 * <pre>
 * FOO
 * Module Foo
 * 1.2.0
 * md
 * [?block?]
 * This module does things.
 * [?block?]
 * # [Commands]
 * 
 * * **!bar**
 *   Executes a command.
 * </pre>
 * 
 * <b>OBS about info blocks:</b> The message sent that contains the block will
 * automatically include the characters for a code block
 * (<tt>```(syntax)\n(block)\n```</tt>, where <tt>(syntax)</tt> is the specified
 * syntax highlighting, if any, and <tt>(block)</tt> is the info block).<br>
 * This implies that:
 * <ul>
 *  <li>The length of the block must be sufficiently smaller than the maximum
 *      message length to account for the overhead characters;</li>
 *  <li>It is possible to change the syntax highlighting mid-block by ending the
 *      code block and starting and starting a new one
 *      (<tt>\n```\n```(new syntax)\n</tt>). They will, however, be displayed by
 *      Discord as separate code blocks.
 * </ul>
 * <p>
 * (<tt>\n</tt> = line break)
 * 
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-15
 */
public class ModuleInfoManager {
    
    private static final Logger LOG = LoggerFactory.getLogger( ModuleInfoManager.class );
    
    /**
     * Encoding that the info files are expected to use.
     */
    public static final String ENCODING = "UTF-8";
    
    /**
     * Expected name of info files.
     */
    public static final String FILE_NAME = "module.info";
    
    /**
     * Delimiter that separates blocks in the info file.
     */
    public static final String BLOCK_DELIMITER = "[?block?]";
    private static final Pattern DELIMITER =
            Pattern.compile( String.format( "\\s*\n\\s*%s\\s*\n",
                    Pattern.quote( BLOCK_DELIMITER ) ) );
    
    private static final Map<String,ModuleInfo> INFOS;
    
    static {
        
        LOG.info( "Loading module info files." );
        
        Map<String,ModuleInfo> infos = new HashMap<>();
        Enumeration<URL> files;
        try {
            files = ModuleInfoManager.class.getClassLoader()
                    .getResources( FILE_NAME );
            while ( files.hasMoreElements() ) {
                
                try {
                    URL next = files.nextElement();
                    LOG.info( "Parsing module info file {}.", next );
                    ModuleInfo info = parseInfo( next.openStream() );
                    infos.put( info.getAlias(), info );
                } catch ( IOException e ) {
                    LOG.error( "Could not read module info file.", e );
                } catch ( InfoFormatException e ) {
                    LOG.error( "Could not parse module info file.", e );
                }
                
            }
            LOG.info( "Finished loading module info files." );
        } catch ( IOException e ) {
            LOG.error( "Could not retrieve module info files.", e );
        }
        
        INFOS = Collections.unmodifiableMap( infos );
        
    }
    
    /**
     * Parses an info file.
     * 
     * @param input The stream to read the file from.
     * @return The module information in the file.
     * @throws InfoFormatException if the file does not match the expected format.
     */
    private static ModuleInfo parseInfo( InputStream input ) throws InfoFormatException {
        
        Scanner scan = new Scanner( input, ENCODING );
        scan.useDelimiter( "\\A" ); // Get file blocks.
        String[] blocks = DELIMITER.split( scan.next().trim() );
        scan.close();
        
        if ( blocks.length < 1 ) {
            throw new InfoFormatException( "Missing header." );
        }
        if ( blocks.length < 2 ) {
            throw new InfoFormatException( "Missing description." );
        }
        
        String[] headerInfo = parseHeader( blocks[0] );
        String description = blocks[1];
        String[] info = Arrays.copyOfRange( blocks, 2, blocks.length );
        
        try {
            return new ModuleInfo( headerInfo[0], headerInfo[1], headerInfo[2],
                             headerInfo[3], description, info );
        } catch ( IllegalArgumentException e ) {
            throw new InfoFormatException( "Invalid info file.", e );
        }
        
    }
    
    /**
     * Parses the header of an info file.<br>
     * The returned array contains the following elements:
     * <p>
     * <ul>
     *  <li>0 - The alias</li>
     *  <li>1 - The name</li>
     *  <li>2 - The version</li>
     *  <li>3 - The syntax highlighting (may be null)</li>
     * </ul>
     * 
     * @param header The header to be parsed. 
     * @return The information in the header.
     * @throws InfoFormatException if the header does not match the expected format.
     */
    private static String[] parseHeader( String header ) throws InfoFormatException {
        
        Scanner scan = new Scanner( header );
        try {
            
            String[] info = new String[4];
            
            if ( !scan.hasNextLine() ) {
                throw new InfoFormatException( "Missing alias." );
            }
            info[0] = scan.nextLine().trim(); // Get alias.
            if ( info[0].isEmpty() ) {
                throw new InfoFormatException( "Alias cannot be blank." );
            }
            
            if ( !scan.hasNextLine() ) {
                throw new InfoFormatException( "Missing name." );
            }
            info[1] = scan.nextLine().trim(); // Get name.
            if ( info[1].isEmpty() ) {
                throw new InfoFormatException( "Name cannot be blank." );
            }
            
            if ( !scan.hasNextLine() ) {
                throw new InfoFormatException( "Missing version." );
            }
            info[2] = scan.nextLine().trim(); // Get version.
            if ( info[2].isEmpty() ) {
                throw new InfoFormatException( "Version cannot be blank." );
            }
            
            if ( scan.hasNextLine() ) { // Highlight line present.
                info[3] = scan.nextLine().trim(); // Get highlight.
                if ( info[3].isEmpty() ) {
                    throw new InfoFormatException( "Syntax highlighting cannot be blank." );
                }
            }
            return info;
        
        } finally {
            
            scan.close();
            
        }
        
    }
    
    /**
     * Retrieves the module information stored under the given alias.
     * 
     * @param alias The alias to get info for.
     * @return The information for that alias, or <tt>null</tt> if there is no
     *         information registered for that alias.
     */
    public static ModuleInfo getInfo( String alias ) {
        
        return INFOS.get( alias );
        
    }
    
    /**
     * Retrieves all the module information instances stored in this manager.
     * 
     * @return The stored information.
     */
    public static Collection<ModuleInfo> getInfos() {
        
        return INFOS.values();
        
    }
    
    /**
     * An exception that indicates that an info file found did not follow the
     * appropriate format.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-15
     */
    public static class InfoFormatException extends Exception {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -7539693611116470135L;

        /**
         * Constructs an exception with no detail message.
         */
        protected InfoFormatException() {
            
            super();
            
        }
        
        /**
         * Constructs an exception with the given detail message.
         * 
         * @param message The detail message.
         */
        protected InfoFormatException( String message ) {
            
            super( message );
            
        }
        
        /**
         * Constructs a new exception with the specified cause and a
         * detail message of <tt>(cause==null ? null : cause.toString())</tt>.
         * 
         * @param cause The cause.
         */
        protected InfoFormatException( Throwable cause ) {
            
            super( cause );
            
        }
        
        /**
         * Constructs a new exception with the specified detail message and cause. 
         * 
         * @param message The detail message.
         * @param cause The cause.
         */
        protected InfoFormatException( String message, Throwable cause ) {
            
            super( message, cause );
            
        }
        
    }

}
