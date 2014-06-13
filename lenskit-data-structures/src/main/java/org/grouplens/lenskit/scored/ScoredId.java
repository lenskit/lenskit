/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * A numerical ID associated with a score and optional side channels.
 * A {@code ScoredId} object is intended to be immutable.  Scored IDs can be created by using the
 * {@linkplain ScoredIds#newBuilder() builder} or accumulated in a {@link PackedScoredIdList}.
 * <p>
 * In addition to the score, a scored id associates <em>channels</em> with the id.  Channels are
 * identified by {@link TypedSymbol}s.  As an optimization, channels of type {@code double} can be
 * accessed in unboxed fashion using an untyped {@link Symbol}.
 * </p>
 * <p>
 * A channel, if it is present, cannot contain {@code null}.
 * </p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 * @compat Public
 */
public interface ScoredId {

    /**
     * Retrieve the numerical identifier of this {@code ScoredId}.
     * @return An identifier.
     */
    long getId();

    /**
     * Retrieve the score of this {@code ScoredId}.
     * @return The ID's score.
     */
    double getScore();

    /**
     * Determine the symbols associated with all unboxed double side channels of a {@code ScoredId}.
     * @return A set of {@code  Symbol} objects, each of which maps to a value in
     * one of the {@code ScoredId}'s unboxed double side channels.
     */
    @Nonnull
    Set<Symbol> getUnboxedChannelSymbols();

    /**
     * Determine the typed symbols associated with all side channels of a {@code ScoredId}.
     * @return A set of {@code  TypedSymbol} objects, each of which maps to a value in
     * one of the {@code ScoredId}'s side channels.
     */
    @Nonnull
    Set<TypedSymbol<?>> getChannelSymbols();

    /**
     * Get the channels associated with a scored ID.
     * @return The channels associated with this ID and their values.
     */
    @Nonnull
    Collection<SymbolValue<?>> getChannels();

    /**
     * Get the unboxed channels associated with a scored ID.
     * @return The unboxed channels associated with this ID and their values.
     */
    @Nonnull
    Collection<DoubleSymbolValue> getUnboxedChannels();

    /**
     * Get the value for a channel.
     * @param sym The channel symbol.
     * @param <T> The type contained.
     * @return The channel's value, or {@code null} if no such channel is present.
     */
    @Nullable
    <T> T getChannelValue(@Nonnull TypedSymbol<T> sym);

    /**
     * Get the unboxed value for a channel.  The channel must exist.
     * @param sym The channel symbol.
     * @return The channel's value.
     * @throws NullPointerException if the symbol names a nonexistent channel.
     */
    double getUnboxedChannelValue(Symbol sym);

    /**
     * Determine if a {@code ScoredId} has a specific channel.
     * @param s The side channel's symbol.
     * @return {@code true} if the {@code ScoredId} has a channel associated
     * with this symbol, {@code false} otherwise.
     */
    boolean hasUnboxedChannel(Symbol s);
    
    /**
     * Determine if a {@code ScoredId} has a specific typed channel.
     * @param s The typed side channel's symbol.
     * @return {@code true} if the {@code ScoredId} has a channel associated
     * with this symbol, {@code false} otherwise.
     */
    boolean hasChannel(TypedSymbol<?> s);
}
