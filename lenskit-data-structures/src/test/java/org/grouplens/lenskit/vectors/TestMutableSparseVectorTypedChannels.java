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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Ignore;
import org.junit.Test;

public class TestMutableSparseVectorTypedChannels {
    private static final TypedSymbol<String> fooStrSym = TypedSymbol.of("foo", String.class);
    private static final TypedSymbol<String> barStrSym = TypedSymbol.of("bar", String.class);
    private static final TypedSymbol<Integer> fooIntSym = TypedSymbol.of("foo", Integer.class);
    private static final TypedSymbol<Integer> barIntSym = TypedSymbol.of("bar", Integer.class);

    @Test
    public void testAddChanel(){
        MutableSparseVector sv = new MutableSparseVector();
        assertFalse(sv.hasChannel(fooStrSym));
        assertTrue(sv.getTypedChannels().isEmpty());
        
        TypedSideChannel<String> fooStrChan = sv.addChannel(fooStrSym);
        assertTrue(sv.hasChannel(fooStrSym));
        assertEquals(Collections.singleton(fooStrSym),sv.getTypedChannels());
        
        assertEquals (fooStrChan, sv.channel(fooStrSym));
        assertEquals (fooStrChan, sv.alwaysAddChannel(fooStrSym));
        try {
            sv.addChannel(fooStrSym);
            fail("re-adding should be illegal.");
        } catch (IllegalArgumentException e) {/* expected */}
    }
    
    @Test
    public void testMultipleChannels() {
        MutableSparseVector sv = new MutableSparseVector();
        TypedSideChannel<String> fs = sv.addChannel(fooStrSym);
        TypedSideChannel<Integer> fi = sv.addChannel(fooIntSym);
        TypedSideChannel<String> bs = sv.addChannel(barStrSym);
        TypedSideChannel<Integer> bi = sv.addChannel(barIntSym);
        
        assertNotSame(fs, fi);
        assertNotSame(fs, bs);
        assertNotSame(fs, bi);
        assertNotSame(fi, bs);
        assertNotSame(fi, bi);
        assertNotSame(bs, bi);
        
        assertEquals(fs,sv.channel(fooStrSym));
        assertEquals(fi,sv.channel(fooIntSym));
        assertEquals(bs,sv.channel(barStrSym));
        assertEquals(bi,sv.channel(barIntSym));
        
        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,fooIntSym,barStrSym,barIntSym}),
                     sv.getTypedChannels());
    }
    
    @Test
    public void testDomainMatches() {
        MutableSparseVector sv = new MutableSparseVector();
        TypedSideChannel<String> fs = sv.addChannel(fooStrSym);
        try {
            fs.put(1, "hi");
            fail("exception expected");
        } catch (IllegalArgumentException e) {/* expected */ }
        
        sv = new MutableSparseVector(new long[]{1,2,3}, new double[]{2,3,4});
        
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
    public void testAddSpecificSideChannel() {
        long[] domain = {1,2,4};
        TypedSideChannel<String> ts = new TypedSideChannel<String>(domain);
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        ts.put(1,"a");
        
        TypedSideChannel<String> ts2 = sv.addChannel(fooStrSym, ts);
        assertNotSame(ts,ts2);
        assertSame(ts2, sv.channel(fooStrSym));
        assertEquals("a",ts2.get(1));
        
        ts.put(2, "b");
        assertFalse(ts2.containsKey(2));
        
        ts.put(1, "b");
        assertEquals("a", ts2.get(1));
    }

    @Test
    public void testRemove() {
        MutableSparseVector sv = new MutableSparseVector();
        TypedSideChannel<String> fs = sv.addChannel(fooStrSym);
        sv.addChannel(barStrSym);

        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,barStrSym}),
                     sv.getTypedChannels());
        assertEquals(fs, sv.removeChannel(fooStrSym));
        assertEquals(Collections.singleton(barStrSym), sv.getTypedChannels());
        
        TypedSideChannel<String> fs2 = sv.addChannel(fooStrSym);
        assertNotSame(fs,fs2);
        
        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,barStrSym}),
                     sv.getTypedChannels());
        sv.removeAllTypedChannels();
        assertTrue(sv.getTypedChannels().isEmpty());
    }
    
    @Test
    public void testMutableCopy() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        TypedSideChannel<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        MutableSparseVector sv2 = sv.mutableCopy();
        assertTrue(sv2.hasChannel(fooStrSym));
        TypedSideChannel<String> ts2 = sv2.channel(fooStrSym);
        assertEquals("a", ts2.get(1));
        ts.put(2,"b");
        assertNull(ts2.get(2));
        ts2.put(2, "c");
        assertEquals("b", ts.get(2));
    }
    
    @Test
    public void testImmutableCopy() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        sv.set(1, 1); // required to ensure 1 and 2 in domain after immutable copy.
        sv.set(2, 2);
        TypedSideChannel<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        ImmutableSparseVector sv2 = sv.immutable();
        assertTrue(sv2.hasChannel(fooStrSym));
        ImmutableTypedSideChannel<String> ts2 = sv2.channel(fooStrSym);
        assertEquals("a", ts2.get(1));
        ts.put(2,"b");
        assertNull(ts2.get(2));
    }
    
    @Test
    public void testFreeze() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        sv.set(1, 1); // required to ensure 1 and 2 in domain after freeze.
        sv.set(2, 2);
        TypedSideChannel<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        ImmutableSparseVector sv2 = sv.freeze();
        assertTrue(sv2.hasChannel(fooStrSym));
        ImmutableTypedSideChannel<String> ts2 = sv2.channel(fooStrSym);
        assertEquals("a", ts2.get(1));
        try {
            ts.put(2,"b");
            fail("ts should also become frozen as a sideffect");
        } catch (IllegalStateException e) {/* expected */}
    }
    
    @Test
    public void testWithDomain() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        TypedSideChannel<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        ts.put(2, "b");
        
        MutableSparseVector sv2 = sv.withDomain(new LongSortedArraySet(new long[]{1,4}));
        TypedSideChannel<String> ts2 = sv2.channel(fooStrSym);
        assertNotSame(ts,ts2);
        assertEquals("a", ts2.get(1));
        assertNull(ts2.get(2));
    }
    
    @Ignore // ignore test until scored ids can be debugged.
    @Test
    public void testScoredIds() {
        long [] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        sv.set(1,1);
        sv.set(2,2);
        TypedSideChannel<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        ts.put(2, "b");
        TypedSideChannel<String> bs = sv.addChannel(barStrSym);
        bs.put(1,"ba");
        
        ScoredIdBuilder builder = new ScoredIdBuilder();
        
        Set<ScoredId> expected = new ObjectArraySet<ScoredId>();
        expected.add(builder.setId(1)
                .setScore(1)
                .addChannel(fooStrSym, "a")
                .addChannel(barStrSym, "ba")
                .build());
        builder.clearChannels();
        expected.add(builder.setId(2)
                .setScore(1)
                .addChannel(fooStrSym, "b")
                .build());
        
        assertEquals(expected,sv.scoredIds());
    }
}
