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
package org.lenskit.util.keys;

import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class KeyedObjectMapTest {
    @Test
    public void testEmptyMap() {
        KeyedObjectMap<String> m = createMap();
        assertThat(m.size(), equalTo(0));
        assertThat(m.isEmpty(), equalTo(true));
        assertThat(m.get(42L), nullValue());

        assertThat(m.keySet(), hasSize(0));
        assertThat(m.values(), hasSize(0));
        assertThat(m.entrySet(), hasSize(0));

        try {
            m.firstLongKey();
            fail("firstLongKey should fail on empty map");
        } catch (NoSuchElementException e) {
            /* expected */
        }
        try {
            m.lastLongKey();
            fail("lastLongKey should fail on empty map");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testSingletonMap() {
        KeyedObjectMap<String> m = createMap("42");
        assertThat(m.size(), equalTo(1));
        assertThat(m.isEmpty(), equalTo(false));
        assertThat(m.get(42L), equalTo("42"));
        assertThat(m.get(39L), nullValue());
        assertThat(m.firstLongKey(), equalTo(42L));
        assertThat(m.lastLongKey(), equalTo(42L));

        assertThat(m.keySet(), hasSize(1));
        assertThat(m.values(), hasSize(1));
        assertThat(m.entrySet(), hasSize(1));
        assertThat(m.keySet(), contains(42L));
        assertThat(m.values(), contains("42"));
    }

    @Test
    public void testSomeItems() {
        KeyedObjectMap<String> m = createMap("37", "59", "42", "67");

        assertThat(m.size(), equalTo(4));
        assertThat(m.keySet(), hasSize(4));
        assertThat(m.values(), hasSize(4));
        assertThat(m.entrySet(), hasSize(4));

        assertThat(m.get(42L), equalTo("42"));
        assertThat(m.get(37L), equalTo("37"));
        assertThat(m.get(59L), equalTo("59"));
        assertThat(m.get(67L), equalTo("67"));

        assertThat(m.containsKey(42L), equalTo(true));
        assertThat(m.containsKey(2L), equalTo(false));
        assertThat(m.containsKey(45L), equalTo(false));
        assertThat(m.get(45L), nullValue());

        assertThat(m.keySet(), contains(37L, 42L, 59L, 67L));
        assertThat(m.values(), contains("37", "42", "59", "67"));
    }

    @Test
    public void testSubMap() {
        KeyedObjectMap<String> m = createMap("37", "59", "42", "67");
        KeyedObjectMap<String> m2 = m.subMap(42L, 66L);
        assertThat(m2.size(), equalTo(2));
        assertThat(m2.keySet(), contains(42L, 59L));
        assertThat(m2.values(), contains("42", "59"));
        assertThat(m2.get(42L), equalTo("42"));
        assertThat(m2.get(37L), nullValue());
    }

    @Test
    public void testTailMap() {
        KeyedObjectMap<String> m = createMap("37", "59", "42", "67");
        KeyedObjectMap<String> m2 = m.tailMap(59L);
        assertThat(m2.size(), equalTo(2));
        assertThat(m2.keySet(), contains(59L, 67L));
        assertThat(m2.values(), contains("59", "67"));
        assertThat(m2.get(59L), equalTo("59"));
        assertThat(m2.get(42L), nullValue());
    }

    @Test
    public void testHeadMap() {
        KeyedObjectMap<String> m = createMap("37", "59", "42", "67");
        KeyedObjectMap<String> m2 = m.headMap(59L);
        assertThat(m2.size(), equalTo(2));
        assertThat(m2.keySet(), contains(37L, 42L));
        assertThat(m2.values(), contains("37", "42"));
        assertThat(m2.get(59L), nullValue());
        assertThat(m2.get(42L), equalTo("42"));
    }

    static KeyedObjectMap<String> createMap(String... strings) {
        return new KeyedObjectMap<>(Arrays.asList(strings), StringEx.INSTANCE);
    }

    enum StringEx implements KeyExtractor<String> {
        INSTANCE {
            @Override
            public long getKey(String obj) {
                return Long.parseLong(obj);
            }
        }
    }
}