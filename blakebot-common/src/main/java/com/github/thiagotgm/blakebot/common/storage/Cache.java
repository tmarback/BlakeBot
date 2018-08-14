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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Class that provides a cache to avoid frequent calls to expensive
 * query operations in database implementations.
 * <p>
 * The capacity of the cache is provided at startup. Whenever the
 * cache is currently at full capacity and an addition is requested,
 * one of the currently cached mappings is removed using an LRU
 * algorithm (the mapping that was requested the longest ago is
 * removed).
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-08-09
 */
public class Cache<K,V> {
	
	private final CacheList data;
	private final BiMap<K,CacheList.Node> keyMap;
	private final BiMap<CacheList.Node,K> nodeMap;
	private final int capacity;
	
	/**
	 * Initializes a cache with the given capacity.
	 * 
	 * @param capacity The capacity of the cache.
	 */
	public Cache( int capacity ) {
		
		data = new CacheList();
		
		keyMap = HashBiMap.create( capacity );
		nodeMap = keyMap.inverse();
		
		this.capacity = capacity;
		
	}
	
	/**
	 * Retrieves the cached value mapped to the given key.
	 * <p>
	 * If there is a cached mapping, it is moved back to the
	 * end of the LRU deletion list.
	 * 
	 * @param key The key to get the cached value for.
	 * @return The cached value, or <tt>null</tt> if there is no
	 *         value currently cached to the given key.
	 */
	public synchronized V get( Object key ) {
		
		CacheList.Node node = keyMap.get( key ); // Look for node.
		if ( node != null ) { // Node found.
			data.moveToFront( node ); // Move node to front.
			return node.getValue();
		} else { // Not found.
			return null;
		}
		
	}
	
	/**
	 * Caches a mapping.
	 * <p>
	 * The mapping will be at the end of the LRU deletion list.
	 * 
	 * @param key The key of the mapping.
	 * @param value The value of the mapping.
	 * @return The value that was previously cached for the given key,
	 *         or <tt>null</tt> if there wasn't one.
	 */
	public synchronized V put( K key, V value ) {
		
		CacheList.Node node = keyMap.get( key ); // Look for node.
		if ( node != null ) { // Already has a node for this key.
			data.moveToFront( node );
			return node.setValue( value ); // Set new value.
		} else { // No existing node.
			if ( keyMap.size() >= capacity ) { // Already reached capacity.
				nodeMap.remove( data.removeLast() ); // Remove least recently used node.
			}
			keyMap.put( key, data.add( value ) ); // Add new data.
			return null;
		}
		
	}
	
	/**
	 * Updates the value mapped to the given key. If there are no
	 * values mapped to the key, does nothing.
	 * <p>
	 * This method does not change the LRU list, so the updated
	 * mapping will be in the same position in the delete queue
	 * as the previous mapping of the given key.
	 * 
	 * @param key The key of the mapping.
	 * @param value The value of the mapping.
	 * @return The value that was previously cached for the given key,
	 *         or <tt>null</tt> if there wasn't one (implies that
	 *         no changes were made).
	 */
	public synchronized V update( K key, V value ) {
		
		CacheList.Node node = keyMap.get( key ); // Look for node.
		if ( node != null ) { // Already has a node for this key.
			return node.setValue( value ); // Set new value.
		} else { // No existing node.
			return null;
		}
		
	}
	
	/**
	 * Removes from the cache the mapping that has the given key.
	 * 
	 * @param key The key of the mapping to remove.
	 * @return The value that was cached to the given key, or <tt>null</tt>
	 *         if there wasn't one.     
	 */
	public synchronized V remove( Object key ) {
		
		CacheList.Node node = keyMap.remove( key ); // Remove node.
		if ( node != null ) { // A node was removed.
			data.remove( node );
			return node.getValue();
		} else { // No node with given key.
			return null;
		}
		
	}
	
	/**
	 * Removes all mappings from the cache.
	 * <p>
	 * The cache will be empty after this.
	 */
	public synchronized void clear() {
		
		keyMap.clear();
		data.clear();
		
	}
	
	/**
	 * Customized version of a linked list that provides (limited) references to its internal nodes,
	 * allowing access, removal and moving elements to the front in <tt>O(1)</tt> time when the node
	 * is already known.
	 * 
	 * @version 1.0
	 * @author ThiagoTGM
	 * @since 2018-08-09
	 */
	protected class CacheList {
		
		private final ListNode head;
		private final ListNode tail;
		
		/**
		 * Instantiates an empty list.
		 */
		public CacheList() {
			
			head = new ListNode();
			tail = new ListNode( null, head, null );
			
		}
		
		/**
		 * Adds a value to the list, returning a reference to the node where it is stored.
		 * 
		 * @param value The value to be added.
		 * @return The node that stores the value.
		 * @throws NullPointerException if the value is <tt>null</tt>.
		 */
		public synchronized Node add( V value ) throws NullPointerException {
			
			if ( value == null ) {
				throw new NullPointerException( "Value cannot be null." );
			}
			
			return new ListNode( value, head, head.getNext() ); // Add to head of list.
			
		}
		
		/**
		 * Removes a node (and thus the stored value) from the list.
		 * 
		 * @param node The node to be removed.
		 */
		public synchronized void remove( Node node ) {
			
			if ( !( node instanceof Cache.CacheList.ListNode ) ) { // Ensure right type.
				throw new IllegalArgumentException( "Not a valid list node." );
			}
			
			@SuppressWarnings("unchecked")
			ListNode theNode = (ListNode) node;
			theNode.remove(); // Removes node.
			
		}
		
		/**
		 * Removes the last node of the list.
		 * 
		 * @return The removed node.
		 */
		public synchronized Node removeLast() {
			
			ListNode last = tail.getPrevious(); // Get last.
			last.remove(); // Remove from list.
			return last;
			
		}
		
		/**
		 * Moves the given node to the front of the list.
		 * 
		 * @param node The node to be moved.
		 */
		public synchronized void moveToFront( Node node ) {
			
			if ( !( node instanceof Cache.CacheList.ListNode ) ) { // Ensure right type.
				throw new IllegalArgumentException( "Not a valid list node." );
			}
			
			@SuppressWarnings("unchecked")
			ListNode theNode = (ListNode) node;
			theNode.move( head, head.getNext() ); // Move to front.
			
		}
		
		/**
		 * Removes all nodes from the list.
		 */
		public synchronized void clear() {
			
			tail.place( head, null );
			
		}
		
		/* Node subclasses */
		
		/**
		 * Node in the list that stores a value.
		 * 
		 * @version 1.0
		 * @author ThiagoTGM
		 * @since 2018-08-09
		 */
		public class Node {
			
			private V value;
			
			/**
			 * Instantiates a node that stores the given value.
			 * 
			 * @param value The value to be stored initially.
			 */
			protected Node( V value ) {
				
				this.value = value;
				
			}
			
			/**
			 * Retrieves the value stored by this node.
			 * 
			 * @return The value stored in this node.
			 */
			public V getValue() {
				
				return value;
				
			}
			
			/**
			 * Sets the value stored by this node.
			 * 
			 * @param value The value to be stored. Cannot be <tt>null</tt>.
			 * @return The value that is currently stored, that got replaced.
			 * @throws NullPointerException if the value is <tt>null</tt>.
			 */
			public V setValue( V value ) throws NullPointerException {
				
				if ( value == null ) {
					throw new NullPointerException( "Value cannot be null." );
				}
				
				V old = this.value;
				this.value = value;
				return old;
				
			}
			
		}
		
		/**
		 * A proper node in the linked list. Stores not only a value,
		 * but also the nodes that come before or after itself.
		 * <p>
		 * Unlike the general node, allows <tt>null</tt> values, to allow the
		 * use of dummy nodes.
		 * 
		 * @version 1.0
		 * @author ThiagoTGM
		 * @since 2018-08-09
		 */
		protected class ListNode extends Node {
			
			private ListNode prev;
			private ListNode next;
			
			/**
			 * Constructs a node with no value, and that has no next or previous nodes.
			 */
			public ListNode() {
				
				this( null, null, null );
				
			}
			
			/**
			 * Constructs a node that holds the given value, and that inserts itself
			 * between the given two nodes (after construction, the new node will be
			 * the node between the given two nodes.
			 * <p>
			 * Any of the arguments may be <tt>null</tt>.
			 * 
			 * @param value The value to be stored.
			 * @param prev The previous node.
			 * @param next The next node.
			 */
			public ListNode( V value, ListNode prev, ListNode next ) {
				
				super( value );
				
				place( prev, next );
				
			}
			
			/**
			 * Retrieves the node that comes before this one.
			 * 
			 * @return The node before this in the list.
			 */
			public ListNode getPrevious() {
				
				return prev;
				
			}
			
			/**
			 * Retrieves the node that comes after this one.
			 * 
			 * @return The node after this in the list.
			 */
			public ListNode getNext() {
				
				return next;
				
			}
			
			/**
			 * Removes this node from its position on the list.
			 * <p>
			 * After this call completes, the nodes that were before and after this
			 * will then point to eachother.
			 */
			public void remove() {
				
				if ( prev != null ) {
					prev.next = next;
				}
				if ( next != null ) {
					next.prev = prev;
				}
				
				prev = null;
				next = null;
				
			}
			
			/**
			 * Places this node between the given nodes.
			 * 
			 * @param prev The node that will come before.
			 * @param next The node that will come after.
			 */
			protected void place( ListNode prev, ListNode next ) {
				
				if ( prev != null ) {
					prev.next = this;
				}
				if ( next != null ) {
					next.prev = this;
				}
				
				this.prev = prev;
				this.next = next;
				
			}
			
			/**
			 * Removes this node from its current position, and places it back
			 * in the position between the given nodes.
			 * 
			 * @param prev The node that will come before.
			 * @param next The node that will come after.
			 */
			public void move( ListNode prev, ListNode next ) {
				
				remove();
				place( prev, next );
				
			}
			
		}
		
	}

}
