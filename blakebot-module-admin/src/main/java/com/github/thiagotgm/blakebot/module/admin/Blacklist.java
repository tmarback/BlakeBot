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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.DatabaseManager;
import com.github.thiagotgm.blakebot.common.storage.Storable;
import com.github.thiagotgm.blakebot.common.storage.Translator.TranslationException;
import com.github.thiagotgm.blakebot.common.storage.translate.SetTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StorableTranslator;
import com.github.thiagotgm.blakebot.common.storage.translate.StringTranslator;
import com.github.thiagotgm.blakebot.common.utils.Tree;
import com.github.thiagotgm.blakebot.common.utils.Utils;

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
public class Blacklist {
    
    private static final Logger LOG = LoggerFactory.getLogger( Blacklist.class );
    
    private static Blacklist instance;
    
    private final Tree<String,Set<Restriction>> blacklist;
    
    /**
     * Creates a new instance.
     */
	protected Blacklist() {
        
    	LOG.info( "Starting blacklist." );
        this.blacklist = DatabaseManager.getDatabase().getDataTree( "Blacklist", new StringTranslator(),
        		new SetTranslator<>( new StorableTranslator<>( () -> { return new Restriction(); } ) ) );
        
    }
    
    /**
     * Returns the running instance. If there isn't one, creates it.
     *
     * @return The currently running instance.
     */
    public static Blacklist getInstance() {
        
        if ( instance == null ) {
            instance = new Blacklist();
        }
        return instance;
        
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
        
    	Set<Restriction> restrictions = blacklist.get( Utils.idString( path ) );
    	if ( restrictions == null ) {
    		return new HashSet<>();
    	} else {
    		return new HashSet<>( restrictions );
    	}
        
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
        
    	String[] strPath = Utils.idString( path );
        Set<Restriction> restrictions = blacklist.get( strPath );
        if ( restrictions == null ) {
            restrictions = new HashSet<>();
        }
        if ( restrictions.add( restriction ) ) {
        	blacklist.set( restrictions, strPath );
        	return true;
        } else {
        	return false;
        }
        
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
        
    	String[] thePath = Utils.idString( path );
        Set<Restriction> restrictions = blacklist.get( thePath );
        if ( ( restrictions != null ) && restrictions.remove( restriction ) ) {
        	blacklist.set( restrictions, thePath );
        	return true;
        } else {
        	return false;
        }
        
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
    public static class Restriction implements Storable {

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
        
        private static final String TEXT_ATTRIBUTE = "text";
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
		public Data toData() {

			Map<String,Data> map = new HashMap<>();
			map.put( TEXT_ATTRIBUTE, Data.stringData( text ) );
			map.put( TYPE_ATTRIBUTE, Data.stringData( type.toString() ) );
			
			return Data.mapData( map );
			
		}

		@Override
		public void fromData( Data data ) throws TranslationException {
			
			if ( !data.isMap() ) {
				throw new TranslationException( "Given data is not a map." );
			}
			Map<String,Data> map = data.getMap();
			
			Data textData = map.get( TEXT_ATTRIBUTE ); // Get text.
			if ( !textData.isString() ) {
				throw new TranslationException( "Text attribute is not a string." );
			}
			text = textData.getString();
			
			Data typeData = map.get( TYPE_ATTRIBUTE ); // Get type.
			if ( !typeData.isString() ) {
				throw new TranslationException( "Type attribute is not a string." );
			}
			try {
				type = Type.valueOf( typeData.getString() ); // Compile pattern.
			} catch ( IllegalArgumentException e ) {
				throw new TranslationException( "Invalid restriction type.", e );
			}
			
			pattern = makePattern( text, type );
			
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
        
    }

}
