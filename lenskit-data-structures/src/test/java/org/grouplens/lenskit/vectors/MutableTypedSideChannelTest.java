/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.junit.Test;

import static org.junit.Assert.*;

public class MutableTypedSideChannelTest {

    private final String a = "a";
    private final String b = "b";
    private final String c = "c";
    
    protected MutableTypedSideChannel<String> emptyDomainSideChannel() {
        return new MutableTypedSideChannel<String>(LongKeyDomain.empty());
    }
    
    protected MutableTypedSideChannel<String> emptySideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2, 4);
        return new MutableTypedSideChannel<String>(keys);
    }
    
    protected MutableTypedSideChannel<String> simpleSideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2, 4);
        String[] values = {a,b,a};
        return new MutableTypedSideChannel<String>(keys,values);
    }
    
    protected MutableTypedSideChannel<String> singletonSideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1);
        String[] values = {a};
        return new MutableTypedSideChannel<String>(keys,values);
    }
    
    @Test 
    public void testConstructors() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2);
        MutableTypedSideChannel<String> channel = new MutableTypedSideChannel<String>(keys.clone());
        assertTrue(channel.isEmpty());
        
        channel = new MutableTypedSideChannel<String>(keys.clone(), new String[]{a,b});
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertEquals(b, channel.get(2));

        keys.setActive(0, false);
        channel = new MutableTypedSideChannel<String>(keys.clone(), new String[]{null,b});
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        channel.put(1, a); //check if this is in domain.

        channel = new MutableTypedSideChannel<String>(keys.clone(), new String[]{null,b,c});
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        assertNull(channel.get(3));
        channel.put(1, a); //check if this is in domain.
        try {
            channel.put(3, c);
            fail("3 shouldn't be in the domain of channel.");
        } catch (IllegalArgumentException e) {/* ignore */}
        
    }
    
    @Test
    public void testClear() {
        MutableTypedSideChannel<String> channel = simpleSideChannel();
        assertFalse(channel.isEmpty());
        channel.clear();
        assertTrue(channel.isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(0, emptySideChannel().size());
        assertEquals(1, singletonSideChannel().size());
        assertEquals(3, simpleSideChannel().size());
    }
    

    
    @Test
    public void testContains() {
        MutableTypedSideChannel<String> channel = simpleSideChannel();
        assertTrue(channel.containsKey(1));
        assertTrue(channel.containsKey(new Long(1)));
        assertFalse(channel.containsKey(3));
        assertFalse(channel.containsKey(new Long(3)));
        assertTrue(channel.containsValue(a));
        assertFalse(channel.containsValue(c));
    }
    
    @Test
    public void testDefaultReturnValue(){
        MutableTypedSideChannel<String> channel = emptySideChannel();
        assertNull(channel.defaultReturnValue());
        assertNull(channel.get(1));
        assertNull(channel.remove(1));
        
        channel.defaultReturnValue(a);
        assertEquals(a, channel.defaultReturnValue());
        assertEquals(a, channel.get(1));
        assertEquals(a, channel.remove(1));
    }
    
    @Test
    public void testPut() {
        MutableTypedSideChannel<String> channel = emptySideChannel();
        channel.put(1,a);
        channel.put(new Long(2), b);
        assertEquals(a,channel.get(new Long(1)));
        assertEquals(b,channel.get(2));
    }
    
    @Test
    public void testRemove() {
        MutableTypedSideChannel<String> channel = emptySideChannel();
        assertNull(channel.remove(10));
        channel.put(1,a);
        assertEquals(a,channel.get(1));
        assertEquals(a,channel.remove(1));
        assertNull(channel.remove(1));
        assertNull(channel.get(1));
        
        channel.put(1,a);
        assertEquals(a,channel.get(new Long(1)));
        channel.remove(1);
        assertNull(channel.get(1));
    }
    
    @Test
    public void testKeySet() {
        assertTrue(emptySideChannel().keySet().isEmpty());
        assertTrue(emptyDomainSideChannel().keySet().isEmpty());
        LongSet expected = new LongArraySet(new long[]{1,2,4}); 
        assertEquals(expected, simpleSideChannel().keySet());
    }
    
    @Test
    public void testMutableCopy() {
        MutableTypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        MutableTypedSideChannel<String> mutCopy = simple.mutableCopy();
        assertFalse(mutCopy.containsKey(1));
        assertFalse(mutCopy.containsKey(3));
        assertEquals(b,mutCopy.get(2));
        assertEquals(a,mutCopy.get(4));
        
        // simple doesn't effect copy.
        simple.remove(2);
        simple.put(1, c);
        assertEquals(b,mutCopy.get(2));
        assertFalse(mutCopy.containsKey(1));
        
        //copy doesn't effect simple.
        mutCopy.remove(4);
        mutCopy.put(1, a);
        assertEquals(a,simple.get(4));
        assertEquals(c,simple.get(1));
    }
    
    @Test
    public void testImmutableCopy() {
        MutableTypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        TypedSideChannel<String> copy = simple.immutable();
        assertFalse(copy.containsKey(1));
        assertFalse(copy.containsKey(3));
        assertEquals(b,copy.get(2));
        assertEquals(a,copy.get(4));
        
        // simple doesn't effect copy.
        simple.remove(2);
        simple.put(1, c);
        assertEquals(b,copy.get(2));
        assertFalse(copy.containsKey(1));
    }
    

    
    @Test
    public void testFreeze() {
        MutableTypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        TypedSideChannel<String> copy = simple.immutable(simple.keys.compactCopy(), true);
        assertFalse(copy.containsKey(1));
        assertFalse(copy.containsKey(3));
        assertEquals(b,copy.get(2));
        assertEquals(a,copy.get(4));
        
        // simple is unusable
        try {
            simple.remove(2);
            fail("exception expected");
        } catch (IllegalStateException e) {/* expected */}
    }
    
    @Test
    public void testPartialFreeze() {
        MutableTypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        TypedSideChannel<String> copy = simple.partialFreeze();
        assertEquals(simple, copy);
        
     // simple is unusable
        try {
            simple.remove(2);
            fail("exception expected");
        } catch (IllegalStateException e) {/* expected */}
        
     // copy is unusable
        try {
            copy.remove(2);
            fail("exception expected");
        } catch (IllegalStateException e) {/* expected */}
    }
    
    @Test
    public void testWithDomain() {
        MutableTypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        LongKeyDomain keys = LongKeyDomain.create(1, 2, 3);
        MutableTypedSideChannel<String> subset = simple.withDomain(keys);
        
        //simple is unchanged
        assertFalse(simple.containsKey(3));
        assertFalse(simple.containsKey(1));
        assertEquals(b, simple.get(2));
        assertEquals(a, simple.get(4));
        
        //subset is subset
        assertFalse(subset.containsKey(1));
        assertFalse(subset.containsKey(3));
        assertFalse(subset.containsKey(4));
        assertEquals(b,subset.get(2));
        try {
            subset.put(4, c);
            fail("4 should no longer be in domain");
        } catch (IllegalArgumentException e) { /*expected*/ }
    }
}
