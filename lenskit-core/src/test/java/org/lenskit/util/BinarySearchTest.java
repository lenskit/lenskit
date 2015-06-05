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
package org.lenskit.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someNonEmptyLists;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSortedLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BinarySearchTest {
    @Test
    public void testEmptySearch() {
        BinarySearch search = BinarySearch.forList("foo", ImmutableList.<String>of());
        assertThat(search.search(0, 0), equalTo(-1));
        search = BinarySearch.forList("foo", ImmutableList.of("bar"));
        assertThat(search.search(0, 0), equalTo(-1));
        search = BinarySearch.forList("foo", ImmutableList.of("foo"));
        assertThat(search.search(0, 0), equalTo(-1));
    }

    @Test
    public void testFindOne() {
        BinarySearch search = BinarySearch.forList("foo", ImmutableList.of("foo"));
        assertThat(search.search(0, 1), equalTo(0));
    }

    @Test
    public void testNotFindOneEnd() {
        BinarySearch search = BinarySearch.forList("foo", ImmutableList.of("bar"));
        assertThat(search.search(0, 1), equalTo(-2));
    }

    @Test
    public void testNotFindOneBegin() {
        BinarySearch search = BinarySearch.forList("foo", ImmutableList.of("wumpus"));
        assertThat(search.search(0, 1), equalTo(-1));
    }

    @Test
    public void testFindMiddle() {
        BinarySearch search = BinarySearch.forList("b", ImmutableList.of("a", "b", "c"));
        assertThat(search.search(0, 3), equalTo(1));
    }

    @Test
    public void testFindMissingInMiddle() {
        BinarySearch search = BinarySearch.forList("c", ImmutableList.of("a", "b", "d"));
        assertThat(search.search(0, 3), equalTo(-3));
    }

    @Test
    public void testFindMissingAtEnd() {
        BinarySearch search = BinarySearch.forList("f", ImmutableList.of("a", "b", "d"));
        assertThat(search.search(0, 3), equalTo(-4));
    }

    @Test
    public void testFindMissingAtStart() {
        BinarySearch search = BinarySearch.forList("A", ImmutableList.of("a", "b", "d"));
        assertThat(search.search(0, 3), equalTo(-1));
    }

    @Test
    public void testFindFirst() {
        BinarySearch search = BinarySearch.forList("b", ImmutableList.of("a", "b", "b", "d"));
        assertThat(search.search(0, 3), equalTo(1));
    }

    @Test
    public void testRandomSearchesPresent() {
        for (List<Long> keys: someSortedLists(longs())) {
            List<Long> deduped = Lists.newArrayList(Sets.newLinkedHashSet(keys));
            long key = integers(0, deduped.size()).next();
            BinarySearch search = BinarySearch.forList(key, deduped);
            assertThat(search.search(0, keys.size()),
                       equalTo(Collections.binarySearch(deduped, key)));
        }
    }

    @Test
    public void testRandomSearches() {
        for (List<Long> keys: someSortedLists(longs())) {
            List<Long> deduped = Lists.newArrayList(Sets.newLinkedHashSet(keys));
            long key = longs().next();
            BinarySearch search = BinarySearch.forList(key, deduped);
            int rv = search.search(0, keys.size());
            assertThat(search.search(0, keys.size()),
                       equalTo(Collections.binarySearch(deduped, key)));
            int idx = BinarySearch.resultToIndex(rv);
            if (deduped.isEmpty()) {
                assertThat(idx, equalTo(0));
            } else if (idx == deduped.size()) {
                assertThat(key, greaterThan(deduped.get(deduped.size() - 1)));
            } else {
                assertThat(key, lessThanOrEqualTo(deduped.get(idx)));
            }
        }
    }

    /**
     * Use random lists to test that we always find the *first* matching element.
     */
    @Test
    public void testRandomSearchesFindFirst() {
        for (List<String> keys: someNonEmptyLists(strings())) {
            Collections.sort(keys);
            // pick an element to duplicate
            int toDup = integers(0, keys.size() - 1).next();
            // and # of times to duplicate
            int ndups = integers(0, 10).next();
            String dupKey = keys.get(toDup);
            for (int i = 0; i < ndups; i++) {
                keys.add(toDup, dupKey);
            }
            BinarySearch search = BinarySearch.forList(dupKey, keys);
            int rv = search.search(0, keys.size());
            assertThat(rv, greaterThanOrEqualTo(0));
            assertThat(search.search(0, keys.size()),
                       lessThanOrEqualTo(Collections.binarySearch(keys, dupKey)));
            assertThat(keys.get(rv), equalTo(dupKey));
            if (rv > 0) {
                // this is the first one
                assertThat(keys.get(rv-1), lessThan(dupKey));
            }
        }
    }

    @Test
    public void testRandomSubsetSearches() {
        // Test over some random lists, from 10 to 150 items long
        // Use bytes, so we have a decent probability of picking a key in the list
        for (List<Byte> keys: someSortedLists(bytes(), 10, 50)) {
            // Randomly pick a search key
            byte key = bytes().next();
            BinarySearch search = BinarySearch.forList(key, keys);
            // Randomly pick start and end positions within the list
            int start = integers(0, keys.size()).next();
            int end = integers(start, keys.size()).next();

            // Search...
            int rv = search.search(start, end);
            int idx = BinarySearch.resultToIndex(rv);

            // and make sure the result is right
            assertThat(idx, greaterThanOrEqualTo(start));
            assertThat(idx, lessThanOrEqualTo(end));
            if (rv >= 0) {
                assertThat(rv, lessThan(end));
                assertThat(keys.get(rv), equalTo(key));
            } else {
                if (idx < end) {
                    assertThat(keys.get(idx), greaterThan(key));
                }
                if (idx > start) {
                    assertThat(keys.get(idx - 1), lessThan(key));
                }
            }
        }
    }
}
