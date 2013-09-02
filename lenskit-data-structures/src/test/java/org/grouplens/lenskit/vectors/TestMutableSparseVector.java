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

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.junit.Test;

import java.util.Set;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestMutableSparseVector extends SparseVectorTestCommon {
    @Override
    protected MutableSparseVector emptyVector() {
        return new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
    }

    @Override
    protected MutableSparseVector simpleVector() {
        long[] keys = { 3, 7, 8 };
        double[] values = { 1.5, 3.5, 2 };
        return MutableSparseVector.wrap(keys, values);
    }

    @Override
    protected MutableSparseVector simpleVector2() {
        long[] keys = { 3, 5, 8 };
        double[] values = { 2, 2.3, 1.7 };
        return MutableSparseVector.wrap(keys, values);
    }

    @Override
    protected MutableSparseVector singleton() {
        return MutableSparseVector.wrap(new long[] { 5 },
                                        new double[] { Math.PI });
    }

    // Ensure that the way we're constructing the vectors leaves their
    // parts independent.
    @Test
    public void testIndependentMakers() {
        MutableSparseVector v1 = simpleVector();
        MutableSparseVector v2 = simpleVector();
        assertThat(v1.set(3, 77), closeTo(1.5));
        assertThat(v1.get(3), closeTo(77));
        assertThat(v2.get(3), closeTo(1.5));
    }

    @Test
    public void testMapConstructor() {
        Long2DoubleMap map = new Long2DoubleArrayMap();
        long[] keys = { 3, 7, 8 };
        double[] values = { 1.5, 3.5, 2 };
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        
        MutableSparseVector msv = new MutableSparseVector(map);

        assertThat(msv.get(3), closeTo(1.5));
        assertThat(msv.get(7), closeTo(3.5));
        assertThat(msv.get(8), closeTo(2));
    }

    @Test
    public void testCopy() {
        assertTrue(emptyVector().copy().isEmpty());
        MutableSparseVector v1 = singleton();
        MutableSparseVector v2 = v1.copy();
        assertThat(v1, not(sameInstance(v2)));
        assertThat(v1.keySet(), equalTo(v2.keySet()));
        assertThat(v1.values(), equalTo(v2.values()));
        assertThat(v1, equalTo(v2));
        v2.subtract(simpleVector2());
        assertThat(v2.sum(), closeTo(Math.PI - 2.3));
        assertThat(v1.sum(), closeTo(Math.PI));
    }

    @Test
    public void testImmutable() {
        MutableSparseVector v = simpleVector();
        ImmutableSparseVector iv = v.immutable();
        // we don't want to freeze
        assertThat(iv.values, not(sameInstance(v.values)));
        v.set(7, 42.0);  // the original is still mutable
        assertThat(v.get(7), closeTo(42.0));
        assertThat(iv.get(7), closeTo(3.5));
    }

    @Test
    public void testFreeze() {
        MutableSparseVector v = simpleVector();
        double[] vs = v.values;
        ImmutableSparseVector iv = v.freeze();
        assertThat(iv, equalTo((SparseVector) simpleVector()));
        // make sure freeze actually reused storage
        assertThat(iv.values, sameInstance(vs));
        
        MutableSparseVector v2 = simpleVector();
        ImmutableSparseVector iv2 = v2.freeze();
        assertThat(iv2, equalTo((SparseVector) simpleVector()));
        try {
            v2.set(3, 12);
            fail("should throw IllegalStateException because the mutable vector is frozen");
        } catch(IllegalStateException iae) { /* skip */ }
    }

    @Test
    public void testSubtract() {
        MutableSparseVector v = emptyVector();
        v.subtract(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.subtract(singleton());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(2.3 - Math.PI));
        assertThat(v.get(8), closeTo(1.7));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.subtract(simpleVector2());
        assertThat(v.get(5), closeTo(Math.PI - 2.3));
        assertThat(v.sum(), closeTo(Math.PI - 2.3));

        v = simpleVector();
        v.subtract(simpleVector2());
        assertThat(v.get(3), closeTo(-0.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(0.3));

        v = simpleVector();
        v.subtract(singleton());
        assertEquals(v, simpleVector());
        
        v = simpleVector();
        MutableSparseVector v2 = simpleVector();
        v2.unset(7);  v2.unset(8);
        v.unset(3);
        v.subtract(v2);
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(2));
        
        v = simpleVector();
        v2 = simpleVector();
        v2.unset(7);  
        v.unset(3);
        v.subtract(v2);
        assertThat(v.containsKey(3), equalTo(false));
        assertThat(v.get(3, Double.NaN), notANumber());
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(0));
    }

    @Test
    public void testAddVector() {
        MutableSparseVector v = emptyVector();
        v.add(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.add(singleton());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(2.3 + Math.PI));
        assertThat(v.get(8), closeTo(1.7));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.add(simpleVector2());
        assertThat(v.get(5), closeTo(Math.PI + 2.3));
        assertThat(v.sum(), closeTo(Math.PI + 2.3));

        v = simpleVector();
        v.add(simpleVector2());
        assertThat(v.get(3), closeTo(3.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(3.7));

        v = simpleVector();
        v.add(singleton());
        assertThat(v, equalTo(simpleVector()));
        
        v = simpleVector();
        MutableSparseVector v2 = simpleVector();
        v2.unset(7);  
        v.unset(3);
        v.add(v2);
        assertThat(v.get(3, Double.NaN), notANumber());
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(4));
    }
    
    @Test
    public void testAddValue() {
        MutableSparseVector msv = simpleVector();
       
        try {
            msv.add(12, 12);
            fail("should throw IllegalStateException because the key is not in the keyset");
        } catch(IllegalArgumentException iae) { /* skip */ }

        msv.add(3, 1);
        assertThat(msv.get(3), closeTo(2.5));
        
        msv.unset(3);
        
        try {
            msv.add(3, 12);
            fail("should throw IllegalStateException because the key is not set to a value");
        } catch(IllegalArgumentException iae) { /* skip */ }

     }

    @Test
    public void testSetConstructor() {
        long[] keys = { 2, 5 };
        MutableSparseVector v = new MutableSparseVector(new LongSortedArraySet(keys));
        assertThat(v.size(), equalTo(0));
        assertThat(v.keyDomain().toLongArray(),
                   equalTo(new long[] { 2, 5 }));
        assertFalse(v.containsKey(2));
        assertTrue(v.keyDomain().contains(2));
        assertThat(v.get(2, Double.NaN), notANumber());
    }

    @Test @SuppressWarnings("deprecation")
    public void testClear() {
        long[] keys = { 2, 5 };
        MutableSparseVector v =
            new MutableSparseVector(new LongSortedArraySet(keys));
        assertThat(v.set(2, Math.PI), notANumber());
        assertThat(v.size(), equalTo(1));
        assertThat(v.get(2), closeTo(Math.PI));
        assertThat(v.containsKey(2), equalTo(true));
        assertThat(v.containsKey(5), equalTo(false));
        assertThat(v.keySet(),
                   equalTo((Set<Long>) Sets.newHashSet(2l)));

        v.unset(2);
        assertThat(v.isEmpty(), equalTo(true));
        assertThat(v.size(), equalTo(0));
        assertThat(v.containsKey(2), equalTo(false));
        assertThat(v.set(2, Math.E), notANumber());
    }

    @Test
    public void testSimpleUnset() {
        long[] keys = { 2, 5 };
        MutableSparseVector v =
                new MutableSparseVector(new LongSortedArraySet(keys));
        assertThat(v.set(2, Math.PI), notANumber());
        assertThat(v.size(), equalTo(1));
        assertThat(v.get(2), closeTo(Math.PI));
        assertThat(v.containsKey(2), equalTo(true));
        assertThat(v.containsKey(5), equalTo(false));
        assertThat(v.keySet(),
                   equalTo((Set<Long>) Sets.newHashSet(2l)));

        v.unset(2);
        assertThat(v.isEmpty(), equalTo(true));
        assertThat(v.size(), equalTo(0));
        assertThat(v.get(2, Double.NaN), notANumber());
        assertThat(v.set(2, Math.E), notANumber());
    }

    @Test
    public void testSet() {
        try {
            emptyVector().set(5, 5);
            fail("Should throw an IllegalArgumentException because the key is not in the key domain.");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        MutableSparseVector v = simpleVector();
        assertThat(v.set(7, 2), closeTo(3.5));
        assertThat(v.get(7), closeTo(2));
    }

    @Test
    public void testSetVector() {
        MutableSparseVector v = emptyVector();
        v.set(singleton());
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.set(singleton());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(Math.PI));
        assertThat(v.get(8), closeTo(1.7));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.set(simpleVector2());
        assertThat(v.get(5), closeTo(2.3));
        assertThat(v.sum(), closeTo(2.3));

        v = simpleVector();
        v.set(simpleVector2());
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5, Double.NaN), notANumber());
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(1.7));

        v = simpleVector();
        v.set(singleton());
        assertThat(v, equalTo(simpleVector()));
        
        v = simpleVector();
        MutableSparseVector v2 = simpleVector();
        v2.unset(7);
        v.unset(3);
        v.set(v2);
        assertThat(v.get(3), closeTo(1.5));
        assertThat(v.get(7), closeTo(3.5));
        assertThat(v.get(8), closeTo(2));
     }
    
    @Test
    public void testScale() {
        MutableSparseVector v = emptyVector();
        v.multiply(1);
        assertTrue(v.isEmpty());

        v = simpleVector2();
        v.multiply(1);
        assertThat(v.get(3), closeTo(2));
        assertThat(v.get(5), closeTo(2.3));
        assertThat(v.get(8), closeTo(1.7));

        v = simpleVector2();
        v.multiply(2);
        assertThat(v.get(3), closeTo(4));
        assertThat(v.get(5), closeTo(4.6));
        assertThat(v.get(8), closeTo(3.4));

        v = singleton();
        assertThat(v.sum(), closeTo(Math.PI));
        v.multiply(3);
        assertThat(v.sum(), closeTo(Math.PI * 3));

        v = simpleVector();
        v.multiply(0.5);
        assertThat(v.get(3), closeTo(1.5 / 2));
        assertThat(v.get(7), closeTo(3.5 / 2));
        assertThat(v.get(8), closeTo(2 / 2));
    }

    
    @Test
    public void testAddToItem() {
        try {
            emptyVector().add(5, 5);
            fail("add with invalid key should throw exception");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        MutableSparseVector v = simpleVector();
        assertThat(v.add(7, 2), closeTo(5.5));
        assertThat(v.get(7), closeTo(5.5));
        assertThat(v.get(3), closeTo(1.5));
        
    }

    @Test
    public void testWrap() {
        long[] keys = { 3, 7, 9 };
        double[] values = { Math.PI, Math.E, 0.42 };
        MutableSparseVector msv = MutableSparseVector.wrap(LongArrayList.wrap(keys), DoubleArrayList.wrap(values));
        assertThat(msv.get(3), closeTo(Math.PI));
        assertThat(msv.get(7), closeTo(Math.E));
        assertThat(msv.get(9), closeTo(0.42));
    }
    
    @Test
    public void testWrapUnsorted() {
        long[] keys = { 7, 3, 9 };
        double[] values = { Math.E, Math.PI, 0.42 };
        MutableSparseVector msv = MutableSparseVector.wrapUnsorted(keys, values);
        assertThat(msv.get(3), closeTo(Math.PI));
        assertThat(msv.get(7), closeTo(Math.E));
        assertThat(msv.get(9), closeTo(0.42));
    }

    @Test
    public void testWrapTooLong() {
        long[] keys = { 3, 7, 9, 11 };
        double[] values = { Math.PI, Math.E, 0.42 };
        try {
            @SuppressWarnings("unused")
            MutableSparseVector v = MutableSparseVector.wrap(keys, values, 4);
            fail("Should throw an exception since the values array is not long enough.");
        } catch(IllegalArgumentException iae) { /* okay */ }
        
        long[] keys2 = { 3, 7, 9 };
        double[] values2 = { Math.PI, Math.E, 0.42, 7.6 };
        try {
            @SuppressWarnings("unused")
            MutableSparseVector v = MutableSparseVector.wrap(keys2, values2, 4);
            fail("Should throw an exception since the keys array is not long enough.");
        } catch(IllegalArgumentException iae) { /* okay */ }
    }
    
    @Test
    public void testWrapNotSorted() {
        long[] keys = { 3, 9, 7 };
        double[] values = { Math.PI, Math.E, 0.42 };
        try {
            @SuppressWarnings("unused")
            MutableSparseVector v = MutableSparseVector.wrap(keys, values);
            fail("Should throw an exception since the keys array is not sorted.");
        } catch(IllegalArgumentException iae) { /* okay */ }
    }
    
    @Test
    public void testOverSize() {
        long[] keys = { 3, 7, 9 };
        double[] values = { Math.PI, Math.E, 0.42 };
        MutableSparseVector v = MutableSparseVector.wrap(keys, values, 2);
        assertThat(v.size(), equalTo(2));
        assertThat(v.containsKey(9), equalTo(false));
        assertThat(v.get(9, Double.NaN), notANumber());
        assertThat(v.get(3), closeTo(Math.PI));
        v.unset(3);
        assertThat(v.size(), equalTo(1));
        assertArrayEquals(new Long[] { 7L }, v.keySet().toArray(new Long[0]));
        assertThat(v.get(7), closeTo(Math.E));
        try {
            v.set(9, 1.0);
            fail("Should throw an IllegalArgumentException because the key is not in the key domain.");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        assertThat(v.get(9, Double.NaN), notANumber());
        assertThat(v.containsKey(9), equalTo(false));
    }

    @Test
    public void testFreezeClear() {
        long[] keys = { 3, 7, 9 };
        double[] values = { Math.PI, Math.E, 0.42 };
        MutableSparseVector v = MutableSparseVector.wrap(keys, values);
        v.unset(7);
        assertThat(v.size(), equalTo(2));
        ImmutableSparseVector f = v.freeze();
        assertThat(f.size(), equalTo(2));
        assertThat(f.keySet().toLongArray(), equalTo(new long[] { 3, 9 }));
        assertThat(f.get(3), closeTo(Math.PI));
        assertThat(f.get(9), closeTo(0.42));
        assertThat(f.containsKey(7), equalTo(false));
        assertThat(f.get(7, Double.NaN), notANumber());
    }

    @Test
    public void testWithDefaultCleared() {
        MutableSparseVector v = simpleVector();
        v.unset(8);
        assertThat(v.get(8, -1), closeTo(-1));
        assertThat(v.get(8, 42), closeTo(42));
        assertThat(v.get(8, -7), closeTo(-7));
        assertThat(v.get(8, Math.E), closeTo(Math.E));
    }

    @Test
    public void testWithDomain() {
        MutableSparseVector simple = simpleVector();

        // Check that iteration on simple goes through the right
        // number of items.
        assertThat(Iterators.size(simple.iterator()), equalTo(3));

        simple.unset(8);
        assertThat(Iterators.size(simple.iterator()), equalTo(2));
        assertThat(Iterators.size(simple.fast(VectorEntry.State.EITHER)
            .iterator()), equalTo(3));
        assertThat(Iterators.size(simple.fast(VectorEntry.State.UNSET)
                                        .iterator()), equalTo(1));

        MutableSparseVector msvShrunk = simple.shrinkDomain();
        assertThat(Iterators.size(msvShrunk.fast(VectorEntry.State.UNSET)
                                           .iterator()), equalTo(0));
        assertThat(Iterators.size(msvShrunk.fast(VectorEntry.State.EITHER)
                                           .iterator()), equalTo(2));
        assertThat(Iterators.size(msvShrunk.fast(VectorEntry.State.SET)
                                           .iterator()), equalTo(2));
    }

    @Test
    public void testOtherConstructors() {
        long[] keys = { 3, 5, 8 };
        MutableSparseVector msv =
            new MutableSparseVector(new LongSortedArraySet(keys), 7);
        assertThat(msv.get(3), closeTo(7));
        assertThat(msv.get(5), closeTo(7));
        assertThat(msv.get(8), closeTo(7));
        try {
            msv.set(9, 1.0);
            fail("Should throw an IllegalArgumentException because the key is not in the key domain.");
        } catch (IllegalArgumentException iae) { /* skip */
        }

        MutableSparseVector empty = new MutableSparseVector();
        try {
            empty.set(9, 1.0);
            fail("Should throw an IllegalArgumentException because the vector has no keys.");
        } catch (IllegalArgumentException iae) { /* skip */
        }
    }

    // @Test
    // public void testCheckMutable() {
    // long[] keys = {3, 5, 8};
    // MutableSparseVector msv = new MutableSparseVector(new
    // LongSortedArraySet(keys), 7);
    // msv.
    // MutableSparseVector msvFrozen = msv.immutable();
    // try {
    // isv.set(3, 1.0);
    // fail("Should throw an IllegalArgumentException because the sparse vector is not mutable.");
    // } catch (IllegalArgumentException iae) { /* skip */ }
    // }

    @Override
    @Test
    public void testVectorEntryMethods() {
        MutableSparseVector simple = simpleVector();
        VectorEntry ve = new VectorEntry(simple, 0, 3, 33, true);
        simple.set(ve, 7);
        assertThat(simple.get(3), closeTo(7));
        assertThat(ve.getValue(), closeTo(7));
        MutableSparseVector copy = simple.copy();
        copy.set(ve, 5);
        assertThat(simple.get(3), closeTo(7));
        assertThat(copy.get(3), closeTo(5));
        assertThat(ve.getValue(), closeTo(7));  // unchanged, since we were operating on a copy
 
        VectorEntry veBogus = new VectorEntry(simple, -1, 3, 33, true);
        try {
            simple.set(veBogus, 7);
            fail("Should throw an IllegalArgumentException because the vector entry has a bogus index");
        } catch (IllegalArgumentException iae) { /* skip */
        }

        VectorEntry veBogus2 = new VectorEntry(simple, -1, 33, 33, true);
        try {
            simple.set(veBogus2, 7);
            fail("Should throw an IllegalArgumentException because the vector entry has a bogus index");
        } catch (IllegalArgumentException iae) { /* skip */
        }

        VectorEntry veNull = new VectorEntry(null, 0, 3, 33, true);
        try {
            simple.set(veNull, 7);
            fail("Should throw an IllegalArgumentException because the vector entry is not attached to this sparse vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        
        VectorEntry veBogusKeyDomain = new VectorEntry(simpleVector2(), 0, 3, 1.5, true);
        try {
            simple.set(veBogusKeyDomain, 7);
            fail("Should throw an IllegalArgumentException because the vector entry has a different key domain from the vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
    }

    @Test
    public void testUnsetVectorEntry() {
        MutableSparseVector simple = simpleVector();
        assertThat(simple.get(3, -1), closeTo(1.5));
        
        VectorEntry veBogus = new VectorEntry(null, -1, 3, 33, true);
        try {
            simple.unset(veBogus);
            fail("Should throw an IllegalArgumentException because the vector entry does not refer to the correct vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }
        
        VectorEntry veBogusKeyDomain = new VectorEntry(simpleVector2(), 0, 3, 1.5, true);
        try {
            simple.unset(veBogusKeyDomain);
            fail("Should throw an IllegalArgumentException because the vector entry has a different key domain from the vector");
        } catch (IllegalArgumentException iae) { /* skip */
        }

        VectorEntry veGood= new VectorEntry(simple, 0, 3, 1.5, true);
        simple.unset(veGood);
        assertThat(simple.get(3, -1), closeTo(-1));
    }

    @Test
    public void testUnset() {        
        MutableSparseVector simple = simpleVector();
        
        try {
            simple.unset(12);
            fail("Attempt to unset a missing key should result in IllegalArgumentException");
        } catch(IllegalArgumentException iae) { /* all good */ }
        

        simple.unset(3);
        assertThat(simple.get(3, -1), closeTo(-1));
        
        simple.fill(12);
        VectorEntry ve = new VectorEntry(simple, 2, 8, 12, true);
        simple.unset(ve);
        assertThat(simple.get(8, -1), closeTo(-1));

        simple.clear();
        for (VectorEntry ve2 : simple) {
            assertThat(simple.get(ve2), notANumber());
        }
    }
    
    @Test
    public void testFill() {
        MutableSparseVector simple = simpleVector();
        assertThat(simple.get(3), closeTo(1.5));

        simple.fill(12);
        assertThat(Iterators.size(simple.iterator()), equalTo(3));
        for (VectorEntry ve: simple) {
            assertThat(ve.getValue(), closeTo(12));
        }
        simple.unset(3);
        assertThat(Iterators.size(simple.iterator()), equalTo(2));

        simple.fill(33);
        for (VectorEntry ve: simple) {
            assertThat(ve.getValue(), closeTo(33));
        }
    }
    
    // We already have tests that keysByValue works as long as the values are unique.
    // Here we extend those tests to make sure non-unique values sort as expected. (By key, that is.)
    @Override
    @Test
    public void testSortedKeys() {
        long[] keys = { 3, 5, 8 };
        double[] values = { 1.7, 2.3, 1.7 };
        MutableSparseVector msv = MutableSparseVector.wrap(keys, values);

        assertArrayEquals(new long[]{3, 8, 5}, msv.keysByValue().toLongArray());
        assertArrayEquals(new long[]{5, 3, 8}, msv.keysByValue(true).toLongArray());
    }
    
    @Test
    public void testCachedValues() {
        /**
         * Test method for
         * {@link org.grouplens.lenskit.vectors.MutableSparseVector#norm()}.
         */
        // MSVs no longer cache their values, but we're keeping these tests
        // since they helped discover the danger!
        MutableSparseVector simple = simpleVector();
        simple.set(3, 3);
        assertThat(simple.norm(), closeTo(5.0249378105));

        /**
         * Test method for
         * {@link org.grouplens.lenskit.vectors.MutableSparseVector#sum()}.
         */
        simple = simpleVector();
        simple.set(3, 3);
        assertThat(simple.sum(), closeTo(8.5));
        
        simple.unset(3);
        assertThat(simple.sum(), closeTo(5.5));
        
        simple.fill(7);
        assertThat(simple.sum(), closeTo(21));

        /**
         * Test method for
         * {@link org.grouplens.lenskit.vectors.MutableSparseVector#mean()}.
         */
        simple = simpleVector();
        assertThat(simpleVector().mean(), closeTo(7.0 / 3));

    }

    @Test
    public void testPartialEquals() {
        // We add this test here where it is easier to change a part of a sparse vector,
        // rather than in SparseVector common.
        MutableSparseVector msv = simpleVector();
        MutableSparseVector msv2 = simpleVector();
        msv2.set(7, 77);
        assertFalse(msv.equals(msv2));
        assertFalse(msv2.equals(msv));
    }

    @Test
    public void testVectorEntryIsSet() {
        MutableSparseVector msv = simpleVector();
        VectorEntry entry = msv.iterator().next();
        assertThat(msv.isSet(entry), equalTo(true));
        msv.unset(3);
        entry = msv.fastIterator(VectorEntry.State.EITHER).next();
        assertThat(entry.getKey(), equalTo(3L));
        assertThat(msv.isSet(entry), equalTo(false));
    }

    @Test
    public void testPairwiseMultiply() {
        MutableSparseVector v1 = simpleVector();
        MutableSparseVector v2 = MutableSparseVector.create(3, 5, 8, 9);
        v2.fill(2);
        v2.set(8, 3);
        v1.multiply(v2);
        assertThat(v1.get(3), closeTo(3));
        assertThat(v1.get(7), closeTo(3.5));
        assertThat(v1.get(8), closeTo(6));
    }

    @Test
    public void testRemoveKeys() {
        MutableSparseVector v1 = simpleVector();
        assertThat(v1.keySet().remove(7),
                   equalTo(true));
        assertThat(v1.keySet(), contains(3L, 8L));
        assertThat(v1.size(), equalTo(2));
        assertThat(v1.containsKey(7L), equalTo(false));
    }

    @Test
    public void testRetainKeys() {
        MutableSparseVector v1 = simpleVector();
        assertThat(v1.keySet().retainAll(Longs.asList(3, 4, 8)),
                   equalTo(true));
        assertThat(v1.keySet(), contains(3L, 8L));
        assertThat(v1.size(), equalTo(2));
        assertThat(v1.containsKey(7L), equalTo(false));
    }

    @Test
    public void testUnsetKeySet() {
        MutableSparseVector v1 = simpleVector();
        v1.unset(7);
        assertThat(v1.unsetKeySet(), contains(7L));
    }
}
