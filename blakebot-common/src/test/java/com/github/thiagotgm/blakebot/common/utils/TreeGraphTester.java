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

package com.github.thiagotgm.blakebot.common.utils;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/**
 * Tester class for {@link TreeGraph}.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-08-19
 */
public class TreeGraphTester {
    
    private TreeGraph<String,String> graph;

    @Before
    public void setUp() {
        
        graph = new TreeGraph<>();
        graph.add( "value 1", "hi", "i" );
        graph.add( "value 2", "hi" );
        graph.add( "value 3", "hi", "i", "am", "here" );

    }

    @Test
    public void testAddAndGet() {
        
        TreeGraph<Long,String> graph2 = new TreeGraph<>();
        TreeGraph<String,Integer> graph3 = new TreeGraph<>();

        /* Test adding */
        assertTrue( "Could not add.", graph.add( "value 4", "potato" ) );
        assertTrue( "Could not add.", graph.add( "value 5", "potato", "salad" ) );
        assertTrue( "Could not add.", graph.add( "value 6", "potato", "salad", "with dressing" ) );
        
        assertTrue( "Could not add.", graph2.add( "value 1", 1990L, 420L ) );
        assertTrue( "Could not add.", graph2.add( "value 2", 0L, -19L ) );
        
        assertTrue( "Could not add.", graph3.add( 90, "a", "number" ) );
        assertTrue( "Could not add.", graph3.add( 404, "another", "number" ) );
        assertTrue( "Could not add.", graph3.add( -1, "a", "nother", "number" ) );
        
        
        
        /* Test adding repeated */
        assertFalse( "Added repeated value.", graph.add( "other", "hi", "i" ) );
        assertFalse( "Added repeated value.", graph.add( "other", "hi" ) );
        assertFalse( "Added repeated value.", graph.add( "other", "hi", "i", "am", "here" ) );
        assertFalse( "Added repeated value.", graph.add( "other", "potato" ) );
        assertFalse( "Added repeated value.", graph.add( "other", "potato", "salad" ) );
        assertFalse( "Added repeated value.", graph.add( "other", "potato", "salad", "with dressing" ) );
        
        assertFalse( "Added repeated value.", graph2.add( "other", 1990L, 420L ) );
        assertFalse( "Added repeated value.", graph2.add( "other", 0L, -19L ) );
        
        assertFalse( "Added repeated value.", graph3.add( 0, "a", "number" ) );
        assertFalse( "Added repeated value.", graph3.add( 0, "another", "number" ) );
        assertFalse( "Added repeated value.", graph3.add( 0, "a", "nother", "number" ) );
        
        /* Test getting */
        assertEquals( "Incorrect value retrieved.", "value 1", graph.get( "hi", "i" ) );
        assertEquals( "Incorrect value retrieved.", "value 2", graph.get( "hi" ) );
        assertEquals( "Incorrect value retrieved.", "value 3", graph.get( "hi", "i", "am", "here" ) );
        assertEquals( "Incorrect value retrieved.", "value 4", graph.get( "potato" ) );
        assertEquals( "Incorrect value retrieved.", "value 5", graph.get( "potato", "salad" ) );
        assertEquals( "Incorrect value retrieved.", "value 6", graph.get( "potato", "salad", "with dressing" ) );
        
        assertEquals( "Incorrect value retrieved.", "value 1", graph2.get( 1990L, 420L ) );
        assertEquals( "Incorrect value retrieved.", "value 2", graph2.get( 0L, -19L ) );
        
        assertEquals( "Incorrect value retrieved.", new Integer( 90 ), graph3.get( "a", "number" ) );
        assertEquals( "Incorrect value retrieved.", new Integer( 404 ), graph3.get( "another", "number" ) );
        assertEquals( "Incorrect value retrieved.", new Integer( -1 ), graph3.get( "a", "nother", "number" ) );
        
    }

    @Test
    public void testSet() {

        assertEquals( "Incorrect old value.", "value 3",
                graph.set( "overwrite", "hi", "i", "am", "here" ) );
        assertNull( "There should be no old value.",
                graph.set( "new", "new", "path" ) );
        
        assertEquals( "Incorrect value retrieved.", "overwrite", graph.get( "hi", "i", "am", "here" ) );
        assertEquals( "Incorrect value retrieved.", "new", graph.get( "new", "path" ) );
        
    }
    
    @Test
    public void testGetAll() {
        
        String[] expected = { "value 2", "value 1", "value 3" };
        assertEquals( "Incorrect list returned.", Arrays.asList( expected ),
                graph.getAll( "hi", "i", "am", "here" ) );
        
        String[] expected2 = { "value 2", "value 1" };
        assertEquals( "Incorrect list returned.", Arrays.asList( expected2 ),
                graph.getAll( "hi", "i" ) );
        
    }
    
    @Test
    public void testRemove() {
        
        assertEquals( "Wrong value returned by remove.", "value 1", graph.remove( "hi", "i" ) );
        assertNull( "Value was not deleted.", graph.get( "hi", "i" ) );
        assertNull( "Delete succeeded in deleted path.", graph.remove( "hi", "i" ) );
        assertNull( "Deleted a value from an inexistent path.", graph.remove( "does", "not", "exist" ) );
        
    }
    
    @Test
    public void testSize() {
        
        assertEquals( "Incorrect graph size.", 3, graph.size() );
        graph.remove( "hi" );
        assertEquals( "Incorrect graph size.", 2, graph.size() );
        assertEquals( "Incorrect graph size.", 0,
                new TreeGraph<String,String>().size() );
        assertEquals( "Incorrect graph size.", 1,
                new TreeGraph<String,String>( "one" ).size() );
        
    }
    
    @Test
    public void testIsEmpty() {
        
        assertFalse( "Graph should not be empty.", graph.isEmpty() );
        assertTrue( "Graph should be empty.",
                new TreeGraph<String,String>().isEmpty() );
        assertFalse( "Graph should not be empty.",
                new TreeGraph<String,String>( "one" ).isEmpty() );
        
    }
    
    @Test
    public void testClear() {
        
        graph.clear();
        assertTrue( "Graph should become empty.", graph.isEmpty() );
        
    }
    
    @Test
    public void testRoot() {

        assertNull( "Graph root should be empty.", graph.get() );
        
        graph.set( "string" );
        assertEquals( "Incorrect root value.", "string", graph.get() );
        
        TreeGraph<Integer,Double> otherGraph = new TreeGraph<>( 2.3339 );
        assertEquals( "Incorrect root value.", 2.3339, otherGraph.get(), 0.0001 );
        
    }
    
    @Test
    public void testExceptions() {

        try {
            graph.add( null, "i" );
            fail( "Should have thrown an exception." );
        } catch ( NullPointerException e ) {
            // Expected.
        }
        
        try {
            graph.set( null, "i" );
            fail( "Should have thrown an exception." );
        } catch ( NullPointerException e ) {
            // Expected.
        }
        
    }
    
    @Test
    public void testSerialize() {

        String encoded = Utils.serializableToString( graph );
        TreeGraph<String,String> decoded = Utils.stringToSerializable( encoded );
        assertEquals( "Decoded graph not equal to original.", graph, decoded );
        
    }
    
    @Test
    public void testEquals() {
        
        /* Test an equal graph */
        TreeGraph<String,String> equalGraph = new TreeGraph<>();
        equalGraph.add( "value 1", "hi", "i" );
        equalGraph.add( "value 2", "hi" );
        equalGraph.add( "value 3", "hi", "i", "am", "here" );
        
        assertTrue( "Graphs should be equal.", graph.equals( equalGraph ) );
        assertTrue( "Graphs should be equal.", equalGraph.equals( graph ) );
        
        /* Test a different graph */
        TreeGraph<String,String> differentGraph1 = new TreeGraph<>();
        differentGraph1.add( "other", "noob" );
        
        assertFalse( "Graphs should not be equal.", graph.equals( differentGraph1 ) );
        assertFalse( "Graphs should not be equal.", differentGraph1.equals( graph ) );
        
        /* Test a different graph with the same keys */
        TreeGraph<String,String> differentGraph2 = new TreeGraph<>();
        differentGraph2.add( "other 1", "hi", "i" );
        differentGraph2.add( "other 2", "hi" );
        differentGraph2.add( "other 3", "hi", "i", "am", "here" );
        
        assertFalse( "Graphs should not be equal.", graph.equals( differentGraph2 ) );
        assertFalse( "Graphs should not be equal.", differentGraph2.equals( graph ) );
        
        /* Test a different graph with the same values */
        TreeGraph<String,String> differentGraph3 = new TreeGraph<>();
        differentGraph3.add( "value 1", "other" );
        differentGraph3.add( "value 2", "other", "key" );
        differentGraph3.add( "value 3", "other", "key", "here" );
        
        assertFalse( "Graphs should not be equal.", graph.equals( differentGraph3 ) );
        assertFalse( "Graphs should not be equal.", differentGraph3.equals( graph ) );
        
        /* Test a different graph with a single equal mapping */
        TreeGraph<String,String> differentGraph4 = new TreeGraph<>();
        differentGraph4.add( "value 1", "hi", "i" );
        
        assertFalse( "Graphs should not be equal.", graph.equals( differentGraph4 ) );
        assertFalse( "Graphs should not be equal.", differentGraph4.equals( graph ) );
        
        /* Test a different graph with different types */
        TreeGraph<Long,Integer> differentGraphTypes = new TreeGraph<>();
        differentGraphTypes.add( 90, 12L, 100L, 1L );
        
        assertFalse( "Graphs should not be equal.", graph.equals( differentGraphTypes ) );
        assertFalse( "Graphs should not be equal.", differentGraphTypes.equals( graph ) );
        
    }

}
