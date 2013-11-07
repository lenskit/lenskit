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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class MutableSparseVectorTypedChannelsTest {
    private static final TypedSymbol<String> fooStrSym = TypedSymbol.of(String.class, "foo");
    private static final TypedSymbol<String> barStrSym = TypedSymbol.of(String.class, "bar");
    private static final TypedSymbol<Integer> fooIntSym = TypedSymbol.of(Integer.class, "foo");
    private static final TypedSymbol<Integer> barIntSym = TypedSymbol.of(Integer.class, "bar");

    @Test
    public void testAddChanel(){
        MutableSparseVector sv = new MutableSparseVector();
        assertFalse(sv.hasChannel(fooStrSym));
        assertTrue(sv.getChannelSymbols().isEmpty());
        
        Long2ObjectMap<String> fooStrChan = sv.addChannel(fooStrSym);
        assertTrue(sv.hasChannel(fooStrSym));
        assertEquals(Collections.singleton(fooStrSym),sv.getChannelSymbols());
        
        assertEquals (fooStrChan, sv.getChannel(fooStrSym));
        assertEquals (fooStrChan, sv.getOrAddChannel(fooStrSym));
        try {
            sv.addChannel(fooStrSym);
            fail("re-adding should be illegal.");
        } catch (IllegalArgumentException e) {/* expected */}
    }
    
    @Test
    public void testMultipleChannels() {
        MutableSparseVector sv = new MutableSparseVector();
        Long2ObjectMap<String> fs = sv.addChannel(fooStrSym);
        Long2ObjectMap<Integer> fi = sv.addChannel(fooIntSym);
        Long2ObjectMap<String> bs = sv.addChannel(barStrSym);
        Long2ObjectMap<Integer> bi = sv.addChannel(barIntSym);
        
        assertNotSame(fs, fi);
        assertNotSame(fs, bs);
        assertNotSame(fs, bi);
        assertNotSame(fi, bs);
        assertNotSame(fi, bi);
        assertNotSame(bs, bi);
        
        assertEquals(fs,sv.getChannel(fooStrSym));
        assertEquals(fi,sv.getChannel(fooIntSym));
        assertEquals(bs,sv.getChannel(barStrSym));
        assertEquals(bi,sv.getChannel(barIntSym));
        
        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,fooIntSym,barStrSym,barIntSym}),
                     sv.getChannelSymbols());
    }
    
    @Test
    public void testDomainMatches() {
        MutableSparseVector sv = new MutableSparseVector();
        Long2ObjectMap<String> fs = sv.addChannel(fooStrSym);
        try {
            fs.put(1, "hi");
            fail("exception expected");
        } catch (IllegalArgumentException e) {/* expected */ }
        
        sv = new MutableSparseVector(LongKeyDomain.create(1, 2, 3),
                                     new double[]{2,3,4});
        
        fs = sv.addChannel(fooStrSym);
        fs.put(1, "a");
        fs.put(2, "a");
        fs.put(3, "a");
        try {
            fs.put(4, "d");
            fail("expection expected");
        } catch (IllegalArgumentException e) { /* expected */}
    }
    
    @Test
    public void testAddTypedSideChannel() {
        LongKeyDomain domain = LongKeyDomain.create(1, 2, 4);
        MutableTypedSideChannel<String> ts = new MutableTypedSideChannel<String>(domain);
        MutableSparseVector sv = new MutableSparseVector(domain);
        ts.put(1, "a");
        
        sv.addChannel(fooStrSym, ts);
        assertEquals("a",sv.getChannel(fooStrSym).get(1));
    }

    @Test
    public void testRemove() {
        MutableSparseVector sv = new MutableSparseVector();
        Long2ObjectMap<String> fs = sv.addChannel(fooStrSym);
        sv.addChannel(barStrSym);

        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,barStrSym}),
                     sv.getChannelSymbols());
        assertEquals(fs, sv.removeChannel(fooStrSym));
        assertEquals(Collections.singleton(barStrSym), sv.getChannelSymbols());
        
        Long2ObjectMap<String> fs2 = sv.addChannel(fooStrSym);
        assertNotSame(fs,fs2);
        
        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,barStrSym}),
                     sv.getChannelSymbols());
        sv.removeAllChannels();
        assertTrue(sv.getChannelSymbols().isEmpty());
    }
    
    @Test
    public void testMutableCopy() {
        MutableSparseVector sv = MutableSparseVector.create(1, 2, 4);
        Long2ObjectMap<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        MutableSparseVector sv2 = sv.mutableCopy();
        assertTrue(sv2.hasChannel(fooStrSym));
        Long2ObjectMap<String> ts2 = sv2.getChannel(fooStrSym);
        assertEquals("a", ts2.get(1));
        ts.put(2,"b");
        assertNull(ts2.get(2));
        ts2.put(2, "c");
        assertEquals("b", ts.get(2));
    }
    
    @Test
    public void testImmutableCopy() {
        MutableSparseVector sv = MutableSparseVector.create(1,2,4);
        sv.set(1, 1); // required to ensure 1 and 2 in domain after immutable copy.
        sv.set(2, 2);
        Long2ObjectMap<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        ImmutableSparseVector sv2 = sv.immutable();
        assertTrue(sv2.hasChannel(fooStrSym));
        Long2ObjectMap<String> ts2 = sv2.getChannel(fooStrSym);
        assertEquals("a", ts2.get(1L));
        ts.put(2,"b");
        assertNull(ts2.get(2L));
    }
    
    @Test
    public void testFreeze() {
        MutableSparseVector sv = MutableSparseVector.create(1,2,4);
        sv.set(1, 1); // required to ensure 1 and 2 in domain after freeze.
        sv.set(2, 2);
        Long2ObjectMap<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        ImmutableSparseVector sv2 = sv.freeze();
        assertTrue(sv2.hasChannel(fooStrSym));
        Long2ObjectMap<String> ts2 = sv2.getChannel(fooStrSym);
        assertEquals("a", ts2.get(1));
        try {
            ts.put(2,"b");
            fail("ts should also become frozen as a sideffect");
        } catch (IllegalStateException e) {/* expected */}
    }
    
    @Test
    public void testWithDomain() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = MutableSparseVector.create(1,2,4);
        Long2ObjectMap<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        ts.put(2, "b");
        
        MutableSparseVector sv2 = sv.withDomain(LongUtils.packedSet(1,4));
        Long2ObjectMap<String> ts2 = sv2.getChannel(fooStrSym);
        assertNotSame(ts,ts2);
        assertEquals("a", ts2.get(1));
        assertNull(ts2.get(2));
    }
}
