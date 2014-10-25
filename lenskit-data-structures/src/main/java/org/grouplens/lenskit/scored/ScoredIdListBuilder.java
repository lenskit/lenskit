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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.collections.CompactableLongArrayList;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.*;

import static it.unimi.dsi.fastutil.Arrays.quickSort;

/**
 * Builder for packed lists of scored ids.  All ids in the resulting list will have the same set
 * of side channels.
 *
 * @since 1.4
 * @compat Public
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ScoredIdListBuilder implements Builder<PackedScoredIdList> {
    // INVARIANT: all arrays (including channel arrays) have same size, which is capacity
    // INVARIANT: all arrays are non-null unless finish() has been called
    private CompactableLongArrayList ids;
    private DoubleArrayList scores;
    private boolean ignoreUnknown = false;
    private Map<Symbol,ChannelStorage> channels;
    private Map<TypedSymbol<?>,TypedChannelStorage<?>> typedChannels;

    public ScoredIdListBuilder() {
        this(10);
    }

    public ScoredIdListBuilder(int cap) {
        initialize(cap);
    }

    private void initialize(int cap) {
        ids = new CompactableLongArrayList(cap);
        scores = new DoubleArrayList(cap);
        channels = new Reference2ObjectArrayMap<Symbol, ChannelStorage>();
        typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, TypedChannelStorage<?>>();
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

    /**
     * Implementation of {@link #build()} and {@link #finish()}.
     * @param reuse Whether we should try to reuse the builder's storage for the packed list.
     *                 If {@code true}, the builder will be invalid after finishing and the packed
     *                 list will use the same arrays as the builder if they are full.
     * @return The packed ID list.
     */
    private PackedScoredIdList finish(boolean reuse) {
        Preconditions.checkState(ids != null, "builder has been finished");
        Map<Symbol, DoubleList> chans;
        Map<TypedSymbol<?>, List<?>> typedChans;
        if (size() > 0) {
            ImmutableMap.Builder<Symbol, DoubleList> cbld = ImmutableMap.builder();
            for (ChannelStorage chan: channels.values()) {
                DoubleArrayList built;
                if (reuse) {
                    built = chan.values;
                    built.trim();
                } else {
                    built = new DoubleArrayList(chan.values);
                }
                cbld.put(chan.symbol, built);
            }
            chans = cbld.build();
            ImmutableMap.Builder<TypedSymbol<?>, List<?>> tcbld = ImmutableMap.builder();
            for (TypedChannelStorage<?> chan: typedChannels.values()) {
                List<?> built;
                if (reuse) {
                    chan.values.trimToSize();
                    built = chan.values;
                } else {
                    built = new ArrayList<Object>(chan.values);
                }
                tcbld.put(chan.symbol, built);
            }
            typedChans = tcbld.build();
        } else {
            chans = Collections.emptyMap();
            typedChans = Collections.emptyMap();
        }
        LongList builtIds;
        DoubleList builtScores;
        if (reuse) {
            ids.trim();
            builtIds = ids;
            scores.trim();
            builtScores = scores;
            clear();
        } else {
            builtIds = new CompactableLongArrayList(ids);
            builtScores = new DoubleArrayList(scores);
        }
        return new PackedScoredIdList(builtIds, builtScores, typedChans, chans);
    }

    /**
     * Clear the builder. After it is cleared, it can no longer be used.
     */
    public void clear() {
        ids = null;
        scores = null;
        channels = null;
        typedChannels = null;
    }

    /**
     * Build a sparse vector directly from the list of IDs. This allows a scored ID list builder to
     * be used to efficiently accumulate a sparse vector.  If the same ID is added multiple times,
     * the first instance is used.
     *
     * @return A sparse vector containing the data accumulated.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ImmutableSparseVector buildVector() {
        MutableSparseVector msv = MutableSparseVector.create(ids);
        final int size = size();
        for (int i = 0; i < size; i++) {
            msv.set(ids.get(i), scores.get(i));
        }

        for (ChannelStorage chan: channels.values()) {
            MutableSparseVector vchan = msv.getOrAddChannelVector(chan.symbol);
            for (int i = 0; i < size; i++) {
                vchan.set(ids.get(i), chan.values.get(i));
            }
        }

        for (TypedChannelStorage<?> chan: typedChannels.values()) {
            Long2ObjectMap vchan = msv.getOrAddChannel(chan.symbol);
            for (int i = 0; i < size; i++) {
                vchan.put(ids.get(i), chan.values.get(i));
            }
        }

        return msv.freeze();
    }

    /**
     * Get the number of items currently in the builder.
     * @return The number of items in the builder.
     */
    public int size() {
        assert ids.size() == scores.size();
        return ids.size();
    }

    /**
     * Add a scored ID without boxing.  The default value will be used for each channel.
     * @param id The ID to add.
     * @param score The score for the ID.
     * @return The builder (for chaining).
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ScoredIdListBuilder add(long id, double score) {
        Preconditions.checkState(ids != null, "builder has been finished");
        ids.add(id);
        scores.add(score);
        for (ChannelStorage chan: channels.values()) {
            assert chan.values.size() == ids.size() - 1;
            chan.values.add(chan.defaultValue);
        }
        for (TypedChannelStorage chan: typedChannels.values()) {
            assert chan.values.size() == ids.size() - 1;
            chan.values.add(chan.defaultValue);
        }
        return this;
    }

    /**
     * Add a scored ID.  The ID is copied into the builder, not referenced.  All side channels on
     * the ID must have already been added with one of the {@code addChannel} methods.
     * @param id The ID.
     * @return The builder (for chaining).
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ScoredIdListBuilder add(ScoredId id) {
        Preconditions.checkState(ids != null, "builder has been finished");
        // check whether all symbols are valid
        Collection<SymbolValue<?>> chans = id.getChannels();
        if (!ignoreUnknown) {
            for (SymbolValue<?> chan: chans) {
                TypedSymbol<?> sym = chan.getSymbol();
                boolean good = sym.getType().equals(Double.class)
                        ? channels.containsKey(sym.getRawSymbol())
                        : typedChannels.containsKey(sym);
                if (!good) {
                    throw new IllegalArgumentException("channel " + sym + " not known");
                }
            }
        }

        // now we're ready to add
        int idx = ids.size();
        add(id.getId(), id.getScore());
        assert ids.size() == idx + 1;
        assert scores.size() == idx + 1;
        for (SymbolValue<?> sv: chans) {
            TypedSymbol<?> sym = sv.getSymbol();
            if (sym.getType().equals(Double.class) && channels.containsKey(sym.getRawSymbol())) {
                ChannelStorage chan = channels.get(sym.getRawSymbol());
                assert chan.values.size() == idx + 1;
                if (sv instanceof DoubleSymbolValue) {
                    chan.values.set(idx, ((DoubleSymbolValue) sv).getDoubleValue());
                } else {
                    Object v = sv.getValue();
                    chan.values.set(idx, (Double) v);
                }
            } else {
                TypedChannelStorage chan = typedChannels.get(sv.getSymbol());
                if (chan != null) {
                    assert chan.values.size() == idx + 1;
                    chan.values.set(idx, sv.getValue());
                }
            }
        }
        return this;
    }

    /**
     * Add a collection of IDs. The IDs are copied into the builder, not referenced.
     * @param ids The IDs to add.
     * @return The builder (for chaining)
     */
    public ScoredIdListBuilder addAll(Iterable<ScoredId> ids) {
        Preconditions.checkState(ids != null, "builder has been finished");
        for (ScoredId id: ids) {
            add(id);
        }
        return this;
    }

    /**
     * Add a side channel to the list builder with a default value of 0.  It is an error
     * to add the same symbol multiple times.  All side channels that will be used must be added
     * prior to calling {@link #add(ScoredId)}.
     *
     * @param sym The symbol to add.
     * @return The builder (for chaining).
     * @see #addChannel(Symbol, double)
     */
    public ScoredIdListBuilder addChannel(Symbol sym) {
        return addChannel(sym, 0);
    }

    /**
     * Add a side channel to the list builder.  It is an error to add the same symbol multiple times.
     * All side channels that will be used must be added prior to calling {@link #add(ScoredId)}.
     *
     * @param sym The symbol to add.
     * @param dft The default value when adding IDs that lack this channel.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder addChannel(Symbol sym, double dft) {
        Preconditions.checkState(ids != null, "builder has been finished");
        if (channels.containsKey(sym)) {
            throw new IllegalArgumentException(sym + " already in the builder");
        } else {
            channels.put(sym, new ChannelStorage(sym, dft));
        }
        return this;
    }

    /**
     * Add multiple unboxed channels with a default value of 0.
     * @param channels The channels to add.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder addChannels(Iterable<Symbol> channels) {
        for (Symbol sym: channels) {
            addChannel(sym);
        }
        return this;
    }

    /**
     * Add a side channel to the list builder.  It is an error
     * to add the same symbol multiple times.  All side channels that will be used must be added
     * prior to calling {@link #add(ScoredId)}.
     *
     * @param sym The symbol to add.
     * @return The builder (for chaining).
     * @see #addChannel(TypedSymbol, Object)
     */
    public ScoredIdListBuilder addChannel(TypedSymbol<?> sym) {
        return addChannel(sym, null);
    }

    /**
     * Add a typed side channel to the list builder.  It is an error to add the same symbol multiple
     * times. All side channels that will be used must be added prior to calling {@link
     * #add(ScoredId)}.
     *
     * @param sym The symbol to add.
     * @param dft The default value when adding ids that lack this channel.  If {@code null},
     *            it will be omitted from such ids.
     * @return The builder (for chaining).
     */
    public <T> ScoredIdListBuilder addChannel(TypedSymbol<T> sym, T dft) {
        Preconditions.checkState(ids != null, "builder has been finished");
        if (typedChannels.containsKey(sym)) {
            throw new IllegalArgumentException(sym + " already in the builder");
        } else {
            typedChannels.put(sym, new TypedChannelStorage<T>(sym, dft));
        }
        return this;
    }

    /**
     * Add multiple channels with a default value of {@code null}.
     * @param channels The channels to add.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder addTypedChannels(Iterable<? extends TypedSymbol<?>> channels) {
        for (TypedSymbol<?> sym: channels) {
            addChannel(sym);
        }
        return this;
    }

    /**
     * Set the builder to ignore unknown channels on IDs passed to {@link #add(ScoredId)}.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder ignoreUnknownChannels() {
        ignoreUnknown = true;
        return this;
    }

    /**
     * Set the builder to fail on unknown channels.  This is the default response to unknown
     * channels.
     * @return The builder (for chaining).
     */
    public ScoredIdListBuilder failOnUnknownChannels() {
        ignoreUnknown = false;
        return this;
    }

    /**
     * Sort the list-in-progress by the specified comparator.
     * @param order The comparator.
     * @return The buidler (for chaining).
     */
    public ScoredIdListBuilder sort(Comparator<ScoredId> order) {
        Preconditions.checkState(ids != null, "builder has been finished");
        quickSort(0, size(), new SortComp(order), new SortSwap());
        return this;
    }

    /**
     * Comparator for sorting the list.  This comparator internally uses a packed list over the
     * entire capacity of the builder to provide ids for the real comparator to use.
     */
    private class SortComp extends AbstractIntComparator {
        private final Comparator<ScoredId> order;

        private PackedScoredIdList.IndirectScoredId id1;
        private PackedScoredIdList.IndirectScoredId id2;

        public SortComp(Comparator<ScoredId> o) {
            order = o;

            // make an internal list
            Map<Symbol,DoubleList> chanMap = Maps.newHashMap();
            for (ChannelStorage chan: channels.values()) {
                chanMap.put(chan.symbol, chan.values);
            }
            Map<TypedSymbol<?>,List<?>> typedMap = Maps.newHashMap();
            for (TypedChannelStorage<?> chan: typedChannels.values()) {
                typedMap.put(chan.symbol, chan.values);
            }
            PackedScoredIdList list = new PackedScoredIdList(ids, scores, typedMap, chanMap);

            id1 = list.getFlyweight(0);
            id2 = list.getFlyweight(0);
        }

        @Override
        public int compare(int i1, int i2) {
            id1.setIndex(i1);
            id2.setIndex(i2);
            return order.compare(id1, id2);
        }
    }

    /**
     * Swapper for sorting the list.
     */
    private class SortSwap implements Swapper {
        @Override
        public void swap(int i, int j) {
            doSwap(ids, i, j);
            doSwap(scores, i, j);
            for (ChannelStorage chan: channels.values()) {
                doSwap(chan.values, i, j);
            }
            for (TypedChannelStorage<?> chan: typedChannels.values()) {
                doSwap(chan.values, i, j);
            }
        }
    }

    private static void doSwap(LongList longs, int i, int j) {
        longs.set(i, longs.set(j, longs.get(i)));
    }
    private static void doSwap(DoubleList doubles, int i, int j) {
        doubles.set(i, doubles.set(j, doubles.get(i)));
    }
    private static <T> void doSwap(List<T> objs, int i, int j) {
        objs.set(i, objs .set(j, objs .get(i)));
    }

    /**
     * Storage for a side channel.
     */
    private class ChannelStorage {
        private final Symbol symbol;
        private final double defaultValue;
        private DoubleArrayList values;

        public ChannelStorage(Symbol sym, double dft) {
            symbol = sym;
            defaultValue = dft;
            values = new DoubleArrayList(scores.elements().length);
            for (int i = size() - 1; i >= 0; i--) {
                values.add(defaultValue);
            }
        }
    }

    /**
     * Storage for a typed side channel.
     */
    private class TypedChannelStorage<T> {
        private final TypedSymbol<T> symbol;
        private final T defaultValue;
        private ArrayList<T> values;

        @SuppressWarnings("unchecked")
        private TypedChannelStorage(TypedSymbol<T> sym, T dft) {
            symbol = sym;
            defaultValue = dft;
            values = new ArrayList<T>(scores.elements().length);
            for (int i = size() - 1; i >= 0; i--) {
                values.add(defaultValue);
            }
        }
    }
}
