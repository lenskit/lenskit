package org.grouplens.lenskit.scored;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import java.io.Serializable;
import java.util.*;

/**
 * A space-efficient, unboxed list of scored IDs.  These lists are immutable; create them using a
 * {@link ScoredIdListBuilder}.
 *
 * @since 1.4
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PackedScoredIdList extends AbstractList<ScoredId> implements FastCollection<ScoredId> {
    private final long[] ids;
    private final double[] scores;
    private final Map<Symbol,PackedChannel> channels;
    private final Map<TypedSymbol<?>,PackedTypedChannel> typedChannels;

    PackedScoredIdList(long[] ids, double[] scores,
                       Map<Symbol, PackedChannel> chans,
                       Map<TypedSymbol<?>, PackedTypedChannel> tchans) {
        this.ids = ids;
        this.scores = scores;
        channels = chans;
        typedChannels = tchans;
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
        return new IndirectId(i);
    }

    private class IndirectId extends AbstractScoredId implements Serializable {
        private int index;
        private volatile Set<Symbol> cCache;
        private volatile Set<TypedSymbol<?>> tcCache;

        public IndirectId(int idx) {
            index = idx;
        }

        public Object writeReplace() {
            return ScoredIds.copyBuilder(this).build();
        }

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
                for (PackedChannel ch: channels.values()) {
                    if (ch.hasValue(index)) {
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
                for (PackedTypedChannel ch: typedChannels.values()) {
                    if (ch.hasValue(index)) {
                        bld.add(ch.symbol);
                    }
                }
                tcCache = bld.build();
            }
            return tcCache;
        }

        @Override
        public double channel(Symbol s) {
            PackedChannel chan = channels.get(s);
            if (chan != null && chan.hasValue(index)) {
                return chan.get(index);
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @Override
        public <K> K channel(TypedSymbol<K> s) {
            PackedTypedChannel chan = typedChannels.get(s);
            if (chan != null && chan.hasValue(index)) {
                return s.getType().cast(chan.get(index));
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @Override
        public boolean hasChannel(Symbol s) {
            PackedChannel chan = channels.get(s);
            return chan != null && chan.hasValue(index);
        }

        @Override
        public boolean hasChannel(TypedSymbol<?> s) {
            PackedTypedChannel chan = typedChannels.get(s);
            return chan != null && chan.hasValue(index);
        }
    }

    private class FastIter implements Iterator<ScoredId> {
        int next = 0;
        IndirectId id = new IndirectId(0);

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

    //region Channel storage

    abstract static class PackedChannel {
        private final Symbol symbol;

        PackedChannel(Symbol sym) {
            symbol = sym;
        }

        abstract boolean hasValue(int idx);
        abstract double get(int idx);
    }

    static class FullPackedChannel extends PackedChannel {
        private final double[] values;
        private final BitSet used;

        FullPackedChannel(Symbol sym, double[] data, BitSet mask) {
            super(sym);
            used = mask;
            values = data;
        }

        @Override
        boolean hasValue(int idx) {
            return used == null || used.get(idx);
        }

        @Override
        double get(int idx) {
            return values[idx];
        }
    }

    abstract static class PackedTypedChannel {
        @SuppressWarnings("rawtypes")
        private final TypedSymbol symbol;

        private PackedTypedChannel(TypedSymbol<?> sym) {
            symbol = sym;
        }

        protected abstract boolean hasValue(int idx);
        protected abstract Object get(int idx);
    }

    static class FullPackedTypedChannel extends PackedTypedChannel {
        private final Object[] values;
        private final BitSet used;

        FullPackedTypedChannel(TypedSymbol<?> sym, Object[] data, BitSet mask) {
            super(sym);
            used = mask;
            values = data;
        }

        @Override
        protected boolean hasValue(int idx) {
            return used == null || used.get(idx);
        }

        @Override
        protected Object get(int idx) {
            return values[idx];
        }
    }
    //endregion
}
