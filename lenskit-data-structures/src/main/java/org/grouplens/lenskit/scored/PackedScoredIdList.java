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
package org.grouplens.lenskit.scored;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * A space-efficient, unboxed list of scored IDs.  These lists are immutable; create them using a
 * {@link ScoredIdListBuilder} (see {@link ScoredIds#newListBuilder()}.
 *
 * @since 1.4
 * @compat Public
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PackedScoredIdList extends AbstractList<ScoredId> implements FastCollection<ScoredId>, Serializable {
    private static final long serialVersionUID = 1L;
    private final long[] ids;
    private final double[] scores;
    private final Map<Symbol,double[]> unboxedChannels;
    private final Map<TypedSymbol<?>,Object[]> channels;

    PackedScoredIdList(long[] ids, double[] scores,
                       Map<TypedSymbol<?>, Object[]> chans,
                       Map<Symbol, double[]> unboxedChans) {
        assert ids.length == scores.length;
        this.ids = ids;
        this.scores = scores;
        unboxedChannels = unboxedChans;
        channels = chans;
    }

    /**
     * Do some light validation of the scored ID list.
     * @param in The input stream
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (ids.length != scores.length) {
            throw new InvalidObjectException("score array has incorrect size");
        }
        for (double[] chan: unboxedChannels.values()) {
            if (chan.length != ids.length) {
                throw new InvalidObjectException("channel array has incorrect size");
            }
        }
        for (Map.Entry<TypedSymbol<?>,Object[]> tc: channels.entrySet()) {
            if (tc.getValue().length != ids.length) {
                throw new InvalidObjectException("channel array has incorrect size");
            }
            if (!tc.getKey().getType().isAssignableFrom(tc.getValue().getClass().getComponentType())) {
                throw new InvalidObjectException("channel array has incorrect type");
            }
        }
    }

    @Override
    public int size() {
        return ids.length;
    }

    @Override
    public Iterator<ScoredId> fastIterator() {
        return new FastIter();
    }

    @Override
    public ScoredId get(int i) {
        Preconditions.checkElementIndex(i, size());
        return getFlyweight(i);
    }

    /**
     * Get a flyweight id from the list, at the specified index.
     * @param i The index.  No validation is performed of the index; the scored id will fail if the
     *          index is out-of-bounds.  This is to allow other classes in the package (such as the
     *          builder) to have unlimited access to scored ids.
     * @return A flyweight at the specified position.
     */
    IndirectScoredId getFlyweight(int i) {
        return new IndirectScoredId(i);
    }

    /**
     * Flyweight implementation of {@link ScoredId} backed by the list's storage.
     */
    class IndirectScoredId extends AbstractScoredId implements Serializable {
        private int index;

        public IndirectScoredId(int idx) {
            index = idx;
        }

        public Object writeReplace() {
            return ScoredIds.copyBuilder(this).build();
        }

        public void setIndex(int idx) {
            index = idx;
        }

        @Override
        public long getId() {
            return ids[index];
        }

        @Override
        public double getScore() {
            return scores[index];
        }

        @Override
        public Set<Symbol> getUnboxedChannelSymbols() {
            return unboxedChannels.keySet();
        }

        @Override
        public Set<TypedSymbol<?>> getChannelSymbols() {
            return channels.keySet();
        }

        @Nonnull
        @Override
        public Collection<SymbolValue<?>> getChannels() {
            // FIXME Make this fast
            List<SymbolValue<?>> channels = Lists.newArrayList();
            FluentIterable.from(PackedScoredIdList.this.channels.entrySet())
                    .transform(new Function<Map.Entry<TypedSymbol<?>, Object[]>, SymbolValue<?>>() {
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        @Nullable
                        @Override
                        public SymbolValue<?> apply(@Nullable Map.Entry<TypedSymbol<?>, Object[]> input) {
                            assert input != null;
                            Object obj = input.getValue()[index];
                            if (obj != null) {
                                TypedSymbol sym = input.getKey();
                                assert sym.getType().isInstance(obj);
                                return sym.withValue(obj);
                            } else {
                                return null;
                            }
                        }
                    }).filter(Predicates.notNull())
                    .copyInto(channels);
            channels.addAll(getUnboxedChannels());
            return channels;
        }

        @Nonnull
        @Override
        public Collection<DoubleSymbolValue> getUnboxedChannels() {
            return Collections2.transform(
                    unboxedChannels.entrySet(),
                    new Function<Map.Entry<Symbol, double[]>, DoubleSymbolValue>() {
                        @Nullable
                        @Override
                        public DoubleSymbolValue apply(@Nullable Map.Entry<Symbol, double[]> input) {
                            assert input != null;
                            return SymbolValue.of(input.getKey(), input.getValue()[index]);
                        }
                    });
        }

        @Nullable
        @Override
        public <T> T getChannelValue(@Nonnull TypedSymbol<T> sym) {
            Object[] array = channels.get(sym);
            if (array == null) {
                return null;
            } else {
                return sym.getType().cast(array[index]);
            }
        }

        @Override
        public double getUnboxedChannelValue(Symbol sym) {
            double[] array = unboxedChannels.get(sym);
            if (array != null) {
                return array[index];
            } else {
                throw new NullPointerException("no symbol " + sym);
            }
        }

        @Override
        public boolean hasUnboxedChannel(Symbol s) {
            return unboxedChannels.containsKey(s);
        }

        @Override
        public boolean hasChannel(TypedSymbol<?> s) {
            Object[] obj = channels.get(s);
            return obj != null && obj[index] != null;
        }
    }

    /**
     * Fast iterator implementation using a mutable flyweight.
     */
    private class FastIter implements Iterator<ScoredId> {
        int next = 0;
        IndirectScoredId id = new IndirectScoredId(0);

        @Override
        public boolean hasNext() {
            return next < ids.length;
        }

        @Override
        public ScoredId next() {
            if (next < ids.length) {
                id.setIndex(next);
                next++;
                return id;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("packed scored ID lists are immutable");
        }
    }
}
