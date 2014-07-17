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

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import java.util.Set;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.grouplens.lenskit.vectors.SparseVectorTestCommon.closeTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MutableSparseVectorChannelsTest {
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
            empty.addChannelVector(fooSymbol).set(3, 77);
            fail("The channel on an empty vector cannot have any keys set;"
                         + " should throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) { /*ignore */ }
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol).set(3, 77);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77));
        try {
            simple.getChannelVector(fooSymbol).set(5, 77);
            fail("Should throw illegal argument exception because of bogus key 5.");
        } catch (IllegalArgumentException iae) { /*ignore */ }
    }

    @Test
    public void testAddAlreadyExisting() {
        // test adding a channel directly, with values already in it
        MutableSparseVector simple = simpleVector();
        MutableSparseVector simple2 = simpleVector();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol).set(simple2);
        simple.getChannelVector(fooSymbol).set(3, 77);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77));
        try {
            simple.addVectorChannel(fooSymbol, simpleVector());
            fail("Should throw illegal argument exception because channel already exists.");
        } catch (IllegalArgumentException iae) { /*ignore */ }
        try {
            simple.addVectorChannel(barSymbol, simpleVector2());
            fail("Should throw illegal argument exception because of incompatible key domains.");
        } catch (IllegalArgumentException iae) { /*ignore */ }
    }
    
    @Test
    public void testGetChannels() {
        MutableSparseVector simple = simpleVector();
        simple.getOrAddChannelVector(fooSymbol).set(simpleVector());
        simple.getChannelVector(fooSymbol).set(3, 77);
        simple.getOrAddChannelVector(barSymbol).set(simpleVector());
        simple.addChannelVector(foobarSymbol);
        
        Set<Symbol> channelSyms = simple.getChannelVectorSymbols();
        assert(channelSyms.contains(fooSymbol));
        assert(channelSyms.contains(barSymbol));
        assert(channelSyms.contains(foobarSymbol));
   }

    @Test
    public void testCreate() {
        MutableSparseVector empty = emptyVector();
        empty.addChannelVector(fooSymbol);
        empty.getChannelVector(fooSymbol);  // fetch the added channel
        try {
            empty.addChannelVector(fooSymbol);
            fail("Created same channel twice; Should have thrown IllegalArgumentException.");
        } catch (IllegalArgumentException iae) { /* ignore */ }
        assertThat(empty.getChannelVector(barSymbol),
                   nullValue());
        empty.getChannelVector(fooSymbol); // the channel should still be there

        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol);
        try {
            simple.addChannelVector(fooSymbol);
            fail("Created same channel twice; Should have thrown IllegalArgumentException.");
        } catch (IllegalArgumentException iae) { /* ignore */ }
        simple.addChannelVector(barSymbol);
        simple.getChannelVector(barSymbol);  // both channels should be there.
        simple.getChannelVector(fooSymbol);
    }
    
    @Test
    public void testRemoveChannel() {
        MutableSparseVector empty = emptyVector();
        empty.addChannelVector(fooSymbol);
        empty.getChannelVector(fooSymbol);  // fetch the added channel
        try {
            empty.removeChannelVector(barSymbol);
            fail("Should throw exception on removing a channel that does not exist.");
        } catch (IllegalArgumentException iae) { /* ignore */ }
        
        empty.addChannelVector(barSymbol);
        empty.getChannelVector(barSymbol);
        empty.removeChannelVector(barSymbol);
        assertThat(empty.getChannelVector(barSymbol),
                   nullValue());
    }

    @Test
    public void testRemoveAllChannels() {
        MutableSparseVector empty = emptyVector();
        empty.addChannelVector(fooSymbol);
        empty.addChannelVector(barSymbol);
        empty.removeAllChannels();
        
        try {
            empty.removeChannelVector(barSymbol);
            fail("Should throw exception on removing a channel that does not exist.");
        } catch (IllegalArgumentException iae) { /* ignore */ }
        assertThat(empty.getChannelVector(barSymbol),
                   nullValue());
    }

    @Test
    public void testCopy() {
        MutableSparseVector empty = emptyVector();
        empty.addChannelVector(fooSymbol);
        MutableSparseVector emptyCopy = empty.copy();
        emptyCopy.getChannelVector(fooSymbol);  // channel should be here in copy

        MutableSparseVector simple = simpleVector();
        MutableSparseVector earlyCopy = simple.copy();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol);
        assertThat(earlyCopy.getChannelVector(fooSymbol),
                   nullValue());
        simple.getChannelVector(fooSymbol);
        MutableSparseVector lateCopy = simple.copy();
        lateCopy.getChannelVector(fooSymbol).set(3, 4.5);
        assertThat(lateCopy.getChannelVector(fooSymbol).get(3), closeTo(4.5));
        assertThat(simple.getChannelVector(fooSymbol).get(3, -1.1), closeTo(-1.1));
        MutableSparseVector laterCopy = lateCopy.copy();
        assertThat(laterCopy.getChannelVector(fooSymbol).get(3), closeTo(4.5));
    }

    // Test that values set in a channel can be fetched back from it
    @Test
    public void testChannelValues() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol).set(3, 4.5);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(4.5));
        assertThat(simple.getChannelVector(fooSymbol).get(27, -1.0), closeTo(-1.0));
        simple.getChannelVector(fooSymbol).unset(8);
        assertThat(simple.getChannelVector(fooSymbol).get(8, 45.0), closeTo(45.0));
    }
    
    // Test that values set in a channel can be fetched back from it
    @Test
    public void testAlwaysAddChannel() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol).set(3, 4.5);
        simple.getOrAddChannelVector(fooSymbol);
        simple.getOrAddChannelVector(barSymbol);
        assertThat(simple.getChannelVector(barSymbol).isEmpty(),
                   equalTo(true));
        simple.getChannelVector(barSymbol).set(3, 33);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(4.5));
        assertThat(simple.getChannelVector(barSymbol).get(3, -1.0), closeTo(33));
        simple.getChannelVector(fooSymbol).unset(8);
        assertThat(simple.getChannelVector(fooSymbol).get(8, 45.0), closeTo(45.0));
    }



    // Test that only correct key values can be set in a channel
    @Test
    public void testKeySafety() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol).set(3, 77.7);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77.7));
        try {
            simple.getChannelVector(fooSymbol).set(27, 4.5);
            fail("no such key in domain; should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException iae) { /* ignore */ }
    }

    @Test
    public void testWithDomainChannels() {
        MutableSparseVector simple = simpleVector();

        simple.addChannelVector(fooSymbol);   // domain is 3, 7, 8
        simple.getChannelVector(fooSymbol).set(3, 77.7);
        simple.getChannelVector(fooSymbol).set(7, 22.2);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77.7));

        simple.unset(3);
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77.7));

        // We shrink the domain to 7, 8
        MutableSparseVector msvShrunk = simple.shrinkDomain();
        assertThat(msvShrunk.getChannelVector(fooSymbol).containsKey(3), equalTo(false));
        assertThat(msvShrunk.getChannelVector(fooSymbol).get(3, Double.NaN), notANumber());
        assertThat(msvShrunk.getChannelVector(fooSymbol).get(7), closeTo(22.2));
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77.7));

        // The channel should shrink to only 2 items total, one of
        // which is set
        assertThat(Iterators.size(msvShrunk.getChannelVector(fooSymbol).fast(VectorEntry.State.EITHER).iterator()), equalTo(2));

        assertThat(Iterators.size(msvShrunk.getChannelVector(fooSymbol).fast(VectorEntry.State.UNSET).iterator()), equalTo(1));
        assertThat(Iterators.size(simple.getChannelVector(fooSymbol).fast(VectorEntry.State.UNSET).iterator()), equalTo(1));
        assertThat(Iterators.size(simple.getChannelVector(fooSymbol).fast(VectorEntry.State.SET).iterator()), equalTo(2));
    }

    // Test that VectorEntrys can safely set values in a channel
    @Test
    public void testChannelVectorEntry() {
        MutableSparseVector simple = simpleVector();
        simple.addChannelVector(fooSymbol);
        simple.getChannelVector(fooSymbol).set(3, 4.5);
        for (VectorEntry ve : simple) {
            simple.getChannelVector(fooSymbol).set(ve, 77.7);
        }
        assertThat(simple.getChannelVector(fooSymbol).get(3), closeTo(77.7));
        assertThat(simple.getChannelVector(fooSymbol).get(7), closeTo(77.7));
        assertThat(simple.getChannelVector(fooSymbol).get(8), closeTo(77.7));

        MutableSparseVector simple2 = simpleVector();
        simple2.unset(3);
        simple2.shrinkDomain();
        for (VectorEntry ve : simple) {
            if (ve.getKey() != 3) {
                try {
                    simple2.set(ve, 88.8);
                    fail("Should have been illegal to use ve to set simple2 value!");
                } catch (IllegalArgumentException iae) { /*expected */ }
            }
        }
    }
    
    @Test
    public void testImmutable() {
        MutableSparseVector v = simpleVector();
        v.getOrAddChannelVector(fooSymbol).set(simpleVector());
        v.getChannelVector(fooSymbol).set(3, 77);
        v.getOrAddChannelVector(barSymbol).set(simpleVector());
        v.addChannelVector(foobarSymbol);
        
        ImmutableSparseVector iv = v.immutable();
        v.set(7, 42.0);  // the original is still mutable
        v.getChannelVector(fooSymbol).set(7, 77);
        assertThat(v.get(7), closeTo(42.0));
        assertThat(iv.get(7), closeTo(3.5));
        assertThat(v.getChannelVector(fooSymbol).get(7), closeTo(77));
    }

    @Test
    public void testFreeze() {
        MutableSparseVector v = simpleVector();
        v.getOrAddChannelVector(fooSymbol).set(simpleVector());
        v.getChannelVector(fooSymbol).set(3, 77);
        v.getOrAddChannelVector(barSymbol).set(simpleVector());
        v.addChannelVector(foobarSymbol);
        ImmutableSparseVector iv = v.freeze();
        assertThat(iv, equalTo((SparseVector) simpleVector()));
        
        try {
            v.getChannelVector(fooSymbol).set(3, 12);
            fail("should throw IllegalStateException because the mutable vector is frozen");
        } catch(IllegalStateException iae) { /* skip */ }
    }

    @Test
    public void testAddChannelCreatesWrapper() {
        MutableSparseVector v = simpleVector();
        v.addChannelVector(fooSymbol);
        assertThat(v.getChannelVector(fooSymbol),
                   not(nullValue()));
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   instanceOf(MutableSparseVectorMap.class));
        v.getChannelVector(fooSymbol).set(7, Math.PI);
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   hasEntry(7L, Math.PI));
        assertThat(v.hasChannel(fooSymbol.withType(Double.class)),
                   equalTo(true));
        assertThat(v.getChannelSymbols(),
                   contains((TypedSymbol) fooSymbol.withType(Double.class)));
        assertThat(v.getChannelVectorSymbols(), contains(fooSymbol));
    }

    @Test
    public void testAddChannelVectorWithType() {
        MutableSparseVector v = simpleVector();
        v.addChannel(fooSymbol.withType(Double.class));
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   instanceOf(MutableSparseVectorMap.class));
        assertThat(v.getChannelVector(fooSymbol),
                   not(nullValue()));
        v.getChannelVector(fooSymbol).set(7, Math.PI);
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   hasEntry(7L, Math.PI));
        assertThat(v.getChannelSymbols(),
                   contains((TypedSymbol) fooSymbol.withType(Double.class)));
        assertThat(v.getChannelVectorSymbols(), contains(fooSymbol));
    }

    @Test
    public void testRemoveChannelDeletesWrapper() {
        MutableSparseVector v = simpleVector();
        v.addChannelVector(fooSymbol);
        v.removeChannelVector(fooSymbol);
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   nullValue());
    }

    @Test
    public void testRemoveChannelVectorByType() {
        MutableSparseVector v = simpleVector();
        v.addChannelVector(fooSymbol);
        v.removeChannel(fooSymbol.withType(Double.class));
        assertThat(v.getChannelVector(fooSymbol),
                   nullValue());
        assertThat(v.getChannel(fooSymbol.withType(Double.class)),
                   nullValue());
    }
}
