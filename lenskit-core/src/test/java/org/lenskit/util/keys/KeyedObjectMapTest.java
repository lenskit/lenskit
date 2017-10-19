/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

    @Test
    public void testAddDuplicates() {
        KeyedObjectMap<String> m = createMap("37", "59", "37");
        assertThat(m.size(), equalTo(2));
        assertThat(m.keySet(),
                   contains(37L, 59L));
    }

    @Test
    public void testContainsValue() {
        KeyedObjectMap<String> m = createMap("42", "39", "108");
        assertThat(m.containsValue("42"), equalTo(true));
        assertThat(m.containsValue(null), equalTo(false));
        assertThat(m.containsValue("108"), equalTo(true));
        assertThat(m.containsValue("107"), equalTo(false));
        assertThat(m.containsValue(107L), equalTo(false));
    }

    static KeyedObjectMap<String> createMap(String... strings) {
        return KeyedObjectMap.newBuilder(StringEx.INSTANCE)
                             .add(strings)
                             .build();
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
