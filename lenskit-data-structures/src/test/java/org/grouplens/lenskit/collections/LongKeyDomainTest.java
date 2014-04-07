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
        assertThat(keys.keyList(), hasSize(0));
        assertThat(keys.isCompletelySet(), equalTo(true));
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
}
