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
package org.lenskit.util.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.LongSortedArraySet;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Utilities for working with longs and collections of them from Fastutil.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
@ParametersAreNonnullByDefault
public final class LongUtils {
    private LongUtils() {}

    /**
     * Create a frozen long-to-double map.  This effectively creates a copy of a map, but if the provided map is an
     * instance of {@link org.lenskit.util.keys.Long2DoubleSortedArrayMap}, which is immutable, it is returned as-is
     * for efficiency.
     *
     * @param map The source map.
     * @return An immutable map with the same data as {@code map}.
     */
    public static Long2DoubleSortedMap frozenMap(Map<Long,Double> map) {
        if (map instanceof Long2DoubleSortedArrayMap) {
            return (Long2DoubleSortedMap) map;
        } else {
            return new Long2DoubleSortedArrayMap(map);
        }
    }

    /**
     * Create a frozen long set.  If the underlying collection is already an immutable sorted set (specifically, a
     * {@link LongSortedArraySet}, it is used as-is. Otherwise, it is copied into a sorted array set.
     *
     * This is equivalent to {@link #packedSet(Collection)}.
     *
     * @param longs The collection.
     * @return The sorted array set.
     * @see #packedSet(Collection)
     */
    public static LongSortedSet frozenSet(Collection<Long> longs) {
        return packedSet(longs);
    }

    /**
     * Pack longs into a sorted set.
     * @param longs A collection of longs.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedArraySet packedSet(Collection<Long> longs) {
        if (longs instanceof LongSortedArraySet) {
            return (LongSortedArraySet) longs;
        } else {
            return SortedKeyIndex.fromCollection(longs).keySet();
        }
    }

    /**
     * Pack longs into a sorted set.
     * @param longs An array of longs.  This array is copied, not wrapped.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedArraySet packedSet(long... longs) {
        return SortedKeyIndex.create(longs).keySet();
    }

    /**
     * Create a comparator that compares long keys by associated double values.
     * @param vals The value map.
     * @return A comparator that will compare keys by looking them up in a map.
     */
    public static LongComparator keyValueComparator(final Long2DoubleFunction vals) {
        return new AbstractLongComparator() {
            @Override
            public int compare(long k1, long k2) {
                double v1 = vals.containsKey(k1) ? vals.get(k1) : Double.NaN;
                double v2 = vals.containsKey(k2) ? vals.get(k2) : Double.NaN;
                return Doubles.compare(v1, v2);
            }
        };
    }

    /**
     * Create a flyweight vector with a key set and value function.
     * @param keys The key set.
     * @param valueFunc Function to compute keys from values.
     * @return The flyweight map.
     */
    public static Long2DoubleMap flyweightMap(LongSet keys, LongToDoubleFunction valueFunc) {
        return new FlyweightLong2DoubleMap(keys, valueFunc);
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
     * Wrap or cast a long-to-double map into Fastutil.
     * @param map The map.
     * @return A function backed by {@code map}, or {@code map} if it is a Fastutil map.
     */
    public static Long2DoubleMap asLong2DoubleMap(final Map<Long,Double> map) {
        if (map instanceof Long2DoubleMap) {
            return (Long2DoubleMap) map;
        } else {
            return new Long2DoubleMapWrapper(map);
        }
    }

    /**
     * Create a long-to-double function from a map, casting if appropriate. Useful to allow unboxed access to maps that
     * are really fastutil maps.
     * @param map The map.
     * @return A function backed by {@code map}, or {@code map} if it is a Fastutil map.
     * @deprecated see {@link #asLong2DoubleMap(Map)}
     */
    @Deprecated
    public static Long2DoubleMap asLong2DoubleFunction(final Map<Long,Double> map) {
        return asLong2DoubleMap(map);
    }

    public static LongList asLongList(List<Long> longs) {
       if (longs instanceof  LongList) {
           return (LongList) longs;
       } else {
           return new LongListWrapper(longs);
       }
    }

    /**
     * Create a map that maps a group of items to the same value.
     * @param keys The keys.
     * @param value The value.
     * @return A map that contains all `keys`, mapping each of them to `value`.
     */
    public static Long2DoubleMap constantDoubleMap(Set<Long> keys, double value) {
        // TODO Implement this using a flyweight wrapper
        SortedKeyIndex idx = SortedKeyIndex.fromCollection(keys);
        double[] values = new double[idx.size()];
        Arrays.fill(values, value);
        return Long2DoubleSortedArrayMap.wrap(idx, values);
    }

    /**
     * Get a Fastutil {@link LongSet} from a {@link java.util.Set} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     */
    public static LongSet asLongSet(@Nullable final Set<Long> longs) {
        if (longs == null) {
            return null;
        } else if (longs instanceof LongSet) {
            return (LongSet) longs;
        } else {
            return new LongSetWrapper(longs);
        }
    }

    /**
     * Get a Fastutil {@link LongSet} from a {@link java.util.Collection} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     */
    public static LongSet asLongSet(@Nullable final Collection<Long> longs) {
        if (longs == null) {
            return null;
        } else if (longs instanceof Set) {
            return asLongSet((Set<Long>) longs);
        } else {
            return SortedKeyIndex.fromCollection(longs).keySet();
        }
    }

    /**
     * Compute the ranks for a list of longs.
     * @param results The list of longs.
     * @return The map of ranks; its default return value will be -1.
     */
    public static Long2IntMap itemRanks(LongList results) {
        Long2IntMap ranks = new Long2IntOpenHashMap(results.size());
        ranks.defaultReturnValue(-1);
        LongListIterator iter = results.listIterator();
        while (iter.hasNext()) {
            int i = iter.nextIndex();
            long val = iter.nextLong();
            ranks.put(val, i);
        }
        return ranks;
    }

    /**
     * Compute the set difference of two sets.
     *
     * @param items   The initial set
     * @param exclude The items to remove
     * @return The elements of <var>items</var> that are not in <var>exclude</var>.
     */
    public static LongSortedSet setDifference(LongSet items, LongSet exclude) {
        long[] data = new long[items.size()];
        final LongIterator iter = items.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final long x = iter.nextLong();
            if (!exclude.contains(x)) {
                data[i++] = x;
            }
        }
        if (!(items instanceof LongSortedSet)) {
            Arrays.sort(data, 0, i);
        }
        // trim the array
        if (data.length * 2 > i * 3) {
            data = Arrays.copyOf(data, i);
        }
        return SortedKeyIndex.wrap(data, i).keySet();
    }

    /**
     * Compute the size of the intersection of two sets.
     * @param a The first set.
     * @param b The second set.
     * @return The size of the intersection of the two sets.
     */
    public static int intersectSize(LongSortedSet a, LongSortedSet b) {
        return countCommonItems(a, b, -1);
    }

    /**
     * Compute the size of the intersection of two sets.
     * @param a The first set.
     * @param b The second set.
     * @return The size of the intersection of the two sets.
     */
    public static int intersectSize(LongSet a, LongSet b) {
        if (a instanceof LongSortedSet && b instanceof LongSortedSet) {
            return intersectSize((LongSortedSet) a, (LongSortedSet) b);
        } else {
            int n = 0;
            LongIterator iter = a.iterator();
            while (iter.hasNext()) {
                long x = iter.nextLong();
                if (b.contains(x)) {
                    n += 1;
                }
            }
            return n;
        }
    }

    /**
     * Check if two sets have at least a given number of common items.
     * @param a The first set.
     * @param b The second set.
     * @param n The number of common items to require.
     * @return `true` if the two sets have at least `n` common items.
     */
    public static boolean hasNCommonItems(LongSortedSet a, LongSortedSet b, int n) {
        Preconditions.checkArgument(n >= 0, "common item count must be nonnegative");
        return n == 0 || countCommonItems(a, b, n) >= n;
    }

    /**
     * Count the common items in two sets.
     * @param a The first set.
     * @param b The second set.
     * @param max The maximum number of common items to count; negative means no limit.
     * @return The number of common items, or `max` if there are at least `max` common items.
     */
    private static int countCommonItems(LongSortedSet a, LongSortedSet b, int max) {
        LongIterator ait = a.iterator();
        LongIterator bit = b.iterator();
        boolean hasA = ait.hasNext();
        boolean hasB = bit.hasNext();
        long nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
        long nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
        int nshared = 0;
        while (hasA && hasB && (max < 0 || nshared < max)) {
            if (nextA < nextB) {
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
            } else if (nextB < nextA) {
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            } else {
                nshared += 1;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            }
        }
        return nshared;
    }

    /**
     * Compute the size of the union of two sets.
     * @param a The first set.
     * @param b The second set.
     * @return The size of the union of the two sets.
     */
    public static int unionSize(LongSortedSet a, LongSortedSet b) {
        return a.size() + b.size() - intersectSize(a, b);
    }

    /**
     * Compute the union of two sets.
     *
     * @param a The first set.
     * @param b The second set.
     * @return The elements of <var>items</var> that are not in <var>exclude</var>.
     */
    public static LongSortedSet setUnion(LongSet a, LongSet b) {
        if (a instanceof LongSortedSet && b instanceof LongSortedSet) {
            return setUnion((LongSortedSet) a, (LongSortedSet) b);
        } else {
            LongSet set = new LongOpenHashSet(a);
            set.addAll(b);
            return packedSet(set);
        }
    }

    /**
     * Compute the union of two sets.
     *
     * @param a The first set.
     * @param b The second set.
     * @return The elements of <var>items</var> that are not in <var>exclude</var>.
     */
    public static LongSortedSet setUnion(LongSortedSet a, LongSortedSet b) {
        long[] data = new long[unionSize(a, b)];

        LongIterator ait = a.iterator();
        LongIterator bit = b.iterator();
        boolean hasA = ait.hasNext();
        boolean hasB = bit.hasNext();
        long nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
        long nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
        int i = 0;
        while (hasA || hasB) {
            if (!hasB || nextA < nextB) {
                // use A
                data[i++] = nextA;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
            } else if (!hasA || nextB < nextA) {
                // use B
                data[i++] = nextB;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            } else {
                // they're both present and equal, use A but advance both
                // edge case: if one is missing but other is MAX_VALUE, it will go here
                // but that is fine, no harm will be done, as they're both MAX_VALUE
                data[i++] = nextA;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            }
        }
        assert i == data.length;

        return SortedKeyIndex.wrap(data, data.length).keySet();
    }

    /**
     * Compute the intersection of two sets.
     *
     * @param a The first set.
     * @param b The second set.
     * @return The elements present in both sets.
     */
    public static LongSortedSet setIntersect(LongSet a, LongSet b) {
        if (a instanceof LongSortedSet && b instanceof LongSortedSet) {
            return setIntersect((LongSortedSet) a, (LongSortedSet) b);
        } else if (a.size() <= b.size()) {
            LongArrayList longs = new LongArrayList(Math.min(a.size(), b.size()));
            LongIterator iter = a.iterator();
            while (iter.hasNext()) {
                long key = iter.nextLong();
                if (b.contains(key)) {
                    longs.add(key);
                }
            }
            return LongUtils.packedSet(longs);
        } else {
            return setIntersect(b, a);
        }
    }

    /**
     * Compute the intersection of two sets.
     *
     * @param a The first set.
     * @param b The second set.
     * @return The elements present in both sets.
     */
    public static LongSortedSet setIntersect(LongSortedSet a, LongSortedSet b) {
        long[] data = new long[Math.min(a.size(), b.size())];

        LongIterator ait = a.iterator();
        LongIterator bit = b.iterator();
        boolean hasA = ait.hasNext();
        boolean hasB = bit.hasNext();
        long nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
        long nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
        int i = 0;
        while (hasA && hasB) {
            if (nextA < nextB) {
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
            } else if (nextB < nextA) {
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            } else {
                // they're both present and equal, use A but advance both
                data[i++] = nextA;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            }
        }
        if (data.length > i + i / 2) {
            data = Arrays.copyOf(data, i);
        }

        return SortedKeyIndex.wrap(data, i).keySet();
    }

    /**
     * Selects a random subset of {@code n} longs from a given set of longs. If fewer than {@code n}
     * items can be selected the whole set is returned. 
     *
     *
     * @param set the set of items to select from
     * @param num The number of random items to add.
     * @param random a random number generator to be used.
     * @return An item selector that selects the items selected by {@code base} plus an additional
     * {@code nRandom} items.
     */
    public static LongSet randomSubset(LongSet set, int num, Random random) {
        return randomSubset(set, num, LongSortedSets.EMPTY_SET, random);
    }
    
    /**
     * Selects a random subset of {@code n} longs from a given set of longs such that no selected 
     * items is in a second set of longs. If fewer than {@code n} items can be selected the whole set is returned. 
     *
     *
     * @param set the set of items to select from
     * @param num The number of random items to add.
     * @param exclude a set of longs which must not be returned
     * @param rng a random number generator to be used.
     * @return An item selector that selects the items selected by {@code base} plus an additional
     * {@code nRandom} items.
     */
    public static LongSortedSet randomSubset(LongSet set, int num, LongSet exclude,
                                             Random rng) {
        LongSet initial = exclude;
        LongList selected = new LongArrayList(num);
        int n = 0;
        LongIterator iter = set.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            if (exclude.contains(item)) {
                continue;
            }
            // algorithm adapted from Wikipedia coverage of Fisher-Yates shuffle
            // https://en.wikipedia.org/wiki/Fisher-Yates_shuffle
            int j = rng.nextInt(n + 1);
            n = n + 1;
            if (j < num) {
                if (j == selected.size()) {
                    selected.add(item);
                } else {
                    long old = selected.getLong(j);
                    if (selected.size() ==  num) {
                        selected.set(num - 1, old);
                    } else {
                        selected.add(old);
                    }
                    selected.set(j, item);
                }
            }
        }
        return LongUtils.packedSet(selected);
    }

    public static <T, E> Collector<T,?,Long2ObjectMap<List<E>>> mapCollector(ToLongFunction<T> kf, Function<T, E> vf) {
        return new MapCollector<>(kf, vf);
    }

    private static class MapCollector<T, E> implements Collector<T, Long2ObjectMap<List<E>>, Long2ObjectMap<List<E>>> {
        private final ToLongFunction<T> keyFunction;
        private final Function<T, E> valueFunction;

        MapCollector(ToLongFunction<T> kf, Function<T, E> vf) {
            keyFunction = kf;
            valueFunction = vf;
        }

        @Override
        public Supplier<Long2ObjectMap<List<E>>> supplier() {
            return Long2ObjectOpenHashMap::new;
        }

        @Override
        public BiConsumer<Long2ObjectMap<List<E>>, T> accumulator() {
            return (m, v) -> {
                long k = keyFunction.applyAsLong(v);
                List<E> lst = m.get(k);
                if (lst == null) {
                    lst = new ArrayList<>();
                    m.put(k, lst);
                }
                lst.add(valueFunction.apply(v));
            };
        }

        @Override
        public BinaryOperator<Long2ObjectMap<List<E>>> combiner() {
            return (m1, m2) -> {
                for (Long2ObjectMap.Entry<List<E>> e2: m2.long2ObjectEntrySet()) {
                    List<E> l2 = e2.getValue();
                    List<E> l1 = m1.get(e2.getLongKey());
                    if (l1 == null) {
                        m1.put(e2.getLongKey(), l2);
                    } else {
                        l1.addAll(l2);
                    }
                }
                return m1;
            };
        }

        @Override
        public Function<Long2ObjectMap<List<E>>, Long2ObjectMap<List<E>>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
        }
    }
}
