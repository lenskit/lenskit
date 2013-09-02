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
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestImmutableSparseVectorTypedChannels {
    private static final TypedSymbol<String> fooStrSym = TypedSymbol.of(String.class, "foo");
    private static final TypedSymbol<String> barStrSym = TypedSymbol.of(String.class, "bar");
    private static final TypedSymbol<Integer> fooIntSym = TypedSymbol.of(Integer.class, "foo");
    private static final TypedSymbol<Integer> barIntSym = TypedSymbol.of(Integer.class, "bar");

    @Test
    public void testHasChannel() {
        MutableSparseVector msv = new MutableSparseVector();
        msv.addChannel(fooStrSym);
        ImmutableSparseVector isv = msv.immutable();
        assertTrue(isv.hasChannel(fooStrSym));
    }
    
    @Test
    public void testChannel() {
        MutableSparseVector msv = MutableSparseVector.create(LongUtils.packedSet(1, 2));
        msv.set(1,1);
        Long2ObjectMap<String> msc = msv.addChannel(fooStrSym);
        msc.put(1,"a");
        ImmutableSparseVector isv = msv.immutable();
        Long2ObjectMap<String> isc = isv.getChannel(fooStrSym);
        assertEquals("a", isc.get(1L));
    }
    
    @Test
    public void testMultipleChannels() {
        assertTrue(new ImmutableSparseVector().getChannelSymbols().isEmpty());
        
        MutableSparseVector msv = new MutableSparseVector();
        msv.addChannel(fooStrSym);
        msv.addChannel(fooIntSym);
        msv.addChannel(barStrSym);
        msv.addChannel(barIntSym);
        
        assertEquals(new ObjectArraySet<TypedSymbol<?>>(new TypedSymbol<?>[]{fooStrSym,fooIntSym,barStrSym,barIntSym}),
                     msv.immutable().getChannelSymbols());
       
    }
    
    @Test
    public void testMutableCopy() {
        MutableSparseVector sv = new MutableSparseVector(LongUtils.packedSet(1, 2, 4));
        sv.set(1,1); //required to ensure 1 and 2 in domain after immutable.
        sv.set(2,2);
        Long2ObjectMap<String> ts = sv.addChannel(fooStrSym);
        ts.put(1,"a");
        
        ImmutableSparseVector isv = sv.immutable();
        Long2ObjectMap<String> isc = isv.getChannel(fooStrSym);
        assertEquals("a", isc.get(1));
        
        sv = isv.mutableCopy();
        ts = sv.getChannel(fooStrSym);
        assertNotSame(isc,ts);
        assertEquals("a", ts.get(1));
        ts.put(1, "b");
        ts.put(2, "c");
        
        assertEquals("a", isc.get(1));
        assertNull(isc.get(2));
    }
}
