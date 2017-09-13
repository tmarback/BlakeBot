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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.SaveManager;
import com.github.thiagotgm.blakebot.common.Settings;
import com.github.thiagotgm.blakebot.common.utils.IDLinkedGraph;
import com.github.thiagotgm.blakebot.common.utils.Utils;
import com.github.thiagotgm.blakebot.common.utils.XMLElement;
import com.github.thiagotgm.blakebot.common.utils.xml.XMLSet;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Class that keeps record of the blacklist for all servers.
 * <p>
 * All the restriction sets retrieved in get methods are copies, and so do not reflect on
 * the internal blacklist if changed.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-02-07
 */
public class Blacklist implements SaveManager.Saveable, IListener<ReadyEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger( Blacklist.class );
    
    private static final Path DEFAULT_PATH = Settings.DATA_PATH.resolve( "Blacklist.xml" );
    private static Map<IDiscordClient, Blacklist> instances;
    
    static {

        instances = Collections.synchronizedMap( new HashMap<>() );
        
    }
    
    private final Path filePath;
    private final IDLinkedGraph<RestrictionSet> blacklist;
    
    /**
     * Creates a new instance using data loaded from the blacklist file.<br>
     * If the file doesn't exist, starts a new document.
     * 
     * @param client The client to be used to obtain ID-linked objects from their IDs.
     * @param filePath The path of the file to load the blacklist from and save
     *                 it in. If it does not exist, a new file and empty blacklist
     *                 are created.
     */
    protected Blacklist( IDiscordClient client, Path filePath ) {
        
        this.blacklist = new IDLinkedGraph<>( client, RestrictionSet.newFactory() );
        this.filePath = filePath;
        client.getDispatcher().registerTemporaryListener( this );
        
    }
    
    /**
     * Returns the instance for a given client. If there isn't one, creates it.
     *
     * @param client The client to get the instance for.
     * @return The Blacklist instance.
     */
    public static Blacklist getInstance( IDiscordClient client ) {
        
        Blacklist instance = instances.get( client );
        if ( instance == null ) {
            instance = new Blacklist( client, DEFAULT_PATH );
            instances.put( client, instance );
        }
        return instance;
        
    }
    
    /**
     * Once the bot is connected, loads the blacklist from file.
     *
     * @param event
     */
    public void handle( ReadyEvent event ) {
        
        load();
        
    }
    
    /**
     * Loads an existing blacklist.
     */
    private synchronized void load() {
        
        File inputFile = filePath.toFile();
        if ( inputFile.isFile() ) {
            LOG.info( "Loading Blacklist." );
            try {
                Utils.readXMLDocument( new FileInputStream( inputFile ), blacklist );
                LOG.debug( "Loaded Blacklist." );
            } catch ( FileNotFoundException e ) {
                LOG.error( "Could not open blacklist file.", e );
            } catch ( XMLStreamException e ) {
                LOG.error( "Failed to load blacklist.", e );
            }
        }
        
    }
    
    /**
     * Writes blacklist to file.
     */
    @Override
    public synchronized void save() {
        
        LOG.info( "Saving Blacklist..." );
        
        Path folders = filePath.getParent();
        if ( folders != null ) { // Ensure folders exist.
            try {
                Files.createDirectories( folders );
            } catch ( IOException e ) {
                LOG.error( "Failed to create blacklist file directories.", e );
                return;
            }
        }
            
        try {
            Utils.writeXMLDocument( new FileOutputStream( filePath.toFile() ), blacklist );
        } catch ( FileNotFoundException e ) {
            LOG.error( "Could not create or open blacklist file.", e );
            return;
        } catch ( XMLStreamException e ) {
            LOG.error( "Failed to save blacklist.", e );
            return;
        }
        
        LOG.debug( "Blacklist saved." );
        
    }
    
    /* Methods for interacting with the blacklist */
    
    // Methods for getting restrictions.
    
    /**
     * Retrieves a copy of the restrictions for a given path.
     *
     * @param path Desired path.
     * @return The restrictions that apply for that path.
     */
    protected Set<Restriction> get( IIDLinkedObject... path ) {
        
        return new RestrictionSet( blacklist.get( path ) );
        
    }
    
    /**
     * Retrieves the restrictions for a given Guild.
     *
     * @param guild Desired Guild.
     * @return The restrictions that apply for that Guild.
     */
    public Set<Restriction> getRestrictions( IGuild guild ) {
        
        return get( guild );
        
    }
    
    /**
     * Retrieves the restrictions for a given Channel.
     *
     * @param channel Desired Channel.
     * @return The restrictions that apply for that Channel.
     */
    public Set<Restriction> getRestrictions( IChannel channel ) {
        
        return get( channel.getGuild(), channel );
        
    }
    
    /**
     * Retrieves the restrictions for a given User, in a given channel.
     *
     * @param user Desired User.
     * @param channel Channel the user is in.
     * @return The restrictions that apply for that User.
     */
    public Set<Restriction> getRestrictions( IUser user, IChannel channel ) {
        
        return get( channel.getGuild(), channel, user );
        
    }
    
    /**
     * Retrieves the restrictions for a given User, in a given guild.
     *
     * @param user Desired User.
     * @param guild Guild the user is in.
     * @return The restrictions that apply for that User.
     */
    public Set<Restriction> getRestrictions( IUser user, IGuild guild ) {
        
        return get( guild, user );
        
    }
    
    /**
     * Retrieves the restrictions for a given Role, in a given channel.
     *
     * @param role Desired Role.
     * @param channel Channel the role is in.
     * @return The restrictions that apply for that Role.
     */
    public Set<Restriction> getRestrictions( IRole role, IChannel channel ) {
        
        return get( channel.getGuild(), channel, role );
        
    }
    
    /**
     * Retrieves the restrictions for a given Role, in a given guild.
     *
     * @param role Desired Role.
     * @param guild Guild the role is in.
     * @return The restrictions that apply for that Role.
     */
    public Set<Restriction> getRestrictions( IRole role, IGuild guild ) {
        
        return get( guild, role );
        
    }
    
    /**
     * Retrieves all the restrictions that apply for a given User in a given Channel, for all scopes,
     * both scope-wide and user-specific.
     * 
     * @param user User to get restrictions for.
     * @param channel Channel where the user is in.
     * @return The set of restrictions that apply for that user in that channel.
     */
    public Set<Restriction> getAllRestrictions( IUser user, IChannel channel ) {
        
        IGuild guild = channel.getGuild();
        List<IRole> roles = user.getRolesForGuild( guild );
        
        /* Get user and scope-wide restrictions */
        Set<Restriction> restrictions = getRestrictions( user, channel );
        restrictions.addAll( getRestrictions( channel ) );
        restrictions.addAll( getRestrictions( user, guild ) );
        restrictions.addAll( getRestrictions( guild ) );
        
        /* Get role restrictions */
        for ( IRole role : roles ) {
            
            restrictions.addAll( getRestrictions( role, channel ) );
            restrictions.addAll( getRestrictions( role, guild ) );
            
        }
        
        return restrictions;
        
    }
    
    // Methods for adding restrictions.
    
    /**
     * Adds a restriction to the set linked to the given path. If there is no set linked
     * to the given path, creates it.
     *
     * @param restriction The restriction to add.
     * @param path The path to add the restriction to.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that path.
     */
    protected synchronized boolean add( Restriction restriction, IIDLinkedObject... path ) {
        
        RestrictionSet restrictions = blacklist.get( path );
        if ( restrictions == null ) {
            restrictions = new RestrictionSet();
            blacklist.add( restrictions, path );
        }
        return restrictions.add( restriction );
        
    }
    
    /**
     * Adds a restriction to the given Guild.
     *
     * @param restriction Restriction to be added.
     * @param guild Guild to add the restriction to.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that Guild.
     */
    public boolean addRestriction( Restriction restriction, IGuild guild ) {
        
        return add( restriction, guild );
        
    }
    
    /**
     * Adds a restriction to the given Channel.
     *
     * @param restriction Restriction to be added.
     * @param channel Channel to add the restriction to.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that Channel.
     */
    public boolean addRestriction( Restriction restriction, IChannel channel ) {
        
        return add( restriction, channel.getGuild(), channel );
        
    }
    
    /**
     * Adds a restriction to the given User in a given Channel.
     *
     * @param restriction Restriction to be added.
     * @param user User to add the restriction to.
     * @param channel Channel the user is in.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that User in that Channel.
     */
    public boolean addRestriction( Restriction restriction, IUser user, IChannel channel ) {
        
        return add( restriction, channel.getGuild(), channel, user );
        
    }
    
    /**
     * Adds a restriction to the given User in a given Guild.
     *
     * @param restriction Restriction to be added.
     * @param user User to add the restriction to.
     * @param guild Guild the user is in.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that User in that Guild.
     */
    public boolean addRestriction( Restriction restriction, IUser user, IGuild guild ) {
        
        return add( restriction, guild, user );
        
    }
    
    /**
     * Adds a restriction to the given Role in a given Channel.
     *
     * @param restriction Restriction to be added.
     * @param role Role to add the restriction to.
     * @param channel Channel the role is in.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that Role in that Channel.
     */
    public boolean addRestriction( Restriction restriction, IRole role, IChannel channel ) {
        
        return add( restriction, channel.getGuild(), channel, role );
        
    }
    
    /**
     * Adds a restriction to the given Role in a given Guild.
     *
     * @param restriction Restriction to be added.
     * @param role Role to add the restriction to.
     * @param guild Guild the role is in.
     * @return <tt>true</tt> if the restriction was added successfully.
     *         <tt>false</tt> if the restriction was already present for that Role in that Guild.
     */
    public boolean addRestriction( Restriction restriction, IRole role, IGuild guild ) {
        
        return add( restriction, guild, role );
        
    }
    
    // Methods for removing restrictions.
    
    /**
     * Removes a restriction linked to the given path.
     *
     * @param restriction The restriction to be removed.
     * @param path The path to remove it from.
     * @return <tt>true</tt> if the restriction was removed from the given path.
     *         <tt>false</tt> if there was no such restriction on the given path.
     */
    protected synchronized boolean remove( Restriction restriction, IIDLinkedObject... path ) {
        
        RestrictionSet restrictions = blacklist.get( path );
        return ( ( restrictions != null ) && restrictions.remove( restriction ) );
        
    }
    
    /**
     * Removes a given restriction from a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param guild Guild where the restriction should be removed from.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Guild.
     */
    public boolean removeRestriction( Restriction restriction, IGuild guild ) {
        
        return remove( restriction, guild );
        
    }
    
    /**
     * Removes a given restriction from a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param channel Channel where the restriction should be removed from.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Channel.
     */
    public boolean removeRestriction( Restriction restriction, IChannel channel ) {
        
        return remove( restriction, channel.getGuild(), channel );
        
    }
    
    /**
     * Removes a given restriction from a given User in a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param user User where the restriction should be removed from.
     * @param channel Channel the user is in.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Channel.
     */
    public boolean removeRestriction( Restriction restriction, IUser user, IChannel channel ) {
        
        return remove( restriction, channel.getGuild(), channel, user );
        
    }
    
    /**
     * Removes a given restriction from a given User in a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param user User where the restriction should be removed from.
     * @param guild Guild the user is in.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Guild.
     */
    public boolean removeRestriction( Restriction restriction, IUser user, IGuild guild ) {
        
        return remove( restriction, guild, user );
        
    }
    
    /**
     * Removes a given restriction from a given Role in a given Channel.
     *
     * @param restriction Restriction to be removed.
     * @param role Role where the restriction should be removed from.
     * @param channel Channel the role is in.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Channel.
     */
    public boolean removeRestriction( Restriction restriction, IRole role, IChannel channel ) {
        
        return remove( restriction, channel.getGuild(), channel, role );
        
    }
    
    /**
     * Removes a given restriction from a given Role in a given Guild.
     *
     * @param restriction Restriction to be removed.
     * @param role Role where the restriction should be removed from.
     * @param guild Guild the role is in.
     * @return <tt>true</tt> if the restriction was successfully removed.
     *         <tt>false</tt> if the restriction was not found on the given Guild.
     */
    public boolean removeRestriction( Restriction restriction, IRole role, IGuild guild ) {
        
        return remove( restriction, guild, role );
        
    }
    
    /* Encapsulates a restriction and its metadata */
    
    /**
     * Describes a restriction in the blacklist.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-08-15
     */
    public static class Restriction implements XMLElement {
        
        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = -3849151675045924279L;

        /**
         * Identifies the type of restriction.
         *
         * @version 1.0
         * @author ThiagoTGM
         * @since 2017-08-15
         */
        public enum Type {
            
            /**
             * A sequence of characters anywhere within a message (including within
             * larger words). Case insensitive.
             */
            CONTENT,
            
            /**
             * A full word or expression (preceded and followed by either a blank space
             * or a boundary of the message). Case insensitive.
             */
            WORD,
            
            /**
             * A regex expression. Case sensitive.
             */
            REGEX
            
        }
        
        private static final int CASE_FLAG = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        
        /**
         * Local name that identifies this XML element.
         */
        public static final String TAG = "restriction";
        
        private static final String TYPE_ATTRIBUTE = "type";
        
        private String text;
        private Type type;
        private Pattern pattern;
        
        /**
         * Constructs an empty restriction.
         */
        private Restriction() {
            
            this.text = null;
            this.type = null;
            this.pattern = null;
            
        }

        /**
         * Constructs a restriction of the given type with the given text.
         *
         * @param text The text version of the restriction.
         * @param type The type of restriction.
         */
        public Restriction( String text, Type type ) {
            
            this.text = text;
            this.type = type;
            this.pattern = makePattern( text, type );
            
        }
        
        /**
         * Compiles the restriction pattern from its text and type.
         *
         * @param text The text version of the restriction.
         * @param type The type of restriction.
         * @return The pattern that identifies the restriction.
         */
        private static Pattern makePattern( String text, Type type ) {
            
            String regex;
            int flags = 0;
            switch ( type ) {
                
                case CONTENT:
                    regex = Pattern.quote( text );
                    flags = CASE_FLAG;
                    break;
                    
                case WORD:
                    regex = String.format( "(?:\\A|\\s)%s(?:\\z|\\s)", Pattern.quote( text ) );
                    flags = CASE_FLAG;
                    break;
                    
                case REGEX:
                    regex = text;
                    break;
                    
                default:
                    regex = "";
                    
            }
            return Pattern.compile( regex, flags );
            
        }
        
        /**
         * Retrieves the text of the restriction.
         *
         * @return The restriction text.
         */
        public String getText() {
            
            return text;
            
        }
        
        /**
         * Retrieves the type of the restriction.
         *
         * @return The restriction type.
         */
        public Type getType() {
            
            return type;
            
        }
        
        /**
         * Tests if the given message contains this restriction.
         *
         * @param message The message to be searched.
         * @return <tt>true</tt> if part of the message matches this restriction.
         *         <tt>false</tt> otherwise.
         */
        public boolean test( String message ) {
            
            return pattern.matcher( message ).find();
            
        }

        @Override
        public void read( XMLStreamReader in ) throws XMLStreamException {

            if ( ( in.getEventType() != XMLStreamConstants.START_ELEMENT ) ||
                  !in.getLocalName().equals( TAG ) ) {
                throw new XMLStreamException( "Not in start tag." );
            }
            
            /* Parse type */
            String typeStr = in.getAttributeValue( null, TYPE_ATTRIBUTE );
            if ( typeStr == null ) {
                throw new XMLStreamException( "Missing type attribute." );
            }
            try {
                type = Enum.valueOf( Type.class, typeStr.toUpperCase() );
            } catch ( IllegalArgumentException e ) {
                throw new XMLStreamException( "Invalid type attribute.", e );
            }
            
            text = null;
            while ( in.hasNext() ) {
                
                switch ( in.next() ) {
                    
                    case XMLStreamConstants.START_ELEMENT:
                        throw new XMLStreamException( "Unexpected subelement." );
                        
                    case XMLStreamConstants.CHARACTERS:
                        text = in.getText();
                        break;
                        
                    case XMLStreamConstants.END_ELEMENT:
                        if ( in.getLocalName().equals( TAG ) ) {
                            if ( text != null ) {
                                pattern = makePattern( text, type );
                                return; // Done reading.
                            } else {
                                throw new XMLStreamException( "Missing restriction text." );
                            }
                        } else {
                            throw new XMLStreamException( "Unexpected end element." );
                        }
                    
                }
                
            }
            throw new XMLStreamException( "Unexpected end of document." );
            
        }

        @Override
        public void write( XMLStreamWriter out ) throws XMLStreamException {

            out.writeStartElement( TAG );
            out.writeAttribute( TYPE_ATTRIBUTE, getType().toString().toLowerCase() );
            out.writeCharacters( getText() );
            out.writeEndElement();
            
        }
        
        /**
         * Compares this restriction with an object for equality. Returns <tt>true</tt> if
         * the given object is a Restriction with the same text and type.
         *
         * @param obj The object to compare to.
         * @return <tt>true</tt> if this is equal to the given object.
         *         <tt>false</tt> otherwise.
         */
        @Override
        public boolean equals( Object obj ) {
            
            if ( !( obj instanceof Restriction ) ) {
                return false;
            }
            
            Restriction r = (Restriction) obj;
            return this.getText().equals( r.getText() ) && ( this.getType() == r.getType() );
            
        }
        
        /**
         * Calculates the hash code of the restriction, which is the same as the hash
         * code of its text.
         *
         * @return The hash code of the restriction.
         */
        @Override
        public int hashCode() {
            
            return getText().hashCode();
            
        }
        
        @Override
        public String toString() {
            
            return String.format( "[%s] (%s)", getText(), getType().toString() );
            
        }
        
        /**
         * Creates a factory that produces blank Restriction instances.
         *
         * @return The new Restriction factory.
         */
        public static XMLElement.Factory<Restriction> newFactory() {
            
            return new Factory();
            
        }
        
        /**
         * Factory that creates new blank Restriction instances.
         *
         * @version 1.0
         * @author ThiagoTGM
         * @since 2017-09-11
         */
        private static class Factory implements XMLElement.Factory<Restriction> {

            /**
             * UID that represents this class.
             */
            private static final long serialVersionUID = 1230290568027312086L;

            @Override
            public Restriction newInstance() {

                return new Restriction();

            }

        }
        
    }
    
    /**
     * Contains a set of restrictions.
     *
     * @version 1.0
     * @author ThiagoTGM
     * @since 2017-09-11
     */
    public static class RestrictionSet extends XMLSet<Restriction> {

        /**
         * UID that represents this class.
         */
        private static final long serialVersionUID = 4465227673053178038L;
        
        /**
         * Local name that identifies this XML element.
         */
        public static final String TAG = "restrictions";

        /**
         * Creates a new instance.
         */
        public RestrictionSet() {
            
            super( new HashSet<>(), Restriction.newFactory() );
            
        }
        
        /**
         * Creates a new instance, initializing it to have the restrictions in the given
         * collection.
         *
         * @param restrictions The restrictions that the instantiated set should have.
         *                     If empty or <tt>null</tt>, the instantiated RestrictionSet
         *                     is empty.
         */
        public RestrictionSet( Collection<? extends Restriction> restrictions ) {
            
            this();
            if ( restrictions != null ) {
                addAll( restrictions );
            }
            
        }
        
        @Override
        public String getTag() {
            
            return TAG;
            
        }
        
        /**
         * Creates a factory that produces empty RestrictionSets.
         *
         * @return The new RestrictionSet factory.
         */
        public static XMLElement.Factory<RestrictionSet> newFactory() {
            
            return new Factory();
            
        }
        
        /**
         * Factory that creates new empty RestrictionSets.
         *
         * @version 1.0
         * @author ThiagoTGM
         * @since 2017-09-11
         */
        private static class Factory implements XMLElement.Factory<RestrictionSet> {

            /**
             * UID that represents this class.
             */
            private static final long serialVersionUID = -912979037728857291L;

            @Override
            public RestrictionSet newInstance() {

                return new RestrictionSet();

            }

        }
        
    }

}
