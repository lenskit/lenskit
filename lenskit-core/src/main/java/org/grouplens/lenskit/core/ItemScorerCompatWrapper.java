/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.core;


import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import javax.annotation.Nonnull;
import java.util.Collection;

class ItemScorerCompatWrapper implements org.grouplens.lenskit.ItemScorer {
    private final ItemScorer delegate;

    public ItemScorerCompatWrapper(ItemScorer rec) {
        delegate = rec;
    }

    @Override
    public double score(long user, long item) {
        Result r = delegate.score(user, item);
        if (r == null) {
            return Double.NaN;
        } else {
            return r.getScore();
        }
    }

    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        MutableSparseVector scores = MutableSparseVector.create(items);
        score(user, scores);
        return scores;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        ResultMap results = delegate.score(user, scores.keyDomain());
        for (Result r: results) {
            scores.set(r.getId(), r.getScore());
        }
    }
}
