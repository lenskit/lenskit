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

import java.util.BitSet;
import java.util.Iterator;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class TestTypedSideChannel {

    private final String a = "a";
    private final String b = "b";
    private final String c = "c";
    
    protected TypedSideChannel<String> emptyDomainSideChannel() {
        return new TypedSideChannel<String>(new long[]{});
    }
    
    protected TypedSideChannel<String> emptySideChannel() {
        return new TypedSideChannel<String>(new long[]{1,2,4});
    }
    
    protected TypedSideChannel<String> simpleSideChannel() {
        long[] keys = {1,2,4};
        String[] values = {a,b,a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    protected TypedSideChannel<String> singletonSideChannel() {
        long[] keys = {1};
        String[] values = {a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    @Test 
    public void testConstructors() {
        TypedSideChannel<String> channel = new TypedSideChannel<String>(new long[]{1,2});
        assertTrue(channel.isEmpty());
        
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b});
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertEquals(b, channel.get(2));
        
        channel = new TypedSideChannel<String>(new long[]{1,2},1);
        assertTrue(channel.isEmpty());
        channel.put(1, a); //check if this is in domain
        try {
            channel.put(2, b);
            fail("2 shouldn't be in the domain of channel.");
        } catch (IllegalArgumentException e) {/* ignore */}
        
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, 1);
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertNull(channel.get(2));
        
        BitSet bs = new BitSet(2);
        bs.set(1);
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, bs);
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        channel.put(1, a); //check if this is in domain.
        
        bs = new BitSet(3);
        bs.set(1);
        channel = new TypedSideChannel<String>(new long[]{1,2,3},new String[]{a,b,c}, bs, 2);
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
        TypedSideChannel<String> channel = simpleSideChannel();
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
        assertNull(channel.remove(1));
        
        channel.defaultReturnValue(a);
        assertEquals(a, channel.defaultReturnValue());
        assertEquals(a, channel.get(1));
        assertEquals(a, channel.remove(1));
    }
    
    @Test
    public void testPut() {
        TypedSideChannel<String> channel = emptySideChannel();
        channel.put(1,a);
        channel.put(new Long(2), b);
        assertEquals(a,channel.get(new Long(1)));
        assertEquals(b,channel.get(2));
    }
    
    @Test
    public void testRemove() {
        TypedSideChannel<String> channel = emptySideChannel();
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
        TypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        TypedSideChannel<String> mutCopy = simple.mutableCopy();
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
        TypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        ImmutableTypedSideChannel<String> copy = simple.immutableCopy();
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
        TypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        ImmutableTypedSideChannel<String> copy = simple.freeze();
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
        TypedSideChannel<String> simple = simpleSideChannel();
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
        TypedSideChannel<String> simple = simpleSideChannel();
        simple.remove(1);
        LongSortedArraySet set = new LongSortedArraySet(new long[]{1,2,3});
        TypedSideChannel<String> subset = simple.withDomain(set);
        
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

    @Test
    public void testEntryIsSet() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "foo");
        MutableSparseVector msv = MutableSparseVector.create(3, 4, 6);
        TypedSideChannel<String> chan = msv.addChannel(sym);
        chan.put(3, "hello");
        Iterator<VectorEntry> iter = msv.fastIterator(VectorEntry.State.EITHER);
        VectorEntry entry = iter.next();
        assertThat(chan.isSet(entry), equalTo(true));
        entry = iter.next();
        assertThat(chan.isSet(entry), equalTo(false));
    }

    @Test
    public void testGetEntry() {
        TypedSymbol<String> sym = TypedSymbol.of(String.class, "foo");
        MutableSparseVector msv = MutableSparseVector.create(3, 4, 6);
        TypedSideChannel<String> chan = msv.getOrAddChannel(sym);
        chan.put(3, "hello");
        chan.put(6, "goodbye");
        Iterator<VectorEntry> iter = msv.fastIterator(VectorEntry.State.EITHER);
        VectorEntry entry = iter.next();
        assertThat(chan.get(entry), equalTo("hello"));
        entry = iter.next();
        assertThat(chan.get(entry), nullValue());
        entry = iter.next();
        assertThat(chan.get(entry), equalTo("goodbye"));
    }
}
