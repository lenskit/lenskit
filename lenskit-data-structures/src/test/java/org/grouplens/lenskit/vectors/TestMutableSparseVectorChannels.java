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
public class TestMutableSparseVectorChannels {
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
    public void testAdd() {
	MutableSparseVector empty = emptyVector();
	try {
	    empty.addChannel(fooSymbol).set(3, 77);
	    fail("The channel on an empty vector cannot have any keys set;"
		 + " should throw IllegalArgumentException.");
	} catch(IllegalArgumentException iae) { /*ignore */ }
	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol).set(3, 77);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77));
	try {
	    simple.channel(fooSymbol).set(5, 77);
	    fail("Should throw illegal argument exception because of bogus key 5.");
	} catch(IllegalArgumentException iae) { /*ignore */ }
    }
	
    @Test
    public void testAddAlreadyExisting() {
	// test adding a channel directly, with values already in it
	MutableSparseVector simple = simpleVector();
	MutableSparseVector simple2 = simpleVector();
	simple.addChannel(fooSymbol, simple2).set(3, 77);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77));
	try {
	    simple.addChannel(fooSymbol, simpleVector2());
	    fail("Should throw illegal argument exception because of incompatible key domains.");
	} catch(IllegalArgumentException iae) { /*ignore */ }
    }

    @Test
    public void testCreate() {
	MutableSparseVector empty = emptyVector();
	empty.addChannel(fooSymbol);
	empty.channel(fooSymbol);  // fetch the added channel
	try {
	    empty.addChannel(fooSymbol);
	    fail("Created same channel twice; Should have thrown IllegalArgumentException.");
	} catch(IllegalArgumentException iae) { /* ignore */ }
	try {
	    empty.channel(barSymbol);
	    fail("No such channel has been added yet; Should have thrown IllegalArgumentException.");
	} catch(IllegalArgumentException iae) { /* ignore */ }
	empty.channel(fooSymbol); // the channel should still be there

	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol);
	try {
	    simple.addChannel(fooSymbol);
	    fail("Created same channel twice; Should have thrown IllegalArgumentException.");
	} catch(IllegalArgumentException iae) { /* ignore */ }
	simple.addChannel(barSymbol);
	simple.channel(barSymbol);  // both channels should be there.
	simple.channel(fooSymbol);
    }

    @Test
    public void testCopy() {
	MutableSparseVector empty = emptyVector();
	empty.addChannel(fooSymbol);
	MutableSparseVector emptyCopy = empty.copy();
	emptyCopy.channel(fooSymbol);  // channel should be here in copy

	MutableSparseVector simple = simpleVector();
	MutableSparseVector earlyCopy = simple.copy();
	simple.addChannel(fooSymbol);
	simple.channel(fooSymbol);
	try {
	    earlyCopy.channel(fooSymbol);
	    fail("Copy should not have this channel, which was created after the copy; Should have thrown IllegalArgumentException.");
	} catch(IllegalArgumentException iae) { /* ignore */ }
	simple.channel(fooSymbol);
	MutableSparseVector lateCopy = simple.copy();
	lateCopy.channel(fooSymbol).set(3, 4.5);
	assertThat(lateCopy.channel(fooSymbol).get(3), closeTo(4.5));
	assertThat(simple.channel(fooSymbol).get(3, -1.1), closeTo(-1.1));
	MutableSparseVector laterCopy = lateCopy.copy();
	assertThat(laterCopy.channel(fooSymbol).get(3), closeTo(4.5));
    }

    // Test that values set in a channel can be fetched back from it
    @Test
    public void testChannelValues() {
	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol);
	simple.channel(fooSymbol).set(3, 4.5);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(4.5));
	assertThat(simple.channel(fooSymbol).get(27, -1.0), closeTo(-1.0));
	simple.channel(fooSymbol).clear(8);
	assertThat(simple.channel(fooSymbol).get(8, 45.0), closeTo(45.0));
    }

    // Test that only correct key values can be set in a channel
    @Test
    public void testKeySafety() {
	MutableSparseVector simple = simpleVector();
	simple.addChannel(fooSymbol);
	simple.channel(fooSymbol).set(3, 77.7);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77.7));
	try {
	    simple.channel(fooSymbol).set(27, 4.5);
	    fail("no such key in domain; should have failed with IllegalArgumentException");
	} catch(IllegalArgumentException iae) { /* ignore */ }
    }

    @Test
    public void testWithDomainChannels() {
	MutableSparseVector simple = simpleVector();
	
	simple.addChannel(fooSymbol);   // domain is 3, 7, 8
	simple.channel(fooSymbol).set(3, 77.7);
	simple.channel(fooSymbol).set(7, 22.2);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77.7));

	simple.clear(3);
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77.7));

	// We shrink the domain to 7, 8
	MutableSparseVector msvShrunk = simple.withDomain();
	assertTrue(Double.isNaN(msvShrunk.channel(fooSymbol).get(3)));
	assertThat(msvShrunk.channel(fooSymbol).get(7), closeTo(22.2));
	assertThat(simple.channel(fooSymbol).get(3), closeTo(77.7));

	// The channel should shrink to only 2 items total, one of
	// which is set
	int count = 0;
	for (VectorEntry entry : msvShrunk.channel(fooSymbol).fast(VectorEntry.State.EITHER)) {
	    count += 1;
	}
	assertThat(count, equalTo(2));

	count = 0;
	for (VectorEntry entry : msvShrunk.channel(fooSymbol).fast(VectorEntry.State.UNSET)) {
	    count += 1;
	}
	assertThat(count, equalTo(1));

	// The original channel should still have 1 unset, and 2 set.
	count = 0;
	for (VectorEntry entry : simple.channel(fooSymbol).fast(VectorEntry.State.UNSET)) {
	    count += 1;
	}
	assertThat(count, equalTo(1));

	count = 0;
	for (VectorEntry entry : simple.channel(fooSymbol).fast(VectorEntry.State.SET)) {
	    count += 1;
	}
	assertThat(count, equalTo(2));
    }


}
