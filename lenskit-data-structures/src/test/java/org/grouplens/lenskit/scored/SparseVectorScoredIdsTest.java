/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.scored;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class SparseVectorScoredIdsTest {
    private final Symbol fooSym = Symbol.of("foo");
    private final Symbol barSym = Symbol.of("bar");
    private final Symbol bazSym = Symbol.of("baz");
    private final TypedSymbol<String> tsym = TypedSymbol.of(String.class, "test.wombat");
    
    @Test
    public void testSparseVectorScoredIds() {
        MutableSparseVector sv = MutableSparseVector.create(LongUtils.packedSet(1,2,4));
        sv.set(1,1.0);
        sv.set(4,16.0);
        
        MutableSparseVector foo = sv.addChannelVector(fooSym);
        foo.set(1,2.0);
        foo.set(4,5.0);
        
        MutableSparseVector bar = sv.addChannelVector(barSym);
        bar.set(1,3.0);
        
        MutableSparseVector baz = sv.addChannelVector(bazSym);
        baz.set(2, 100.0);

        Long2ObjectMap<String> wombat = sv.addChannel(tsym);
        wombat.put(1, "hello");
        wombat.put(4, "goodbye");
        
        // check that the hasUnboxedChannel function is correct.
        for (ScoredId sid: ScoredIds.collectionFromVector(sv)) {
            assertTrue(sid.hasUnboxedChannel(fooSym));
            assertFalse(sid.hasUnboxedChannel(bazSym));
            assertTrue(sid.hasChannel(tsym));
        }
        
        ScoredIdBuilder builder = new ScoredIdBuilder();
        Set<ScoredId> expected = new HashSet<ScoredId>();
        expected.add(builder.setId(1)
                            .setScore(1.0)
                            .addChannel(fooSym, 2.0)
                            .addChannel(barSym, 3.0)
                            .addChannel(tsym, "hello")
                            .build());
        expected.add(builder.clearChannels()
                            .setId(4)
                            .setScore(16.0)
                            .addChannel(fooSym, 5.0)
                            .addChannel(tsym, "goodbye")
                            .build());
        
        // get the scored ids and put them in a hashset (for comparison).
        assertEquals(expected, new HashSet<ScoredId>(ScoredIds.collectionFromVector(sv)));
    }
}
