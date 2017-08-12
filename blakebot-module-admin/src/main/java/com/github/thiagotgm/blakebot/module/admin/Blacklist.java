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

package com.github.thiagotgm.blakebot.module.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Class that keeps record of the blacklist for all servers.<br>
 * Uses a Singleton pattern.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class Blacklist {
    
    private static final String FILENAME = "Blacklist.xml";
    private static final String FILEPATH = Paths.get( "data" ).toString();
    private static final String PATH = Paths.get( FILEPATH, FILENAME ).toString();
    private static final String ROOT_TAG = "blacklist";
    private static final String RESTRICTION_TAG = "restriction";
    private static final String ID_ATTRIBUTE = "id";
    private static final Map<Class<? extends IIDLinkedObject>, String> TAGS;
    
    static { // Make object-tag map.
        
        Map<Class<? extends IIDLinkedObject>, String> tags = new HashMap<>();
        
        tags.put( IGuild.class, "guild" );
        tags.put( IChannel.class, "channel" );
        tags.put( IUser.class, "user" );
        tags.put( IRole.class, "role" );
        
        TAGS = Collections.unmodifiableMap( tags );
        
    }
    
    private static final Logger log = LoggerFactory.getLogger( Blacklist.class );
    
    private final Document document;
    private final Element root;
    
    private static Blacklist instance;
    
    /**
     * Creates a new instance using data loaded from the blacklist file.<br>
     * If the file doesn't exist, starts a new document.
     */
    private Blacklist() {
        
        Document document = loadDocument();
        this.document = ( document == null ) ? newDocument() : document;
        this.root = this.document.getRootElement(); 
        
    }
    
    /**
     * Returns the current instance. If there isn't one, creates it.
     *
     * @return The Blacklist instance.
     */
    public static Blacklist getInstance() {
        
        if ( instance == null ) {
            instance = new Blacklist();
        }
        return instance;
        
    }
    
    /**
     * Creates a new document to represent the blacklist.
     *
     * @return The newly created document.
     */
    private Document newDocument() {
        
        Document document = DocumentHelper.createDocument();
        document.addElement( ROOT_TAG );
        return document;
        
    }
    
    /**
     * Loads an existing blacklist document.
     *
     * @return The loaded document if it exists, or null if it doesn't exist or
     *         could not be read.
     */
    private synchronized Document loadDocument() {
        
        File inputFile = new File( PATH );
        if ( !inputFile.exists() ) {
            return null;
        }
        SAXReader reader = new SAXReader();
        try {
            return reader.read( inputFile );
        } catch ( DocumentException e ) {
            log.error( "Failed to read blacklist document.", e );
            return null;
        }
        
    }
    
    /**
     * Writes blacklist document to file.
     */
    private synchronized void saveDocument() {
        
        File folders = new File( FILEPATH );
        if ( !folders.exists() ) {
            folders.mkdirs();
        }
            
        try {
            FileOutputStream  output = new FileOutputStream( new File( PATH ) );
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( output, format );
            writer.write( document );
        } catch ( FileNotFoundException e ) {
            log.error( "Could not create or open blacklist file.", e );
        } catch ( UnsupportedEncodingException e ) {
            log.error( "Could not create XML writer.", e );
        } catch ( IOException e ) {
            log.error( "Could not write to blacklist file.", e );
        }
        
    }
    
    /* Methods for traversing the blacklist tree */
    
    /**
     * Retrieves the child of a given element that has a given tag (name) and has
     * a given "name" attribute. If the child doesn't exist, returns the parent.
     *
     * @param parent Parent of the desired element.
     * @param childTag Tag (name) of the desired Element.
     * @param childId "name" attribute of the desired Element.
     * @return The child of parent with specified id/name and "name" attribute.<br>
     *         If it doesn't exist, returns null.
     */
    private Element getChild( Element parent, String childTag, String childId ) {
        
        for ( Element candidate : parent.elements( childTag ) ) {
            
            if ( childId.equals( candidate.attributeValue( ID_ATTRIBUTE ) ) ) {
                return candidate; // Found child.
            }
            
        }
        return null; // Child not found.
        
    }
    
    /**
     * Retrieves the child of a given element that has a given tag (name) and has
     * a given "name" attribute.<br>
     * If the child doesn't exist, creates it.
     *
     * @param parent Parent of the desired element.
     * @param childTag Tag (name) of the desired Element.
     * @param childId "name" attribute of the desired Element.
     * @return The child of parent with specified id/name and "name" attribute.
     */
    private Element getOrCreateChild( Element parent, String childTag, String childId ) {
        
        Element child = getChild( parent, childTag, childId );
        if ( child == null ) {
            child = parent.addElement( childTag );
            child.addAttribute( ID_ATTRIBUTE, childId );
        }
        return child;
        
    }
    
    /* Helpers for translating ID objects to elements */
    
    /**
     * Retrieves the child of a given element that represents the given object.
     *
     * @param parent Parent of the desired element.
     * @param obj The object that the child should represent.
     * @return The child of parent that represents the given object.<br>
     *         If it doesn't exist, returns null.
     */
    private Element getChild( Element parent, IIDLinkedObject obj ) {
        
        return getChild( parent, TAGS.get( obj.getClass() ), obj.getStringID() );
        
    }
    
    /**
     * Retrieves the child of a given element that represents the given object.<br>
     * If the child doesn't exist, creates it.
     *
     * @param parent Parent of the desired element.
     * @param obj The object that the child should represent.
     * @return The child of parent that represents the given object.
     */
    private Element getOrCreateChild( Element parent, IIDLinkedObject obj ) {
        
        return getOrCreateChild( parent, TAGS.get( obj.getClass() ), obj.getStringID() );
        
    }
    
    /* Methods for editing restrictions in an element */
    
    /**
     * Retrieves all the restrictions contained in a specified element.
     * 
     * @param element Element that contains the restrictions.
     * @return The list of restrictions in this element.
     */
    private Set<String> getRestrictions( Element element ) {
        
        Set<String> restrictions = new TreeSet<>();
        for ( Element restriction : element.elements( RESTRICTION_TAG ) ) {
            
            restrictions.add( restriction.getText() );
            
        }
        
        return restrictions;
        
    }
    
    /**
     * Adds a restriction to a given element, if it does not contain that restriction yet.
     * @param element Element to add the restriction to.
     * @param restriction Restriction to be added.
     *
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present in that element.
     */
    private boolean addRestriction( Element element, String restriction ) {
        
        for ( Element existent : element.elements( RESTRICTION_TAG ) ) {
            
            if ( existent.getText().equals( restriction ) ) {
                return false;
            }
            
        }
        element.addElement( RESTRICTION_TAG ).setText( restriction );
        saveDocument();
        return true;
        
    }
    
    /**
     * Removes a given restriction from a given Element. Trims any Elements that become
     * childless due to this operation.
     * @param element Element where the restriction should be removed from.
     * @param restriction Restriction to be removed.
     *
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Element.
     */
    private boolean removeRestriction( Element element, String restriction ) {
        
        for ( Element existent : element.elements( RESTRICTION_TAG ) ) {
            
            if ( existent.getText().equals( restriction ) ) {
                element.remove( existent );
                while ( ( element != root ) && ( element.elements().isEmpty() ) ) {
                    // Trims childless elements.
                    Element parent = element.getParent();
                    parent.remove( element );
                    element = parent;
                    
                }
                saveDocument();
                return true;
            }
            
        }
        return false;
        
    }
    
    /* Methods for interacting with the blacklist */
    
    // Methods for getting restrictions.
    
    /**
     * Retrieves the restrictions for a given Guild.
     *
     * @param guild Desired Guild.
     * @return The restrictions that apply for that Guild.
     */
    public Set<String> getRestrictions( IGuild guild ) {
        
        Element element = getChild( root, guild );
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    /**
     * Retrieves the restrictions for a given Channel.
     *
     * @param channel Desired Channel.
     * @return The restrictions that apply for that Channel.
     */
    public Set<String> getRestrictions( IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( root, channel.getGuild() ), channel );
        } catch ( NullPointerException e ) {
            return new HashSet<>(); // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    /**
     * Retrieves the restrictions for a given User, in a given channel.
     *
     * @param user Desired User.
     * @param channel Channel the user is in.
     * @return The restrictions that apply for that User.
     */
    public Set<String> getRestrictions( IUser user, IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( getChild( root, channel.getGuild() ),
                    channel ), user );
        } catch ( NullPointerException e ) {
            return new HashSet<>(); // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    /**
     * Retrieves the restrictions for a given User, in a given guild.
     *
     * @param user Desired User.
     * @param guild Guild the user is in.
     * @return The restrictions that apply for that User.
     */
    public Set<String> getRestrictions( IUser user, IGuild guild ) {
        
        Element element;
        try {
            element = getChild( getChild( root, guild ), user );
        } catch ( NullPointerException e ) {
            return new HashSet<>(); // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    /**
     * Retrieves the restrictions for a given Role, in a given channel.
     *
     * @param role Desired Role.
     * @param channel Channel the role is in.
     * @return The restrictions that apply for that Role.
     */
    public Set<String> getRestrictions( IRole role, IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( getChild( root, channel.getGuild() ),
                    channel ), role );
        } catch ( NullPointerException e ) {
            return new HashSet<>(); // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    /**
     * Retrieves the restrictions for a given Role, in a given guild.
     *
     * @param role Desired Role.
     * @param guild Guild the role is in.
     * @return The restrictions that apply for that Role.
     */
    public Set<String> getRestrictions( IRole role, IGuild guild ) {
        
        Element element;
        try {
            element = getChild( getChild( root, guild ), role );
        } catch ( NullPointerException e ) {
            return new HashSet<>(); // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return getRestrictions( element );
        } else {
            return new HashSet<>();
        }
        
    }
    
    
    /**
     * Recursively obtains a set of all the restrictions that apply to a given user and his/her roles up to a given
     * scope (eg all restrictions for that user under that element and its parents).
     *
     * @param element The element that represents the scope.
     * @param user The user to get restrictions for.
     * @param roles List of all the roles the user belongs to.
     * @return The set of all restrictions that apply for that user in all scopes up to the given one,
     *         or an empty set if the element is the root element.
     */
    private Set<String> getAllRestrictions( Element element, IUser user, List<IRole> roles ) {
        
        if ( element == root ) {
            return new HashSet<>();
        }
        
        Set<String> restrictions = getAllRestrictions( element.getParent(), user, roles );
        restrictions.addAll( getRestrictions( element ) ); // Adds all scope-wide restrictions.

        Element userElement = getChild( element, user );
        if ( userElement != null ) { // Adds user-specific restrictions, if any.
            restrictions.addAll( getRestrictions( userElement ) );
        }
        for ( IRole role : roles ) {
            
            Element roleElement = getChild( element, role );
            if ( roleElement != null ) { // Adds role-specific restrictions, if any.
                restrictions.addAll( getRestrictions( roleElement ) );
            }
            
        }
        
        return restrictions;
        
    }
    
    /**
     * Retrieves all the restrictions that apply for a given User in a given Channel, for all scopes, both scope-wide
     * and user-specific.
     * 
     * @param user User to get restrictions for.
     * @param channel Channel where the user is in.
     * @return The set of restrictions that apply for that user in that channel.
     */
    public Set<String> getAllRestrictions( IUser user, IChannel channel ) {
        
        Element channelElement = getChild( getChild( root, channel.getGuild() ), channel );
        return getAllRestrictions( channelElement, user, user.getRolesForGuild( channel.getGuild() ) );
        
    }
    
    // Methods for adding restrictions.
    
    /**
     * Adds a restriction to the given Guild.
     *
     * @param restriction Restriction to be added.
     * @param guild Guild to add the restriction to.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Guild.
     */
    public boolean addRestriction( String restriction, IGuild guild ) {
        
        return addRestriction( getOrCreateChild( root, guild ), restriction );
        
    }
    
    /**
     * Adds a restriction to the given Channel.
     *
     * @param restriction Restriction to be added.
     * @param channel Channel to add the restriction to.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Channel.
     */
    public boolean addRestriction( String restriction, IChannel channel ) {
        
        Element element = getOrCreateChild( getOrCreateChild( root, channel.getGuild() ), channel );
        return addRestriction( element, restriction );
        
    }
    
    /**
     * Adds a restriction to the given User in a given Channel.
     *
     * @param restriction Restriction to be added.
     * @param user User to add the restriction to.
     * @param channel Channel the user is in.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that User in that Channel.
     */
    public boolean addRestriction( String restriction, IUser user, IChannel channel ) {
        
        Element element = getOrCreateChild( getOrCreateChild( getOrCreateChild( root, channel.getGuild() ),
                channel ), user );
        return addRestriction( element, restriction );
        
    }
    
    /**
     * Adds a restriction to the given User in a given Guild.
     *
     * @param restriction Restriction to be added.
     * @param user User to add the restriction to.
     * @param guild Guild the user is in.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that User in that Guild.
     */
    public boolean addRestriction( String restriction, IUser user, IGuild guild ) {
        
        Element element = getOrCreateChild( getOrCreateChild( root, guild ), user );
        return addRestriction( element, restriction );
        
    }
    
    /**
     * Adds a restriction to the given Role in a given Channel.
     *
     * @param restriction Restriction to be added.
     * @param role Role to add the restriction to.
     * @param channel Channel the role is in.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Role in that Channel.
     */
    public boolean addRestriction( String restriction, IRole role, IChannel channel ) {
        
        Element element = getOrCreateChild( getOrCreateChild( getOrCreateChild( root, channel.getGuild() ),
                channel ), role );
        return addRestriction( element, restriction );
        
    }
    
    /**
     * Adds a restriction to the given Role in a given Guild.
     *
     * @param restriction Restriction to be added.
     * @param role Role to add the restriction to.
     * @param guild Guild the role is in.
     * @return True if the restriction was added successfully. False if the restriction was
     *         already present for that Role in that Guild.
     */
    public boolean addRestriction( String restriction, IRole role, IGuild guild ) {
        
        Element element = getOrCreateChild( getOrCreateChild( root, guild ), role );
        return addRestriction( element, restriction );
        
    }
    
    // Methods for removing restrictions.
    
    /**
     * Removes a given restriction from a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param guild Guild where the restriction should be removed from.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Guild.
     */
    public boolean removeRestriction( String restriction, IGuild guild ) {
        
        Element element = getChild( root, guild );
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }
    
    /**
     * Removes a given restriction from a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param channel Channel where the restriction should be removed from.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Channel.
     */
    public boolean removeRestriction( String restriction, IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( root, channel.getGuild() ), channel );
        } catch ( NullPointerException e ) {
            return false; // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }
    
    /**
     * Removes a given restriction from a given User in a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param user User where the restriction should be removed from.
     * @param channel Channel the user is in.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given User in the given Channel.
     */
    public boolean removeRestriction( String restriction, IUser user, IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( getChild( root, channel.getGuild() ),
                    channel ), user );
        } catch ( NullPointerException e ) {
            return false; // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }
    
    /**
     * Removes a given restriction from a given User in a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param user User where the restriction should be removed from.
     * @param guild Guild the user is in.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given User in the given Guild.
     */
    public boolean removeRestriction( String restriction, IUser user, IGuild guild ) {
        
        Element element;
        try {
            element = getChild( getChild( root, guild ), user );
        } catch ( NullPointerException e ) {
            return false; // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }
    
    /**
     * Removes a given restriction from a given Role in a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param role Role where the restriction should be removed from.
     * @param channel Channel the role is in.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Role in the given Channel.
     */
    public boolean removeRestriction( String restriction, IRole role, IChannel channel ) {
        
        Element element;
        try {
            element = getChild( getChild( getChild( root, channel.getGuild() ),
                    channel ), role );
        } catch ( NullPointerException e ) {
            return false; // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }
    
    /**
     * Removes a given restriction from a given Role in a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param role Role where the restriction should be removed from.
     * @param guild Guild the role is in.
     * @return true if the restriction was successfully removed. false if the restriction
     *         was not found on the given Role in the given Guild.
     */
    public boolean removeRestriction( String restriction, IRole role, IGuild guild ) {
        
        Element element;
        try {
            element = getChild( getChild( root, guild ), role );
        } catch ( NullPointerException e ) {
            return false; // One of the parent elements does not exist.
        }
        
        if ( element != null ) {
            return removeRestriction( element, restriction );
        } else {
            return false;
        }
        
    }

}
