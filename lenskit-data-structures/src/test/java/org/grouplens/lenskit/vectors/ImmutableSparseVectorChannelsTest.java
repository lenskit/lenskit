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

import static org.grouplens.lenskit.vectors.SparseVectorTestCommon.closeTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;

import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ImmutableSparseVectorChannelsTest {
    Symbol fooSymbol = Symbol.of("foo");
    Symbol barSymbol = Symbol.of("bar");
    Symbol foobarSymbol = Symbol.of("foobar");

    protected MutableSparseVector emptyVector() {
        return MutableSparseVector.create(Long2DoubleMaps.EMPTY_MAP);
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
        simple.addChannelVector(fooSymbol).set(3, 77);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77));

        ImmutableSparseVector simpleImm = simple.immutable();
        assertThat(simpleImm.getChannelVector(fooSymbol).get(3), closeTo(77));
    }
    
    @Test
    public void testMissingChannel() {
        ImmutableSparseVector simpleImm = simpleVector().immutable();
        assertThat(simpleImm.getChannelVector(fooSymbol),
                   nullValue());
    }

    @Test
    public void testCopy() {
        // Initialize a vector and add a channel.
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol).set(3, 77);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77));

        // Make an immutable version
        ImmutableSparseVector simpleImm = simple.immutable();

        // Make a mutable copy of the immutable vector
        MutableSparseVector reSimple = simpleImm.mutableCopy();
        assertThat(reSimple.getChannelVector(fooSymbol).get(3), closeTo(77));

        // Change the entry for 7
        reSimple.getChannelVector(fooSymbol).set(7, 55);
        assertThat(reSimple.getChannelVector(fooSymbol).get(7), closeTo(55));

        // Convert it back to an immutable vector
        ImmutableSparseVector reSimpleImm = reSimple.immutable();
        assertThat(reSimpleImm.getChannelVector(fooSymbol).get(3), closeTo(77));
        assertThat(reSimpleImm.getChannelVector(fooSymbol).get(7), closeTo(55));

        // Now we check that the original immutable version is unchanged
        assertThat(simpleImm.getChannelVector(fooSymbol).get(3), closeTo(77));
        assertThat(simpleImm.getChannelVector(fooSymbol).get(7, -1), closeTo(-1));
    }
    
    @Test
    public void testGetChannels() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol).set(7, 77);
        simple.addChannelVector(barSymbol).set(3, 33);
        simple.addChannelVector(foobarSymbol).set(8, 88);
        
        ImmutableSparseVector simpleImm = simple.immutable();
        Set<Symbol> channelSet = simpleImm.getChannelVectorSymbols();
        assertThat(channelSet.size(), equalTo(3));
        assert(channelSet.contains(fooSymbol));
        assert(channelSet.contains(barSymbol));
        assert(channelSet.contains(foobarSymbol));
    }

    @Test
    public void testChannelVectorsHaveWrappers() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol).set(7, 42);
        ImmutableSparseVector imm = simple.immutable();
        TypedSymbol<Double> sym = fooSymbol.withType(Double.class);
        assertThat(imm.hasChannel(sym), equalTo(true));
        assertThat(imm.getChannelSymbols(), contains((TypedSymbol) sym));
        assertThat(imm.getChannel(sym),
                   hasEntry(7L, 42.0));
        assertThat(imm.getChannelVector(fooSymbol).get(7),
                   equalTo(42.0));
    }

}
