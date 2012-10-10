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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class TestMutabilityConversions {

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
    public void testClear() {
	MutableSparseVector simple = simpleVector();
	assertThat(simple.size(), equalTo(3));
	simple.clear(3);
	assertThat(simple.size(), equalTo(2));

	ImmutableSparseVector isvSimple = simple.immutable();
	assertThat(isvSimple.size(), equalTo(2));

	MutableSparseVector reSimple = isvSimple.mutableCopy();
	assertThat(reSimple.size(), equalTo(2));
	assertTrue(Double.isNaN(reSimple.set(3, 77)));
	assertThat(reSimple.size(), equalTo(3));  // changed!
	assertThat(isvSimple.size(), equalTo(2)); // unchanged
	assertThat(simple.size(), equalTo(2));	   // unchanged
    }

    @Test
    public void testIterate() {
	MutableSparseVector simple = simpleVector();
	assertThat(simple.size(), equalTo(3));
	simple.clear(3);
	assertThat(simple.size(), equalTo(2));

	// Check that iteration on simple goes through the right
	// number of items.
	int count = 0;
	for (VectorEntry entry : simple) {
	    count += 1;
	}
	assertThat(count, equalTo(2));

	// Check that iteration on isvSimple goes through the right
	// number of items.
	ImmutableSparseVector isvSimple = simple.immutable();
	assertThat(isvSimple.size(), equalTo(2));
	count = 0;
	for (VectorEntry entry : isvSimple) {
	    count += 1;
	}
	assertThat(count, equalTo(2));
    }

    @Test
    public void testEquals() {
	MutableSparseVector simple = simpleVector();
	simple.clear(3);
	ImmutableSparseVector isvSimple = simple.immutable();
	assertTrue(isvSimple.equals(simple));
	assertTrue(simple.equals(isvSimple));

	MutableSparseVector reSimple = isvSimple.mutableCopy();
	assertTrue(isvSimple.equals(reSimple));
	assertTrue(reSimple.equals(isvSimple));
	assertTrue(reSimple.equals(simple));
	assertTrue(simple.equals(reSimple));
    }

}