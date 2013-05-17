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

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.symbols.Symbol;

/**
 * Use a {@code ScoredId.Builder} to instantiate new {@code ScoredId} objects.
 */
public class ScoredIdBuilder implements Builder<ScoredId> {

    private long id;
    private double score;
    private Reference2DoubleArrayMap<Symbol> channelMap;

    /**
     * Create a {@code ScoredIdBuilder}. Any {@code ScoredId} objects
     * created by this builder will have the default ID of 0 and default
     * score of 0 unless they are explicitly set by the user.
     */
    public ScoredIdBuilder() {
        this(0, 0);
    }

    /**
     * Create a {@code ScoredIdBuilder}. Any {@code ScoredId} objects
     * created by this builder will have the specified ID and a default
     * score of 0 unless they are explicitly changed by the user.
     * @param id The numerical ID of {@code ScoredId} objects produced
     *           by this builder.
     */
    public ScoredIdBuilder(long id) {
        this(id, 0);
    }

    /**
     * Create a {@code ScoredIdBuilder}. Any {@code ScoredId} objects
     * created by this builder will have the specified ID and score unless
     * they are explicitly changed by the user.
     * @param id The numerical ID of {@code ScoredId} objects produced by
     *           this builder.
     * @param score The score for {@code ScoredId} objects produced by
     *              this builder.
     */
    public ScoredIdBuilder(long id, double score) {
        this.id = id;
        this.score = score;
        channelMap = new Reference2DoubleArrayMap<Symbol>();
    }

    /**
     * Change the ID of the {@code ScoredID} object under construction.
     * @param id The ID to be used for new {@code ScoredId} objects.
     * @return This builder (for chaining)
     */
    public ScoredIdBuilder setId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Change the score of the {@code ScoredId} object under construction.
     * @param score The score to be used for new {@code ScoredId} objects.
     * @return This builder (for chaining)
     */
    public ScoredIdBuilder setScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * Add a new side channel to the {@code ScoredId} under construction.
     * @param s The symbol for the side channel.
     * @param value The numerical value for the side channel.
     * @return This builder (for chaining)
     */
    public ScoredIdBuilder addChannel(Symbol s, double value) {
        channelMap.put(s, value);
        return this;
    }

    /**
     * Removes all channels from new {@code ScoredId} objects produced by the builder.
     * @return This builder (for chaining)
     */
    public ScoredIdBuilder clearChannels() {
        channelMap.clear();
        return this;
    }

    /**
     * Finish constructing the {@code ScoredId} and instantiate it.
     * @return A new {@code ScoredId} object.
     */
    @Override
    public ScoredId build() {
        if (channelMap.isEmpty()) {
            return new ScoredIdImpl(id, score, null);
        } else {
            return new ScoredIdImpl(id, score, channelMap);
        }
    }
}