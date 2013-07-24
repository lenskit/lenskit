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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.scored.PackedScoredIdList.*;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import java.util.*;

import static it.unimi.dsi.fastutil.Arrays.quickSort;
import static org.grouplens.lenskit.scored.PackedScoredIdList.*;

/**
 * Builder for packed lists of scored ids.
 *
 * @since 1.4
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ScoredIdListBuilder implements Builder<PackedScoredIdList> {
    private long[] ids;
    private double[] scores;
    private int size;
    private Map<Symbol,PackedChanBuilder> channels;
    private Map<TypedSymbol<?>,PackedTChanBuilder> typedChannels;

    public ScoredIdListBuilder() {
        this(10);
    }

    public ScoredIdListBuilder(int cap) {
        initialize(cap);
    }

    private void initialize(int cap) {
        ids = new long[cap];
        scores = new double[cap];
        size = 0;
        channels = Maps.newHashMap();
        typedChannels = Maps.newHashMap();
    }

    @Override
    public PackedScoredIdList build() {
        return finish(false);
    }

    /**
     * Destructive version of {@link #build()}, re-using storage if possible.  Future use of the
     * builder is impossible, and all memory used by it is released.
     *
     * @return The scored ID list.
     */
    public PackedScoredIdList finish() {
        return finish(true);
    }

    private PackedScoredIdList finish(boolean reuse) {
        ImmutableMap.Builder<Symbol, PackedChannel> cbld = ImmutableMap.builder();
        ImmutableMap.Builder<TypedSymbol<?>, PackedTypedChannel> tcbld = ImmutableMap.builder();
        if (size > 0) {
            for (PackedChanBuilder pcb: channels.values()) {
                double[] vs;
                vs = reuse && size == pcb.values.length ? pcb.values : Arrays.copyOf(pcb.values, size);
                BitSet mask = finishMask(pcb.used, reuse);
                PackedChannel chan = new FullPackedChannel(pcb.symbol, vs, mask);
                cbld.put(pcb.symbol, chan);
            }
            for (PackedTChanBuilder pcb: typedChannels.values()) {
                Object[] vs;
                vs = reuse && size == pcb.values.length ? pcb.values : Arrays.copyOf(pcb.values, size);
                BitSet mask = finishMask(pcb.used, reuse);
                PackedTypedChannel chan = new FullPackedTypedChannel(pcb.symbol, vs, mask);
                tcbld.put(pcb.symbol, chan);
            }
        }
        long[] builtIds = (reuse && size == capacity()) ? ids : Arrays.copyOf(ids, size);
        double[] builtScores = (reuse && size == capacity()) ? scores : Arrays.copyOf(scores, size);
        if (reuse) {
            ids = null;
            scores = null;
            channels = null;
            typedChannels = null;
        }
        return new PackedScoredIdList(builtIds, builtScores,
                                      cbld.build(), tcbld.build());
    }

    /**
     * Copy a bit mask, re-using if appropriate.
     * @param mask The mask.
     * @param reuse Whether we should try to reuse the mask.
     * @return The final bit set, or {@code} if the mask is full.
     */
    private BitSet finishMask(BitSet mask, boolean reuse) {
        if (mask.cardinality() == size) {
            return null;
        } else if (reuse) {
            return mask;
        } else {
            return (BitSet) mask.clone();
        }
    }

    private int capacity() {
        return ids.length;
    }

    public int size() {
        return size;
    }

    private void requireCapacity(int sz) {
        if (sz > capacity()) {
            int newCap = Math.max(sz, capacity() * 2);
            ids = Arrays.copyOf(ids, newCap);
            scores = Arrays.copyOf(scores, newCap);
            // channel capacities will be lazily increased
            assert capacity() == newCap;
        }
    }

    /**
     * Add a scored ID without boxing.
     * @param id The ID.
     * @param score The score.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder add(long id, double score) {
        int idx = size;
        requireCapacity(idx + 1);
        ids[idx] = id;
        scores[idx] = score;
        size++;
        return this;
    }

    /**
     * Add a scored ID.
     * @param id The ID.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder add(ScoredId id) {
        int idx = size;
        add(id.getId(), id.getScore());
        for (Symbol sym: id.getChannels()) {
            putChannel(idx, sym, id.channel(sym));
        }
        for (TypedSymbol<?> sym: id.getTypedChannels()) {
            putChannel(idx, sym, id.channel(sym));
        }
        return this;
    }

    private void putChannel(int idx, Symbol sym, double channel) {
        PackedChanBuilder pack = channels.get(sym);
        if (pack == null) {
            pack = new PackedChanBuilder(sym);
            channels.put(sym, pack);
        }
        pack.set(idx, channel);
    }

    private void putChannel(int idx, TypedSymbol<?> sym, Object channel) {
        PackedTChanBuilder pack = typedChannels.get(sym);
        if (pack == null) {
            pack = new PackedTChanBuilder(sym);
            typedChannels.put(sym, pack);
        }
        pack.set(idx, channel);
    }

    /**
     * Sort the list-in-progress by the specified comparator.
     * @param order The comparator.
     * @return The buidler (for chaining).
     */
    public ScoredIdListBuilder sort(Comparator<ScoredId> order) {
        quickSort(0, size, new SortComp(order), new SortSwap());
        return this;
    }

    private class SortComp extends AbstractIntComparator {
        private final Comparator<ScoredId> order;
        private IndirectId id1, id2;

        public SortComp(Comparator<ScoredId> o) {
            order = o;
            id1 = new IndirectId();
            id2 = new IndirectId();
        }

        @Override
        public int compare(int i1, int i2) {
            id1.setIndex(i1);
            id2.setIndex(i2);
            return order.compare(id1, id2);
        }
    }

    private class SortSwap implements Swapper {
        @Override
        public void swap(int i, int j) {
            doSwap(ids, i, j);
            doSwap(scores, i, j);
            for (PackedChanBuilder chan: channels.values()) {
                chan.swap(i, j);
            }
            for (PackedTChanBuilder chan: typedChannels.values()) {
                chan.swap(i, j);
            }
        }
    }

    private static void doSwap(long[] longs, int i, int j) {
        long tmp = longs[i];
        longs[i] = longs[j];
        longs[j] = tmp;
    }
    private static void doSwap(double[] doubles, int i, int j) {
        double tmp = doubles[i];
        doubles[i] = doubles[j];
        doubles[j] = tmp;
    }
    private static void doSwap(Object[] objs, int i, int j) {
        Object tmp = objs[i];
        objs[i] = objs[j];
        objs[j] = tmp;
    }

    private class IndirectId extends AbstractScoredId {
        private int index;
        private Set<Symbol> cCache;
        private Set<TypedSymbol<?>> tcCache;

        public void setIndex(int idx) {
            index = idx;
            cCache = null;
            tcCache = null;
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
        public Set<Symbol> getChannels() {
            if (cCache == null) {
                ImmutableSet.Builder<Symbol> bld = ImmutableSet.builder();
                for (PackedChanBuilder ch: channels.values()) {
                    if (ch.used.get(index)) {
                        bld.add(ch.symbol);
                    }
                }
                cCache = bld.build();
            }
            return cCache;
        }

        @Override
        public Set<TypedSymbol<?>> getTypedChannels() {
            if (tcCache == null) {
                ImmutableSet.Builder<TypedSymbol<?>> bld = ImmutableSet.builder();
                for (PackedTChanBuilder ch: typedChannels.values()) {
                    if (ch.used.get(index)) {
                        bld.add(ch.symbol);
                    }
                }
                tcCache = bld.build();
            }
            return tcCache;
        }

        @Override
        public double channel(Symbol s) {
            PackedChanBuilder chan = channels.get(s);
            if (chan != null && chan.used.get(index)) {
                return chan.values[index];
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @Override
        public <K> K channel(TypedSymbol<K> s) {
            PackedTChanBuilder chan = typedChannels.get(s);
            if (chan != null && chan.used.get(index)) {
                return s.getType().cast(chan.values[index]);
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @Override
        public boolean hasChannel(Symbol s) {
            PackedChanBuilder chan = channels.get(s);
            return chan != null && chan.used.get(index);
        }

        @Override
        public boolean hasChannel(TypedSymbol<?> s) {
            PackedTChanBuilder chan = typedChannels.get(s);
            return chan != null && chan.used.get(index);
        }
    }

    private class PackedChanBuilder {
        private final Symbol symbol;
        private BitSet used;
        private double[] values;

        public PackedChanBuilder(Symbol sym) {
            symbol = sym;
            used = new BitSet();
            values = new double[capacity()];
        }

        private void set(int idx, double v) {
            assert idx >= 0 && idx < capacity();
            if (idx >= values.length) {
                values = Arrays.copyOf(values, capacity());
            }
            values[idx] = v;
            used.set(idx);
        }

        private void swap(int i, int j) {
            // anything that's set obviously has a value
            if (used.get(i)) {
                if (used.get(j)) {
                    doSwap(values, i, j);
                } else {
                    set(j, values[i]);
                    used.clear(i);
                }
            } else if (used.get(j)) {
                set(i, values[j]);
                used.clear(j);
            }
        }
    }

    private class PackedTChanBuilder {
        @SuppressWarnings("rawtypes")
        private final TypedSymbol symbol;
        private BitSet used;
        private Object[] values;

        private PackedTChanBuilder(TypedSymbol<?> sym) {
            symbol = sym;
            used = new BitSet();
            values = new Object[capacity()];
        }

        private void set(int idx, Object v) {
            assert idx >= 0 && idx < capacity();
            if (idx >= values.length) {
                values = Arrays.copyOf(values, capacity());
            }
            values[idx] = v;
            used.set(idx);
        }

        private void swap(int i, int j) {
            // anything that's set obviously has a value
            if (used.get(i)) {
                if (used.get(j)) {
                    doSwap(values, i, j);
                } else {
                    set(j, values[i]);
                    used.clear(i);
                }
            } else if (used.get(j)) {
                set(i, values[j]);
                used.clear(j);
            }
        }
    }
}
