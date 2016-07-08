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
package org.grouplens.lenskit.collections;

import net.java.quickcheck.Generator;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.List;

import static net.java.quickcheck.generator.CombinedGenerators.sortedLists;
import static net.java.quickcheck.generator.CombinedGenerators.uniqueValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * Dynamic tests for long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@RunWith(Theories.class)
public class DynamicLongKeyDomainTest {
    private static class KeyData {
        List<Long> allKeys;

        public KeyData(List<Long> keys) {
            allKeys = keys;
        }

        public long getKey() {
            return getKey(0);
        }

        public long getKey(int k) {
            return allKeys.get(1 + k*2);
        }

        public long[] getKeys(int n) {
            long[] keys = new long[n];
            for (int i = 0; i < n; i++) {
                keys[i] = getKey(i);
            }
            return keys;
        }

        public long getLow() {
            return allKeys.get(0);
        }
        public long getAfter(int k) {
            return allKeys.get(2 + k*2);
        }
    }

    @DataPoints
    public static KeyData[] DATA_POINTS;

    /**
     * Method to compute the data points. Split out so that errors it throws get reported.
     */
    @BeforeClass
    public static void makeDataPoints() {
        KeyData[] data = new KeyData[10];
        Generator<Long> intGen = longs(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Generator<Long> longGen = longs(Integer.MAX_VALUE + 1L, Long.MAX_VALUE);
        for (int i = 0; i < 10; i++) {
            Generator<List<Long>> listGen;
            if (i % 2 == 0) {
                // generate ints
                listGen = sortedLists(uniqueValues(intGen), 10, 10);
            } else {
                // generate longs
                listGen = sortedLists(uniqueValues(longGen), 10, 10);
            }
            List<Long> nums = listGen.next();
            data[i] = new KeyData(nums);
        }
        DATA_POINTS = data;
    }

    @Theory
    public void testSingleton(KeyData data) {
        assumeThat(data, notNullValue());
        long key = data.getKey();       // key to use
        long low = data.getLow();       // unused low key
        long high = data.getAfter(1);   // unused high key
        long[] rawKeys = {key};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 1, true);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.activeSetView(), hasSize(1));

        assertThat(keys.getIndex(key), equalTo(0));
        assertThat(keys.getIndexIfActive(key), equalTo(0));
        assertThat(keys.getIndex(low), lessThan(0));
        assertThat(keys.getIndex(high), lessThan(0));
        assertThat(keys.getIndexIfActive(low), lessThan(0));
        assertThat(keys.getIndexIfActive(high), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.keyIsActive(key), equalTo(true));
        assertThat(keys.keyList(), contains(key));
        assertThat(keys.isCompletelySet(), equalTo(true));
    }

    @Theory
    public void testSingletonUnset(KeyData data) {
        long key = data.getKey();
        long low = data.getLow();
        long high = data.getAfter(1);

        long[] rawKeys = {key};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 1, false);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(key), equalTo(0));
        assertThat(keys.getIndexIfActive(key), lessThan(0));
        assertThat(keys.getIndex(low), lessThan(0));
        assertThat(keys.getIndex(high), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.keyIsActive(key), equalTo(false));
        assertThat(keys.keyList(), contains(key));
        assertThat(keys.isCompletelySet(), equalTo(false));
    }

    @Theory
    public void testMultiple(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.domain(), contains(k1, k2, k3));
        assertThat(keys.keyList(), contains(k1, k2, k3));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));

        assertThat(keys.getIndex(k1), equalTo(0));
        assertThat(keys.getIndex(k2), equalTo(1));
        assertThat(keys.getIndex(k3), equalTo(2));
        assertThat(keys.getIndexIfActive(k2), equalTo(1));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.getIndex(data.getLow()), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(0)), lessThan(0));
        assertThat(keys.getIndexIfActive(data.getAfter(0)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(1)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(2)), lessThan(0));

        assertThat(keys.keyIsActive(k2), equalTo(true));
        assertThat(keys.isCompletelySet(), equalTo(true));
    }

    @Theory
    public void testMultipleUnset(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(k1, k2, k3));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(k1), equalTo(0));
        assertThat(keys.getIndex(k2), equalTo(1));
        assertThat(keys.getIndex(k3), equalTo(2));
        assertThat(keys.getIndexIfActive(k2), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndex(data.getLow()), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(0)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(1)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(2)), lessThan(0));

        assertThat(keys.keyIsActive(k2), equalTo(false));
        assertThat(keys.isCompletelySet(), equalTo(false));
    }

    @Theory
    public void testSetAllActive(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(k1, k2, k3));
        assertThat(keys.activeSetView(), hasSize(0));

        keys.setAllActive(true);
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));

        assertThat(keys.getIndex(k1), equalTo(0));
        assertThat(keys.getIndex(k2), equalTo(1));
        assertThat(keys.getIndex(k3), equalTo(2));
        assertThat(keys.getIndexIfActive(k2), equalTo(1));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.getIndex(data.getLow()), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(0)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(1)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(2)), lessThan(0));

        assertThat(keys.keyIsActive(k2), equalTo(true));

        assertThat(keys.isCompletelySet(), equalTo(true));
    }

    @Theory
    public void testSetAllInactive(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.domain(), contains(k1, k2, k3));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));


        keys.setAllActive(false);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(k1), equalTo(0));
        assertThat(keys.getIndex(k2), equalTo(1));
        assertThat(keys.getIndex(k3), equalTo(2));
        assertThat(keys.getIndexIfActive(k2), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndex(data.getLow()), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(0)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(1)), lessThan(0));
        assertThat(keys.getIndex(data.getAfter(2)), lessThan(0));

        assertThat(keys.keyIsActive(k2), equalTo(false));
        assertThat(keys.isCompletelySet(), equalTo(false));
    }

    @Theory
    public void testSetFirstActive(KeyData data) {
        long[] rawKeys = data.getKeys(3);
        long k1 = rawKeys[0];
        long k2 = rawKeys[1];
        long k3 = rawKeys[2];
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        keys.setActive(keys.getIndex(k1), true);

        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.activeSetView(), contains(k1));

        assertThat(keys.getIndexIfActive(k1), equalTo(0));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndexIfActive(k2), lessThan(0));
        assertThat(keys.isCompletelySet(), equalTo(false));
    }

    @Theory
    public void testSetSomeActive(KeyData data) {
        long[] rawKeys = data.getKeys(5);

        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(2));
        assertThat(keys.activeSetView(), contains(rawKeys[1], rawKeys[3]));

        assertThat(keys.getIndexIfActive(rawKeys[0]), lessThan(0));
        assertThat(keys.getIndexIfActive(rawKeys[1]), equalTo(1));
        assertThat(keys.getIndexIfActive(rawKeys[2]), lessThan(0));
        assertThat(keys.getIndexIfActive(rawKeys[3]), equalTo(3));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.indexIsActive(3), equalTo(true));
        assertThat(keys.indexIsActive(4), equalTo(false));
        assertThat(keys.getIndexIfActive(rawKeys[2]), lessThan(0));
    }

    @Theory
    public void testInvert(KeyData data) {
        long[] rawKeys = data.getKeys(5);
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);
        keys.invert();

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), contains(rawKeys[0], rawKeys[2], rawKeys[4]));

        assertThat(keys.getIndexIfActive(rawKeys[0]), equalTo(0));
        assertThat(keys.getIndexIfActive(rawKeys[1]), lessThan(0));
        assertThat(keys.getIndexIfActive(rawKeys[2]), equalTo(2));
        assertThat(keys.getIndexIfActive(rawKeys[3]), lessThan(0));
        assertThat(keys.getIndexIfActive(rawKeys[4]), equalTo(4));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.indexIsActive(3), equalTo(false));
        assertThat(keys.indexIsActive(4), equalTo(true));
    }

    @Theory
    public void testInactiveCopy(KeyData data) {
        LongKeyDomain keys = LongKeyDomain.create(data.getKeys(4));
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.inactiveCopy();
        assertThat(ks2.activeSetView(), hasSize(0));
        assertThat(keys.activeSetView(), contains(data.getKey(0), data.getKey(1), data.getKey(3)));
    }

    @Theory
    public void testCloneCopy(KeyData data) {
        LongKeyDomain keys = LongKeyDomain.create(data.getKeys(4));
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.clone();
        assertThat(ks2.activeSetView(), hasSize(3));
        ks2.setActive(1, false);
        assertThat(ks2.activeSetView(), hasSize(2));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.activeSetView(), hasSize(3));
    }

    @Theory
    public void testOwnership(KeyData data) {
        LongKeyDomain keys = LongKeyDomain.create(data.getKeys(4));
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.unowned().clone();
        assertThat(ks2, sameInstance(keys));
        LongKeyDomain ks3 = keys.clone();
        assertThat(ks3, not(sameInstance(keys)));
        ks3.setActive(3, false);
        assertThat(keys.indexIsActive(3), equalTo(true));
    }
}
