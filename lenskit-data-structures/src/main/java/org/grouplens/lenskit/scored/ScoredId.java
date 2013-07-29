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

import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import java.util.Set;

/**
 * A numerical ID associated with a score and optional side channels.
 * A {@code ScoredId} object is intended to be immutable.  Scored IDs can be created by using the
 * {@linkplain ScoredIds#newBuilder() builder} or accumulated in a {@link PackedScoredIdList}.
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
     * Determine the symbols associated with all side channels of a {@code ScoredId}.
     * @return A set of {@code  Symbol} objects, each of which maps to a value in
     * one of the {@code ScoredId}'s side channels.
     */
    Set<Symbol> getChannels();

    /**
     * Determine the typed symbols associated with all typed side channels of a {@code ScoredId}.
     * @return A set of {@code  TypedSymbol} objects, each of which maps to a value in
     * one of the {@code ScoredId}'s typed side channels.
     */
    Set<TypedSymbol<?>> getTypedChannels();

    /**
     * Retrieve the value stored in the {@code ScoredId}'s side channel
     * associated with a specific symbol.
     * @param s The side channel's symbol.
     * @return The value of the appropriate side channel.
     */
    double channel(Symbol s);
    
    /**
     * Retrieve the value stored in the {@code ScoredId}'s typed side channel
     * associated with a specific symbol.
     * @param s The typed side channel's typed symbol.
     * @return The value of the appropriate side channel.
     */
    <K> K channel(TypedSymbol<K> s);

    /**
     * Determine if a {@code ScoredId} has a specific channel.
     * @param s The side channel's symbol.
     * @return {@code true} if the {@code ScoredId} has a channel associated
     * with this symbol, {@code false} otherwise.
     */
    boolean hasChannel(Symbol s);
    
    /**
     * Determine if a {@code ScoredId} has a specific typed channel.
     * @param s The typed side channel's symbol.
     * @return {@code true} if the {@code ScoredId} has a channel associated
     * with this symbol, {@code false} otherwise.
     */
    boolean hasChannel(TypedSymbol<?> s);
}