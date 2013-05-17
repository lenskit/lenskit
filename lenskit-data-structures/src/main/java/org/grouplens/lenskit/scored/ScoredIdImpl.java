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
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.grouplens.lenskit.symbols.Symbol;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

class ScoredIdImpl extends AbstractScoredId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private final double score;
    Reference2DoubleMap<Symbol> channelMap;

    public ScoredIdImpl(long id, double score) {
        this(id, score, null);
    }

    public ScoredIdImpl(long id, double score, Reference2DoubleMap<Symbol> channelMap) {
        this.id = id;
        this.score = score;
        if (channelMap != null) {
            this.channelMap = new Reference2DoubleArrayMap<Symbol>(channelMap);
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
        return channelMap != null && channelMap.containsKey(s);
    }

    @Override
    public double channel(Symbol s) {
        if (hasChannel(s)) {
            return channelMap.get(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }

    @Override
    public Set<Symbol> getChannels() {
        if (channelMap != null) {
            return channelMap.keySet();
        } else {
            return Collections.emptySet();
        }
    }
}
