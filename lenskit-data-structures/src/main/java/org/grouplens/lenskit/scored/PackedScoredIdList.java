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

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * A space-efficient, unboxed list of scored IDs.  These lists are immutable; create them using a
 * {@link ScoredIdListBuilder}.
 *
 * @since 1.4
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PackedScoredIdList extends AbstractList<ScoredId> implements FastCollection<ScoredId>, Serializable {
    private static final long serialVersionUID = 1L;
    private final long[] ids;
    private final double[] scores;
    private final Map<Symbol,double[]> channels;
    private final Map<TypedSymbol<?>,Object[]> typedChannels;

    PackedScoredIdList(long[] ids, double[] scores,
                       Map<Symbol, double[]> chans,
                       Map<TypedSymbol<?>, Object[]> tchans) {
        assert ids.length == scores.length;
        this.ids = ids;
        this.scores = scores;
        channels = chans;
        typedChannels = tchans;
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
        for (double[] chan: channels.values()) {
            if (chan.length != ids.length) {
                throw new InvalidObjectException("channel array has incorrect size");
            }
        }
        for (Map.Entry<TypedSymbol<?>,Object[]> tc: typedChannels.entrySet()) {
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
        public Set<Symbol> getChannels() {
            return channels.keySet();
        }

        @Override
        public Set<TypedSymbol<?>> getTypedChannels() {
            return typedChannels.keySet();
        }

        @Override
        public double channel(Symbol s) {
            double[] chan = channels.get(s);
            if (chan != null) {
                return chan[index];
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <K> K channel(TypedSymbol<K> s) {
            Object[] chan = typedChannels.get(s);
            if (chan != null) {
                Object obj = chan[index];
                assert obj == null || s.getType().isInstance(obj);
                return (K) obj;
            } else {
                throw new IllegalArgumentException("unknown symbol " + s);
            }
        }

        @Override
        public boolean hasChannel(Symbol s) {
            return channels.containsKey(s);
        }

        @Override
        public boolean hasChannel(TypedSymbol<?> s) {
            return typedChannels.containsKey(s);
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
