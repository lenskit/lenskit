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
package org.grouplens.lenskit.scored;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.grouplens.lenskit.collections.CopyingFastCollection;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Utility classes for working with {@linkplain ScoredId scored IDs}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 * @compat Public
 */
public final class ScoredIds {
    private ScoredIds() {}

    /**
     * Create a new builder initialized to copy the specified scored ID.
     * @param id The scored ID to copy.
     * @return A new builder that will copy the ID.
     */
    @SuppressWarnings("unchecked")
    public static ScoredIdBuilder copyBuilder(ScoredId id) {
        ScoredIdBuilder bld = new ScoredIdBuilder(id.getId(), id.getScore());
        for (Symbol chan: id.getUnboxedChannelSymbols()) {
            bld.addChannel(chan, id.getUnboxedChannelValue(chan));
        }
        for (@SuppressWarnings("rawtypes") SymbolValue chan: id.getChannels()) {
            if (!chan.getSymbol().getType().equals(Double.class)) {
                bld.addChannel(chan.getSymbol(), chan.getValue());
            }
        }
        return bld;
    }

    /**
     * Create a new builder.
     * @return A new scored ID builder.
     */
    public static ScoredIdBuilder newBuilder() {
        return new ScoredIdBuilder();
    }

    /**
     * Create a new list builder.
     * @return A new scored ID list builder.
     */
    public static ScoredIdListBuilder newListBuilder() {
        return new ScoredIdListBuilder();
    }

    /**
     * Create a new list builder.
     * @param cap The initial capacity to reserve.
     * @return A new scored ID list builder.
     */
    public static ScoredIdListBuilder newListBuilder(int cap) {
        return new ScoredIdListBuilder(cap);
    }

    /**
     * Create a scored id with no channels.
     * @param id The ID.
     * @param score The score.
     * @return An ID.
     */
    public static ScoredId create(long id, double score) {
        return new ScoredIdImpl(id, score);
    }

    //region Functions
    /**
     * A {@link Function} that extracts the ID from a scored ID.
     * @return A function returning the ID from any scored ID.
     */
    public static Function<ScoredId,Long> idFunction() {
        return IdFunction.INSTANCE;
    }

    private static enum IdFunction implements Function<ScoredId,Long> {
        INSTANCE {
            @Nullable
            @Override
            public Long apply(@Nullable ScoredId input) {
                if (input == null) {
                    return null;
                } else {
                    return input.getId();
                }
            }
        }
    }
    //endregion

    //region Ordering
    /**
     * An ordering (comparator) that compares IDs by score.
     * @return An ordering over {@link ScoredId}s by score.
     */
    public static Ordering<ScoredId> idOrder() {
        return ID_ORDER;
    }

    private static final Ordering<ScoredId> ID_ORDER = new IdOrder();

    private static final class IdOrder extends Ordering<ScoredId> {
        @Override
        @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
        public int compare(ScoredId left, ScoredId right) {
            return Longs.compare(left.getId(), right.getId());
        }
    }

    /**
     * An ordering (comparator) that compares IDs by score.
     * @return An ordering over {@link ScoredId}s by score.
     */
    public static Ordering<ScoredId> scoreOrder() {
        return SCORE_ORDER;
    }

    private static final Ordering<ScoredId> SCORE_ORDER = new ScoreOrder();

    private static final class ScoreOrder extends Ordering<ScoredId> {
        @Override
        @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
        public int compare(ScoredId left, ScoredId right) {
            return Doubles.compare(left.getScore(), right.getScore());
        }
    }

    /**
     * An ordering (comparator) that compares IDs by channel.  Missing channels are less than
     * present channels.
     * @param chan The channel to sort by.
     * @return An ordering over scored IDs by channel.
     */
    public static Ordering<ScoredId> channelOrder(final Symbol chan) {
        return new Ordering<ScoredId>() {
            @Override
            @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
            public int compare(ScoredId left, ScoredId right) {
                if (left.hasUnboxedChannel(chan)) {
                    if (right.hasUnboxedChannel(chan)) {
                        return Doubles.compare(left.getUnboxedChannelValue(chan),
                                               right.getUnboxedChannelValue(chan));
                    } else {
                        return 1;
                    }
                } else if (right.hasUnboxedChannel(chan)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     * An ordering (comparator) that compares IDs by typed channel.  Null values are sorted first.
     * This method calls {@link #channelOrder(TypedSymbol, Comparator)} with a comparator of
     * <code>{@linkplain Ordering#natural()}.{@linkplain Ordering#nullsFirst() nullsFirst()}</code>.
     * @param chan The channel to sort by.
     * @return A comparator over scored IDs.
     * @see #channelOrder(TypedSymbol, Comparator)
     */
    public static <T extends Comparable<? super T>> Ordering<ScoredId> channelOrder(final TypedSymbol<T> chan) {
        return channelOrder(chan, Ordering.natural().nullsFirst());
    }

    /**
     * An ordering (comparator) that compares IDs by typed channel.  Missing channels and null Ids
     * are both treated as having {@code null} channel values, so their ordering is controlled by
     * the order's null treatment.
     *
     * @param chan  The channel to sort by.
     * @param order The ordering to use.
     * @return A comparator over scored IDs.
     */
    public static <T> Ordering<ScoredId> channelOrder(final TypedSymbol<T> chan, final Comparator<? super T> order) {
        return new Ordering<ScoredId>() {
            @Override
            public int compare(@Nullable ScoredId left, @Nullable ScoredId right) {
                T v1 = null;
                T v2 = null;
                if (left != null && left.hasChannel(chan)) {
                    v1 = left.getChannelValue(chan);
                }
                if (right != null && right.hasChannel(chan)) {
                    v2 = right.getChannelValue(chan);
                }
                return order.compare(v1, v2);
            }
        };
    }
    //endregion

    //region Vector conversion
    /**
     * View a vector as a {@link org.grouplens.lenskit.collections.FastCollection} of {@link ScoredId} objects.
     *
     * @return A fast collection containing this vector's keys and values as
     * {@link ScoredId} objects.
     * @param vector The vector to view as a collection of {@link ScoredId}s
     */
    public static FastCollection<ScoredId> collectionFromVector(SparseVector vector) {
        return new VectorIdCollection(vector);
    }

    private static class VectorIdCollection extends CopyingFastCollection<ScoredId> {

        private final SparseVector vector;

        public VectorIdCollection(SparseVector v) {
            vector = v;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected ScoredId copy(ScoredId elt) {
            ScoredIdBuilder builder = new ScoredIdBuilder();
            builder.setId(elt.getId());
            builder.setScore(elt.getScore());
            for (DoubleSymbolValue chan: elt.getUnboxedChannels()) {
                builder.addChannel(chan.getRawSymbol(),
                                   chan.getDoubleValue());
            }
            for (SymbolValue chan: elt.getChannels()) {
                if (!chan.getSymbol().getType().equals(Double.class)) {
                    builder.addChannel(chan.getSymbol(),
                                       chan.getValue());
                }
            }
            return builder.build();
        }

        @Override
        public int size() {
            return vector.size();
        }

        @Override
        public Iterator<ScoredId> fastIterator() {
            return new VectorIdIter(vector);
        }
    }

    private static class VectorIdIter implements Iterator<ScoredId> {

        private final SparseVector vector;
        private Iterator<VectorEntry> entIter;
        private VectorEntryScoredId id;

        public VectorIdIter(SparseVector v) {
            vector = v;
            entIter = vector.fastIterator();
            id = new VectorEntryScoredId(vector);
        }

        @Override
        public boolean hasNext() {
            return entIter.hasNext();
        }

        @Override
        public ScoredId next() {
            id.setEntry(entIter.next());
            return id;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    //endregion
}
