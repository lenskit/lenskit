/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import static org.grouplens.common.test.MoreMatchers.closeTo;
import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;

import java.util.Set;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.symbols.Symbol;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * @author John Riedl <riedl@cs.umn.edu>
 */
public class TestImmutableSparseVectorChannels {
    Symbol fooSymbol = Symbol.of("foo");
    Symbol barSymbol = Symbol.of("bar");
    Symbol foobarSymbol = Symbol.of("foobar");

    protected MutableSparseVector emptyVector() {
        return new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
    }

    protected MutableSparseVector simpleVector() {
        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        return MutableSparseVector.wrap(keys, values);
    }

    protected MutableSparseVector simpleVector2() {
        long[] keys = {3, 5, 8};
        double[] values = {2, 2.3, 1.7};
        return MutableSparseVector.wrap(keys, values);
    }

    protected MutableSparseVector singleton() {
        return MutableSparseVector.wrap(new long[]{5}, new double[]{Math.PI});
    }

    @Test
    public void testImmutable() {
	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol).set(3, 77);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77));

	ImmutableSparseVector simpleImm = simple.immutable();
	assertThat(simpleImm.channel(fooSymbol).get(3), closeTo(77));
    }

    @Test
    public void testCopy() {
	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol).set(3, 77);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77));

	ImmutableSparseVector simpleImm = simple.immutable();
	MutableSparseVector reSimple = simpleImm.mutableCopy();
	assertThat(reSimple.channel(fooSymbol).get(3), closeTo(77));
	reSimple.channel(fooSymbol).set(7, 55);
	assertThat(reSimple.channel(fooSymbol).get(7), closeTo(55));

	ImmutableSparseVector reSimpleImm = reSimple.immutable();
	assertThat(reSimpleImm.channel(fooSymbol).get(3), closeTo(77));
	assertThat(reSimpleImm.channel(fooSymbol).get(7), closeTo(55));

	// Now we check that the original immutable copy is unchanged
	assertThat(simpleImm.channel(fooSymbol).get(3), closeTo(77));
	assertThat(simpleImm.channel(fooSymbol).get(7, -1), closeTo(-1));
    }

}
