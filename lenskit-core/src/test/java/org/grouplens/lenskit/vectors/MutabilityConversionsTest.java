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
package org.grouplens.lenskit.vectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;

import org.grouplens.lenskit.symbols.Symbol;
import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MutabilityConversionsTest {

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
        simple.unset(3);
        assertThat(simple.size(), equalTo(2));

        ImmutableSparseVector isvSimple = simple.immutable();
        assertThat(isvSimple.size(), equalTo(2));

        MutableSparseVector reSimple = isvSimple.mutableCopy();
        assertThat(reSimple.size(), equalTo(2)); // unchanged
        assertThat(isvSimple.size(), equalTo(2)); // unchanged
        assertThat(simple.size(), equalTo(2));       // unchanged
    }

    @Test
    public void testIterate() {
        MutableSparseVector simple = simpleVector();
        assertThat(simple.size(), equalTo(3));
        simple.unset(3);
        assertThat(simple.size(), equalTo(2));

        // Check that iteration on simple goes through the right
        // number of items.
        assertThat(Iterators.size(simple.iterator()), equalTo(2));
        
        // Check that iteration on isvSimple goes through the right
        // number of items.
        ImmutableSparseVector isvSimple = simple.immutable();
        assertThat(isvSimple.size(), equalTo(2));
        assertThat(Iterators.size(isvSimple.iterator()), equalTo(2));
    }

    @Test
    public void testEquals() {
        MutableSparseVector simple = simpleVector();
        simple.unset(3);
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
