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

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class TypedSideChannelTest {

    private final String a = "a";
    private final String b = "b";
    private final String c = "c";
    
    protected TypedSideChannel<String> emptyDomainSideChannel() {
        return new TypedSideChannel<String>(LongKeyDomain.empty());
    }
    
    protected TypedSideChannel<String> emptySideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2, 4);
        return new TypedSideChannel<String>(keys);
    }
    
    protected TypedSideChannel<String> simpleSideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2, 4);
        String[] values = {a,b,a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    protected TypedSideChannel<String> singletonSideChannel() {
        LongKeyDomain keys = LongKeyDomain.create(1);
        String[] values = {a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    @Test 
    public void testConstructors() {
        LongKeyDomain keys = LongKeyDomain.create(1, 2);
        TypedSideChannel<String> channel = new TypedSideChannel<String>(keys.clone());
        assertTrue(channel.isEmpty());
        
        channel = new TypedSideChannel<String>(keys ,new String[]{a,b});
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertEquals(b, channel.get(2));

        keys.setActive(0, false);
        channel = new TypedSideChannel<String>(keys, new String[]{null,b});
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
    }
    
    @Test
    public void testClear() {
        TypedSideChannel<String> channel = simpleSideChannel();
        try {
            channel.clear();
            fail("immutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testSize() {
        assertEquals(0, emptySideChannel().size());
        assertEquals(1, singletonSideChannel().size());
        assertEquals(3, simpleSideChannel().size());
    }
    

    
    @Test
    public void testContains() {
        TypedSideChannel<String> channel = simpleSideChannel();
        assertTrue(channel.containsKey(1));
        assertTrue(channel.containsKey(new Long(1)));
        assertFalse(channel.containsKey(3));
        assertFalse(channel.containsKey(new Long(3)));
        assertTrue(channel.containsValue(a));
        assertFalse(channel.containsValue(c));
    }
    
    @Test
    public void testDefaultReturnValue(){
        TypedSideChannel<String> channel = emptySideChannel();
        assertNull(channel.defaultReturnValue());
        assertNull(channel.get(1));
//        try {
//            channel.defaultReturnValue(a);
//            fail("imutable side channel should throw an exception on modifiers.");
//        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testPut() {
        TypedSideChannel<String> channel = emptySideChannel();
        try {
            channel.put(1,a);
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testRemove() {
        TypedSideChannel<String> channel = simpleSideChannel();
        try {
            channel.remove(1);
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
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
        TypedSideChannel<String> simple = simpleSideChannel();
        TypedSideChannel<String> mutCopy = simple.mutableCopy();
        assertFalse(mutCopy.containsKey(3));
        assertEquals(a,mutCopy.get(1));
        assertEquals(b,mutCopy.get(2));
        assertEquals(a,mutCopy.get(4));

        //copy doesn't effect simple.
        mutCopy.remove(4);
        mutCopy.put(1, c);
        assertEquals(a,simple.get(4));
        assertEquals(a,simple.get(1));
    }
    
    @Test
    public void testImmutableCopy() {
        TypedSideChannel<String> simple = simpleSideChannel();
        assertThat(simple, sameInstance(simple.immutable()));
    }
}
