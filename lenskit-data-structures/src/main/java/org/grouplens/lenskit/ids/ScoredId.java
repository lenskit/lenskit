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

        private ScoredId sid;

        /**
         * Create a new builder to construct a {@code ScoredId}.
         * @param id A numerical identifier.
         * @param score A score associated with this identifier.
         */
        public Builder(long id, double score) {
            sid = new ScoredId();
            sid.id = id;
            sid.score = score;
        }

        /**
         * Add a new side channel to the {@code ScoredId} under construction.
         * @param s The symbol for the side channel.
         * @param value The numerical value for the side channel.
         * @return This builder (for chaining)
         */
        public Builder addChannel(Symbol s, double value) {
            if (sid.channelMap == null) {
                sid.channelMap = new Reference2DoubleArrayMap<Symbol>();
            }
            sid.channelMap.put(s, value);
            return this;
        }

        /**
         * Finish constructing the {@code ScoredId} and instantiate it.
         * @return A new {@code ScoredId} object.
         */
        @Override
        public ScoredId build() {
            return sid;
        }
    }
}