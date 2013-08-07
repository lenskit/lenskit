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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class ScoredIdImpl extends AbstractScoredId implements Serializable {
    private static final long serialVersionUID = 2L;

    private final long id;
    private final double score;
    @Nonnull
    private final List<SymbolValue<?>> channels;
    private transient volatile List<DoubleSymbolValue> unboxedChannels;

    public ScoredIdImpl(long id, double score) {
        this(id, score, Collections.<SymbolValue<?>>emptyList());
    }

    /**
     * Construct a new {@link ScoredId}.
     * @param id The ID.
     * @param score The score.
     * @param chans The side channel map.
     */
    public ScoredIdImpl(long id, double score, @Nonnull Collection<? extends SymbolValue<?>> chans) {
        this.id = id;
        this.score = score;
        channels = ImmutableList.copyOf(chans);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Nonnull
    @Override
    public Collection<SymbolValue<?>> getChannels() {
        return channels;
    }

    @Nonnull
    @Override
    public Collection<DoubleSymbolValue> getUnboxedChannels() {
        if (unboxedChannels == null) {
            unboxedChannels = FluentIterable.from(channels)
                                            .filter(DoubleSymbolValue.class)
                                            .toList();
        }
        return unboxedChannels;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getChannelValue(@Nonnull TypedSymbol<T> sym) {
        for (SymbolValue<?> channel: channels) {
            if (sym == channel.getSymbol()) {
                assert sym.getType().isInstance(channel.getValue());
                return (T) channel.getValue();
            }
        }
        return null;
    }

    @Override
    public double getUnboxedChannelValue(Symbol sym) {
        for (DoubleSymbolValue channel: getUnboxedChannels()) {
            if (sym == channel.getSymbol().getRawSymbol()) {
                return channel.getDoubleValue();
            }
        }

        throw new NullPointerException("no such symbol " + sym);
    }
}
