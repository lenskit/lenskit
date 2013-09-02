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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.LongLists;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongKeyDomainTest {
    @Test
    public void testEmptyArray() {
        long[] rawKeys = {};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 0, true);
        assertThat(keys.domainSize(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(0));
        assertThat(keys.getIndex(42), lessThan(0));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
    }

    @Test
    public void testEmptyCollection() {
        LongKeyDomain keys = LongKeyDomain.fromCollection(LongLists.EMPTY_LIST, true);
        assertThat(keys.domainSize(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(0));
    }

    @Test
    public void testSingleton() {
        long[] rawKeys = {42};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 1, true);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.activeSetView(), hasSize(1));

        assertThat(keys.getIndex(42), equalTo(0));
        assertThat(keys.getIndexIfActive(42), equalTo(0));
        assertThat(keys.getIndex(39), lessThan(0));
        assertThat(keys.getIndex(68), lessThan(0));
        assertThat(keys.getIndexIfActive(39), lessThan(0));
        assertThat(keys.getIndexIfActive(68), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.keyIsActive(42), equalTo(true));
    }

    @Test
    public void testSingletonUnset() {
        long[] rawKeys = {42};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 1, false);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(42), equalTo(0));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
        assertThat(keys.getIndex(39), lessThan(0));
        assertThat(keys.getIndex(68), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.keyIsActive(42), equalTo(false));
    }

    @Test
    public void testMultiple() {
        long[] rawKeys = {39, 42, 62};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));

        assertThat(keys.getIndex(39), equalTo(0));
        assertThat(keys.getIndex(42), equalTo(1));
        assertThat(keys.getIndex(62), equalTo(2));
        assertThat(keys.getIndexIfActive(42), equalTo(1));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.getIndex(30), lessThan(0));
        assertThat(keys.getIndex(40), lessThan(0));
        assertThat(keys.getIndexIfActive(40), lessThan(0));
        assertThat(keys.getIndex(50), lessThan(0));
        assertThat(keys.getIndex(80), lessThan(0));

        assertThat(keys.keyIsActive(42), equalTo(true));
    }

    @Test
    public void testMultipleUnset() {
        long[] rawKeys = {39, 42, 62};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(39), equalTo(0));
        assertThat(keys.getIndex(42), equalTo(1));
        assertThat(keys.getIndex(62), equalTo(2));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndex(30), lessThan(0));
        assertThat(keys.getIndex(40), lessThan(0));
        assertThat(keys.getIndex(50), lessThan(0));
        assertThat(keys.getIndex(80), lessThan(0));

        assertThat(keys.keyIsActive(42), equalTo(false));
    }

    @Test
    public void testSetAllActive() {
        long[] rawKeys = {39, 42, 62};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.activeSetView(), hasSize(0));

        keys.setAllActive(true);
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));

        assertThat(keys.getIndex(39), equalTo(0));
        assertThat(keys.getIndex(42), equalTo(1));
        assertThat(keys.getIndex(62), equalTo(2));
        assertThat(keys.getIndexIfActive(42), equalTo(1));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.getIndex(30), lessThan(0));
        assertThat(keys.getIndex(40), lessThan(0));
        assertThat(keys.getIndex(50), lessThan(0));
        assertThat(keys.getIndex(80), lessThan(0));

        assertThat(keys.keyIsActive(42), equalTo(true));
    }

    @Test
    public void testSetAllInactive() {
        long[] rawKeys = {39, 42, 62};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), equalTo(keys.domain()));


        keys.setAllActive(false);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.activeSetView(), hasSize(0));

        assertThat(keys.getIndex(39), equalTo(0));
        assertThat(keys.getIndex(42), equalTo(1));
        assertThat(keys.getIndex(62), equalTo(2));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndex(30), lessThan(0));
        assertThat(keys.getIndex(40), lessThan(0));
        assertThat(keys.getIndex(50), lessThan(0));
        assertThat(keys.getIndex(80), lessThan(0));

        assertThat(keys.keyIsActive(42), equalTo(false));
    }

    @Test
    public void testSetFirstActive() {
        long[] rawKeys = {39, 42, 62};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 3, false);
        keys.setActive(keys.getIndex(39), true);

        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.activeSetView(), contains(39L));

        assertThat(keys.getIndexIfActive(39), equalTo(0));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
    }

    @Test
    public void testSetSomeActive() {
        long[] rawKeys = {39, 42, 62, 63, 70};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(2));
        assertThat(keys.activeSetView(), contains(42L, 63L));

        assertThat(keys.getIndexIfActive(39), lessThan(0));
        assertThat(keys.getIndexIfActive(42), equalTo(1));
        assertThat(keys.getIndexIfActive(62), lessThan(0));
        assertThat(keys.getIndexIfActive(63), equalTo(3));
        assertThat(keys.indexIsActive(0), equalTo(false));
        assertThat(keys.indexIsActive(1), equalTo(true));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.indexIsActive(3), equalTo(true));
        assertThat(keys.indexIsActive(4), equalTo(false));
        assertThat(keys.getIndexIfActive(62), lessThan(0));
    }

    @Test
    public void testInvert() {
        long[] rawKeys = {39, 42, 62, 63, 70};
        LongKeyDomain keys = LongKeyDomain.wrap(rawKeys, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);
        keys.invert();

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.activeSetView(), contains(39L, 62L, 70L));

        assertThat(keys.getIndexIfActive(39), equalTo(0));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
        assertThat(keys.getIndexIfActive(62), equalTo(2));
        assertThat(keys.getIndexIfActive(63), lessThan(0));
        assertThat(keys.getIndexIfActive(70), equalTo(4));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(true));
        assertThat(keys.indexIsActive(3), equalTo(false));
        assertThat(keys.indexIsActive(4), equalTo(true));
    }

    @Test
    public void testEmptyUpperBound() {
        LongKeyDomain keys = LongKeyDomain.empty();
        assertThat(keys.upperBound(0), equalTo(0));
    }

    @Test
    public void testSingletonUpperBound() {
        LongKeyDomain keys = LongKeyDomain.create(5);
        assertThat(keys.upperBound(0), equalTo(0));
        assertThat(keys.upperBound(5), equalTo(1));
        assertThat(keys.upperBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysUpperBound() {
        LongKeyDomain keys = LongKeyDomain.create(5, 6, 8);
        assertThat(keys.upperBound(0), equalTo(0));
        assertThat(keys.upperBound(5), equalTo(1));
        assertThat(keys.upperBound(6), equalTo(2));
        assertThat(keys.upperBound(7), equalTo(2));
        assertThat(keys.upperBound(8), equalTo(3));
        assertThat(keys.upperBound(10), equalTo(3));
    }

    @Test
    public void testEmptyLowerBound() {
        LongKeyDomain keys = LongKeyDomain.empty();
        assertThat(keys.lowerBound(0), equalTo(0));
    }

    @Test
    public void testSingletonLowerBound() {
        LongKeyDomain keys = LongKeyDomain.create(5);
        assertThat(keys.lowerBound(0), equalTo(0));
        assertThat(keys.lowerBound(5), equalTo(0));
        assertThat(keys.lowerBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysLowerBound() {
        LongKeyDomain keys = LongKeyDomain.create(5, 6, 8);
        assertThat(keys.lowerBound(0), equalTo(0));
        assertThat(keys.lowerBound(5), equalTo(0));
        assertThat(keys.lowerBound(6), equalTo(1));
        assertThat(keys.lowerBound(7), equalTo(2));
        assertThat(keys.lowerBound(8), equalTo(2));
        assertThat(keys.lowerBound(10), equalTo(3));
    }

    @Test
    public void testInactiveCopy() {
        LongKeyDomain keys = LongKeyDomain.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.inactiveCopy();
        assertThat(ks2.activeSetView(), hasSize(0));
        assertThat(keys.activeSetView(), contains(0L, 1L, 3L));
    }

    @Test
    public void testCloneCopy() {
        LongKeyDomain keys = LongKeyDomain.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.clone();
        assertThat(ks2.activeSetView(), hasSize(3));
        ks2.setActive(1, false);
        assertThat(keys.indexIsActive(1), equalTo(true));
    }

    @Test
    public void testOwnership() {
        LongKeyDomain keys = LongKeyDomain.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeyDomain ks2 = keys.unowned().clone();
        assertThat(ks2, sameInstance(keys));
        LongKeyDomain ks3 = keys.clone();
        assertThat(ks3, not(sameInstance(keys)));
        ks3.setActive(3, false);
        assertThat(keys.indexIsActive(3), equalTo(true));
    }
}
