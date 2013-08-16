package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Utilities for working with longs and collections of them from Fastutil.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class LongUtils {
    private LongUtils() {}

    /**
     * Pack longs into a sorted set.
     * @param longs A collection of longs.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedSet packedSet(Collection<Long> longs) {
        return LongKeyDomain.fromCollection(longs, true).activeSetView();
    }

    /**
     * Pack longs into a sorted set.
     * @param longs An array of longs.  This array is copied, not wrapped.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedSet packedSet(long... longs) {
        return LongKeyDomain.create(longs).activeSetView();
    }

    /**
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongCollection} from a {@link java.util.Collection} of longs.
     * This method simply casts the collection, if possible, and returns a
     * wrapper otherwise.
     *
     * @param longs A collection of longs.
     * @return The collection as a {@link it.unimi.dsi.fastutil.longs.LongCollection}.
     */
    public static LongCollection asLongCollection(final Collection<Long> longs) {
        if (longs instanceof LongCollection) {
            return (LongCollection) longs;
        } else {
            return new LongCollectionWrapper(longs);
        }
    }

    /**
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongSet} from a {@link java.util.Set} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link it.unimi.dsi.fastutil.longs.LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     */
    public static LongSet asLongSet(final Set<Long> longs) {
        if (longs == null) {
            return null;
        } else if (longs instanceof LongSet) {
            return (LongSet) longs;
        } else {
            return new LongSetWrapper(longs);
        }
    }

    /**
     * Wrapper class that implements a {@link LongCollection} by delegating to
     * a {@link Collection}.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    private static class LongCollectionWrapper implements LongCollection {
        protected final Collection<Long> base;

        LongCollectionWrapper(Collection<Long> b) {
            base = b;
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(long key) {
            return base.contains(key);
        }

        @Override
        public LongIterator iterator() {
            return LongIterators.asLongIterator(base.iterator());
        }

        @Override
        public boolean add(Long item) {
            return base.add(item);
        }

        @Override
        public boolean addAll(Collection<? extends Long> items) {
            return base.addAll(items);
        }

        @Override
        public void clear() {
            base.clear();
        }

        @Override
        public boolean contains(Object item) {
            return base.contains(item);
        }

        @Override
        public boolean containsAll(Collection<?> items) {
            return base.containsAll(items);
        }

        @Override
        public boolean isEmpty() {
            return base.isEmpty();
        }

        @Override
        public boolean remove(Object item) {
            return base.remove(item);
        }

        @Override
        public boolean removeAll(Collection<?> items) {
            return base.removeAll(items);
        }

        @Override
        public boolean retainAll(Collection<?> items) {
            return base.retainAll(items);
        }

        @Override
        public Object[] toArray() {
            return base.toArray();
        }

        /**
         * {@inheritDoc}
         *
         * @deprecated see {@link LongCollection#longIterator()}
         */
        @Override
        @Deprecated
        public LongIterator longIterator() {
            return iterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return base.toArray(a);
        }

        @Override
        public long[] toLongArray() {
            final long[] items = new long[size()];
            LongIterators.unwrap(iterator(), items);
            return items;
        }

        @Override
        public long[] toLongArray(long[] a) {
            long[] output = a;
            if (output.length < size()) {
                output = new long[size()];
            }
            final int sz = LongIterators.unwrap(iterator(), output);
            if (sz < output.length) {
                output = Arrays.copyOf(output, sz);
            }
            return output;
        }

        @Override
        public long[] toArray(long[] a) {
            return toLongArray(a);
        }

        @Override
        public boolean add(long key) {
            return base.add(key);
        }

        @Override
        public boolean rem(long key) {
            return base.remove(key);
        }

        @Override
        public boolean addAll(LongCollection c) {
            return base.addAll(c);
        }

        @Override
        public boolean containsAll(LongCollection c) {
            return base.containsAll(c);
        }

        @Override
        public boolean removeAll(LongCollection c) {
            return base.removeAll(c);
        }

        @Override
        public boolean retainAll(LongCollection c) {
            return base.retainAll(c);
        }
    }

    private static class LongSetWrapper extends LongCollectionWrapper implements LongSet {
        LongSetWrapper(Collection<Long> base) {
            super(base);
        }

        @Override
        public boolean remove(long k) {
            return super.rem(k);
        }

        @Override
        public boolean equals(Object obj) {
            return base.equals(obj);
        }

        @Override
        public int hashCode() {
            return base.hashCode();
        }
    }
}
