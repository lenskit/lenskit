package org.grouplens.lenskit.collections;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test long sorted array set.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RemovableLongSortedArraySetTest {
    @Test
    public void testEmptySet() {
        LongSet ls = LongKeyDomain.empty().modifiableActiveSetView();
        assertThat(ls.remove(42), equalTo(false));
    }

    @Test
    public void testRemovePresent() {
        LongKeyDomain lks = LongKeyDomain.create(42);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.remove(42), equalTo(true));
        assertThat(ls.size(), equalTo(0));
        assertThat(lks.size(), equalTo(0));
        assertThat(lks.keyIsActive(42), equalTo(false));
    }

    @Test
    public void testRemoveNotPResent() {
        LongKeyDomain lks = LongKeyDomain.create(42);
        LongSet ls = lks.modifiableActiveSetView();
        assertThat(ls.remove(39), equalTo(false));
        assertThat(ls.size(), equalTo(1));
        assertThat(lks.size(), equalTo(1));
        assertThat(lks.keyIsActive(42), equalTo(true));
    }

    @Test
    public void testRemoveAll() {
        LongKeyDomain lks = LongKeyDomain.create(20, 25, 30, 42, 62);
        LongSet ls = lks.modifiableActiveSetView();
        List<Long> rm = Longs.asList(20, 25, 62, 30, 98, 1);
        assertThat(ls.removeAll(rm), equalTo(true));
        assertThat(ls, contains(42L));
        assertThat(lks.size(), equalTo(1));
    }

    @Test
    public void testRetainAll() {
        LongKeyDomain lks = LongKeyDomain.create(20, 25, 30, 42, 62);
        LongSet ls = lks.modifiableActiveSetView();
        List<Long> rm = Longs.asList(20, 25, 62, 30, 98, 1);
        assertThat(ls.retainAll(rm), equalTo(true));
        assertThat(ls, contains(20L, 25L, 30L, 62L));
        assertThat(Lists.newArrayList(lks.activeIndexIterator()),
                   contains(0, 1, 2, 4));
    }
}
