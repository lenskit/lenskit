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

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.grouplens.lenskit.symbols.Symbol;

/**
 * A mutable version of a {@code ScoredId}.
 * WARNING: This class should not be used under any circumstances, except to
 * implement fast iteration. We make no guarantees about the functionality of
 * this class in any other situation.
 */
public class MutableScoredId extends ScoredId {

    public MutableScoredId(long id, double score) {
        super(id, score);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setChannelMap(Reference2DoubleMap<Symbol> channelMap) {
        this.channelMap = channelMap;
    }

    public Reference2DoubleMap<Symbol> getChannelMap() {
        return channelMap;
    }
}
