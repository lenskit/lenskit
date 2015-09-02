package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class FrozenHashKeyIndexTest {
    @Test
    public void testEmpty() {
        KeyIndex empty = new FrozenHashKeyIndex(LongLists.EMPTY_LIST);
        assertThat(empty.getKeyList(), hasSize(0));
        assertThat(empty.containsKey(30), equalTo(false));
        assertThat(empty.tryGetIndex(30), equalTo(-1));
        try {
            empty.getIndex(30);
            fail("getting absent index should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testSingleton() {
        KeyIndex idx = new FrozenHashKeyIndex(LongLists.singleton(42));
        assertThat(idx.getKeyList(), hasSize(1));
        assertThat(idx.getKeyList(), contains(42L));
        assertThat(idx.containsKey(30), equalTo(false));
        assertThat(idx.containsKey(42), equalTo(true));
        assertThat(idx.tryGetIndex(30), equalTo(-1));
        assertThat(idx.tryGetIndex(42), equalTo(0));
        assertThat(idx.getIndex(42), equalTo(0));
        assertThat(idx.getKey(0), equalTo(42L));
        try {
            idx.getIndex(30);
            fail("getting absent index should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testFailsWithDuplicates() {
        LongList keys = new LongArrayList();
        keys.add(10);
        keys.add(42);
        keys.add(5);
        keys.add(10);
        try {
            KeyIndex idx = new FrozenHashKeyIndex(keys);
            fail("creating key index with duplicates should fail");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }
}
