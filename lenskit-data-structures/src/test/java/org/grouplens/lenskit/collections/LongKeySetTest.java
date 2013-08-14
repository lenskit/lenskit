package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test long key sets.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LongKeySetTest {
    @Test
    public void testEmptyArray() {
        long[] rawKeys = {};
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 0, true);
        assertThat(keys.domainSize(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(0));
        assertThat(keys.getIndex(42), lessThan(0));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
    }

    @Test
    public void testEmptyCollection() {
        LongKeySet keys = LongKeySet.fromCollection(LongLists.EMPTY_LIST, true);
        assertThat(keys.domainSize(), equalTo(0));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(0));
    }

    @Test
    public void testSingleton() {
        long[] rawKeys = {42};
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 1, true);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.asSet(), hasSize(1));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 1, false);
        assertThat(keys.domainSize(), equalTo(1));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), hasSize(1));
        assertThat(keys.asSet(), hasSize(0));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.asSet(), equalTo(keys.domain()));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.asSet(), hasSize(0));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 3, false);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.asSet(), hasSize(0));

        keys.setAllActive(true);
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.asSet(), equalTo(keys.domain()));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 3, true);
        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.domain(), contains(39L, 42L, 62L));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.asSet(), equalTo(keys.domain()));


        keys.setAllActive(false);
        assertThat(keys.size(), equalTo(0));
        assertThat(keys.asSet(), hasSize(0));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 3, false);
        keys.setActive(keys.getIndex(39), true);

        assertThat(keys.domainSize(), equalTo(3));
        assertThat(keys.size(), equalTo(1));
        assertThat(keys.asSet(), contains(39L));

        assertThat(keys.getIndexIfActive(39), equalTo(0));
        assertThat(keys.indexIsActive(0), equalTo(true));
        assertThat(keys.indexIsActive(1), equalTo(false));
        assertThat(keys.indexIsActive(2), equalTo(false));
        assertThat(keys.getIndexIfActive(42), lessThan(0));
    }

    @Test
    public void testSetSomeActive() {
        long[] rawKeys = {39, 42, 62, 63, 70};
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(2));
        assertThat(keys.asSet(), contains(42L, 63L));

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
        LongKeySet keys = LongKeySet.wrap(rawKeys, 0, 5, false);
        keys.setActive(1, true);
        keys.setActive(3, true);
        keys.invert();

        assertThat(keys.domainSize(), equalTo(5));
        assertThat(keys.size(), equalTo(3));
        assertThat(keys.asSet(), contains(39L, 62L, 70L));

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
        LongKeySet keys = LongKeySet.empty();
        assertThat(keys.upperBound(0), equalTo(0));
    }

    @Test
    public void testSingletonUpperBound() {
        LongKeySet keys = LongKeySet.create(5);
        assertThat(keys.upperBound(0), equalTo(0));
        assertThat(keys.upperBound(5), equalTo(1));
        assertThat(keys.upperBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysUpperBound() {
        LongKeySet keys = LongKeySet.create(5, 6, 8);
        assertThat(keys.upperBound(0), equalTo(0));
        assertThat(keys.upperBound(5), equalTo(1));
        assertThat(keys.upperBound(6), equalTo(2));
        assertThat(keys.upperBound(7), equalTo(2));
        assertThat(keys.upperBound(8), equalTo(3));
        assertThat(keys.upperBound(10), equalTo(3));
    }

    @Test
    public void testEmptyLowerBound() {
        LongKeySet keys = LongKeySet.empty();
        assertThat(keys.lowerBound(0), equalTo(0));
    }

    @Test
    public void testSingletonLowerBound() {
        LongKeySet keys = LongKeySet.create(5);
        assertThat(keys.lowerBound(0), equalTo(0));
        assertThat(keys.lowerBound(5), equalTo(0));
        assertThat(keys.lowerBound(7), equalTo(1));
    }

    @Test
    public void testSomeKeysLowerBound() {
        LongKeySet keys = LongKeySet.create(5, 6, 8);
        assertThat(keys.lowerBound(0), equalTo(0));
        assertThat(keys.lowerBound(5), equalTo(0));
        assertThat(keys.lowerBound(6), equalTo(1));
        assertThat(keys.lowerBound(7), equalTo(2));
        assertThat(keys.lowerBound(8), equalTo(2));
        assertThat(keys.lowerBound(10), equalTo(3));
    }

    @Test
    public void testEmptyPointer() {
        LongKeySet keys = LongKeySet.empty();
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(true));
        assertThat(ip.advance(), equalTo(false));
    }

    @Test
    public void testSingletonPointer() {
        LongKeySet keys = LongKeySet.create(3);
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(false));
        assertThat(ip.getInt(), equalTo(0));
        assertThat(ip.advance(), equalTo(false));
        assertThat(ip.isAtEnd(), equalTo(true));
    }

    @Test
    public void testMultiPointer() {
        LongKeySet keys = LongKeySet.create(3, 4, 5);
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(false));
        assertThat(ip.getInt(), equalTo(0));
        assertThat(ip.advance(), equalTo(true));
        assertThat(ip.isAtEnd(), equalTo(false));
        assertThat(ip.getInt(), equalTo(1));
        assertThat(ip.advance(), equalTo(true));
        assertThat(ip.getInt(), equalTo(2));
        assertThat(ip.advance(), equalTo(false));
        assertThat(ip.isAtEnd(), equalTo(true));
    }

    @Test
    public void testMaskedPointer() {
        LongKeySet keys = LongKeySet.create(3, 4, 5);
        keys.setActive(1, false);
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(false));
        assertThat(ip.getInt(), equalTo(0));
        assertThat(ip.advance(), equalTo(true));
        assertThat(ip.getInt(), equalTo(2));
        assertThat(ip.advance(), equalTo(false));
        assertThat(ip.isAtEnd(), equalTo(true));
    }

    @Test
    public void testMaskFirstPointer() {
        LongKeySet keys = LongKeySet.create(3, 4, 5);
        keys.setActive(0, false);
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(false));
        assertThat(ip.getInt(), equalTo(1));
        assertThat(ip.advance(), equalTo(true));
        assertThat(ip.getInt(), equalTo(2));
        assertThat(ip.advance(), equalTo(false));
        assertThat(ip.isAtEnd(), equalTo(true));
    }

    @Test
    public void testMaskAllPointer() {
        LongKeySet keys = LongKeySet.create(3, 4, 5);
        keys.setAllActive(false);
        IntPointer ip = keys.activeIndexPointer();
        assertThat(ip.isAtEnd(), equalTo(true));
        assertThat(ip.advance(), equalTo(false));
    }

    @Test
    public void testInactiveCopy() {
        LongKeySet keys = LongKeySet.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeySet ks2 = keys.inactiveCopy();
        assertThat(ks2.asSet(), hasSize(0));
        assertThat(keys.asSet(), contains(0L, 1L, 3L));
    }

    @Test
    public void testCloneCopy() {
        LongKeySet keys = LongKeySet.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeySet ks2 = keys.clone();
        assertThat(ks2.asSet(), hasSize(3));
        ks2.setActive(1, false);
        assertThat(keys.indexIsActive(1), equalTo(true));
    }

    @Test
    public void testOwnership() {
        LongKeySet keys = LongKeySet.create(0, 1, 2, 3);
        keys.setActive(2, false);
        LongKeySet ks2 = keys.unowned().clone();
        assertThat(ks2, sameInstance(keys));
        LongKeySet ks3 = keys.clone();
        assertThat(ks3, not(sameInstance(keys)));
        ks3.setActive(3, false);
        assertThat(keys.indexIsActive(3), equalTo(true));
    }
}
