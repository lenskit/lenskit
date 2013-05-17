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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.symbols.Symbol;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSparseVectorScoredIds {
    private final Symbol fooSym = Symbol.of("foo");
    private final Symbol barSym = Symbol.of("bar");
    private final Symbol bazSym = Symbol.of("baz");
    
    @Test
    public void testSparseVectorScoredIds() {
        long[] domain = {1,2,4};
        MutableSparseVector sv = new MutableSparseVector(new LongSortedArraySet(domain));
        sv.set(1,1.0);
        sv.set(4,16.0);
        
        MutableSparseVector foo = sv.addChannel(fooSym);
        foo.set(1,2.0);
        foo.set(4,5.0);
        
        MutableSparseVector bar = sv.addChannel(barSym);
        bar.set(1,3.0);
        
        MutableSparseVector baz = sv.addChannel(bazSym);
        baz.set(2, 100.0);
        
        // check that the hasChannel function is correct.
        for(Iterator<ScoredId> it = sv.scoredIds().fastIterator(); it.hasNext();) {
            ScoredId sid = it.next();
            assertTrue(sid.hasChannel(fooSym));
            assertFalse(sid.hasChannel(bazSym));
        }
        
        ScoredIdBuilder builder = new ScoredIdBuilder();
        Set<ScoredId> expected = new HashSet<ScoredId>();
        expected.add(builder.setId(1)
                            .setScore(1.0)
                            .addChannel(fooSym, 2.0)
                            .addChannel(barSym, 3.0)
                            .build());
        expected.add(builder.clearChannels()
                            .setId(4)
                            .setScore(16.0)
                            .addChannel(fooSym, 5.0)
                            .build());
        
        // get the scored ids and put them in a hashset (for comparison).
        assertEquals(expected, new HashSet<ScoredId>(sv.scoredIds()));
    }
}
