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
package org.grouplens.lenskit.ids;

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.symbols.Symbol;

import java.util.Comparator;

/**
 * A numerical ID associated with a score and optional side channels.
 * A {@code ScoredId} object is intended to be immutable. The {@code ScoredId.Builder}
 * class may be used instantiate to a new {@code ScoredId} object, and the
 * {@code MutableScoredId} class may be used to implement fast iteration.
 */
public class ScoredId {

    protected long id;
    protected double score;
    protected Reference2DoubleMap<Symbol> channelMap;

    public static final Comparator<ScoredId> DESCENDING_SCORE_COMPARATOR = new Comparator<ScoredId>() {
        @Override
        public int compare(ScoredId o1, ScoredId o2) {
            return DoubleComparators.OPPOSITE_COMPARATOR.compare(o1.getScore(), o2.getScore());
        }
    };

    public ScoredId(long id, double score) {
        this.id = id;
        this.score = score;
    }

    private ScoredId(long id, double score, Reference2DoubleMap<Symbol> channelMap) {
        this.id = id;
        this.score = score;
        if (channelMap != null) {
            this.channelMap = new Reference2DoubleArrayMap<Symbol>(channelMap);
        }
    }

    public long getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    /**
     * Determine if a {@code ScoredId} has a specific channel.
     * @param s The side channel's symbol.
     * @return {@code true} if the {@code ScoredId} has a channel associated
     * with this symbol, {@code false} otherwise.
     */
    public boolean hasChannel(Symbol s) {
        return channelMap != null && channelMap.containsKey(s);
    }

    /**
     * Retrieve the value stored in the {@code ScoredId}'s side channel
     * associated with a specific symbol.
     * @param s The side channel's symbol.
     * @return The value of the appropriate side channel.
     */
    public double channel(Symbol s) {
        if (hasChannel(s)) {
            return channelMap.get(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScoredId) {
            ScoredId oid = (ScoredId) o;
            return getId() == oid.getId() && getScore() == oid.getScore();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(score)
                .toHashCode();
    }

    /**
     * Use a {@code ScoredId.Builder} to instantiate new {@code ScoredId} objects.
     */
    public static class Builder implements org.apache.commons.lang3.builder.Builder {

        private long id;
        private double score;
        private Reference2DoubleArrayMap<Symbol> channelMap;

        /**
         * Create a {@code ScoredId.Builder}. Any {@code ScoredId} objects
         * created by this builder will have the default ID of 0 and default
         * score of 0 unless they are explicitly set by the user.
         */
        public Builder() {
            this(0, 0);
        }

        /**
         * Create a {@code ScoredId.Builder}. Any {@code ScoredId} objects
         * created by this builder will have the specified ID and a default
         * score of 0 unless they are explicitly changed by the user.
         * @param id The numerical ID of {@code ScoredId} objects produced
         *           by this builder.
         */
        public Builder(long id) {
            this(id, 0);
        }

        /**
         * Create a {@code ScoredId.Builder}. Any {@code ScoredId} objects
         * created by this builder will have the specifed ID and score unless
         * they are explicitly changed by the user.
         * @param id The numerical ID of {@code ScoredId} objects produced by
         *           this builder.
         * @param score The score for {@code ScoredId} objects produced by
         *              this builder.
         */
        public Builder(long id, double score) {
            this.id = id;
            this.score = score;
            channelMap = new Reference2DoubleArrayMap<Symbol>();
        }

        /**
         * Change the ID of {@code ScoredID} object under construction.
         * @param id The ID to be used for new {@code ScoredId} objects.
         * @return This builder (for chaining)
         */
        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        /**
         * Change the score of the {@code ScoredId} object under construction.
         * @param score The score to be used for new {@code ScoredId} objects.
         * @return This builder (for chaining)
         */
        public Builder setScore(double score) {
            this.score = score;
            return this;
        }

        /**
         * Add a new side channel to the {@code ScoredId} under construction.
         * @param s The symbol for the side channel.
         * @param value The numerical value for the side channel.
         * @return This builder (for chaining)
         */
        public Builder addChannel(Symbol s, double value) {
            channelMap.put(s, value);
            return this;
        }

        /**
         * Removes all channels from new {@code ScoredId} objects produced by the builder.
         */
        public void clearChannels() {
            channelMap.clear();
        }

        /**
         * Finish constructing the {@code ScoredId} and instantiate it.
         * @return A new {@code ScoredId} object.
         */
        @Override
        public ScoredId build() {
            if (channelMap.isEmpty()) {
                return new ScoredId(id, score, null);
            } else {
                return new ScoredId(id, score, channelMap);
            }
        }
    }
}