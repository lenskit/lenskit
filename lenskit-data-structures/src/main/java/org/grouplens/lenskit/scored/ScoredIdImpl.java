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

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unimi.dsi.fastutil.objects.*;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

class ScoredIdImpl extends AbstractScoredId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private final double score;
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Nonnull
    private final Reference2DoubleMap<Symbol> channels;
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Nonnull
    private final Map<TypedSymbol<?>, ?> typedChannels;

    public ScoredIdImpl(long id, double score) {
        this(id, score, null, null);
    }

    /**
     * Construct a new {@link ScoredId}.
     * @param id The ID.
     * @param score The score.
     * @param chans The side channel map.
     * @param tChans The typed side channel map.
     */
    public ScoredIdImpl(long id, double score, Reference2DoubleMap<Symbol> chans,
            Reference2ObjectMap<TypedSymbol<?>, ?> tChans) {
        this.id = id;
        this.score = score;
        if (chans != null) {
            this.channels = new Reference2DoubleArrayMap<Symbol>(chans);
        } else {
            this.channels = Reference2DoubleMaps.EMPTY_MAP;
        }
        if (tChans != null) {
            this.typedChannels = ImmutableMap.copyOf(tChans);
        } else {
            this.typedChannels = Collections.emptyMap();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public boolean hasChannel(Symbol s) {
        return channels.containsKey(s);
    }

    @Override
    public boolean hasChannel(TypedSymbol<?> s) {
        return typedChannels.containsKey(s);
    }

    @Override
    public double channel(Symbol s) {
        if (hasChannel(s)) {
            return channels.get(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K> K channel(TypedSymbol<K> s) {
        if (hasChannel(s)) {
            return (K) typedChannels.get(s);
        }
        throw new IllegalArgumentException("No existing typed channel under name " + s.getName());
    }

    @Override
    public Set<Symbol> getChannels() {
        return Collections.unmodifiableSet(channels.keySet());
    }

    @Override
    public Set<TypedSymbol<?>> getTypedChannels() {
        return typedChannels.keySet();
    }
}
