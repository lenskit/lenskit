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
import com.google.common.collect.FluentIterable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Scored ID implementation backed by a sparse vector.
 *
 * @since 1.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class VectorEntryScoredId extends AbstractScoredId {

    private final SparseVector vector;
    private VectorEntry ent;

    /**
     * Construct a new vector entry scored ID.
     * @param v The vector whose entries will back this scored ID.
     */
    public VectorEntryScoredId(SparseVector v) {
        vector = v;
    }

    @Override
    public long getId() {
        return ent.getKey();
    }

    @Override
    public double getScore() {
        return ent.getValue();
    }

    @Nonnull
    @Override
    public Collection<SymbolValue<?>> getChannels() {
        return FluentIterable.from(vector.getChannelSymbols())
                             .transform(new Function<TypedSymbol<?>, SymbolValue<?>>() {
                                 @SuppressWarnings({"unchecked", "rawtypes"})
                                 @Nullable
                                 @Override
                                 public SymbolValue<?> apply(@Nullable TypedSymbol input) {
                                     assert input != null;
                                     Object obj = vector.getChannel(input).get(ent.getKey());
                                     if (obj == null) {
                                         return null;
                                     } else {
                                         return input.withValue(obj);
                                     }
                                 }
                             }).filter(Predicates.notNull()).toList();
    }

    @Nonnull
    @Override
    public Collection<DoubleSymbolValue> getUnboxedChannels() {
        // FIXME Make this fast
        return FluentIterable.from(vector.getChannelVectorSymbols())
                             .transform(new Function<Symbol, DoubleSymbolValue>() {
                                 @Nullable
                                 @Override
                                 public DoubleSymbolValue apply(@Nullable Symbol input) {
                                     assert input != null;
                                     if (vector.getChannelVector(input).isSet(ent)) {
                                         return SymbolValue.of(input, vector.getChannelVector(input).get(ent));
                                     } else {
                                         return null;
                                     }
                                 }
                             }).filter(Predicates.notNull()).toList();
    }

    @Nullable
    @Override
    public <T> T getChannelValue(@Nonnull TypedSymbol<T> sym) {
        Long2ObjectMap<T> channel = vector.getChannel(sym);
        if (channel != null) {
            return channel.get(ent.getKey());
        } else {
            return null;
        }
    }

    @Override
    public double getUnboxedChannelValue(Symbol sym) {
        return vector.getChannelVector(sym).get(ent);
    }

    @Override
    public boolean hasUnboxedChannel(Symbol s) {
        return vector.hasChannelVector(s) && vector.getChannelVector(s).isSet(ent);
    }

    @Override
    public boolean hasChannel(TypedSymbol<?> s) {
        return vector.hasChannel(s) && vector.getChannel(s).containsKey(ent.getKey());
    }

    /**
     * Set the entry backing this scored ID.
     * @param e The entry backing the scored ID.
     */
    public void setEntry(VectorEntry e) {
        Preconditions.checkArgument(e.getVector() == vector, "entry must be associated with vector");
        ent = e;
    }
}
