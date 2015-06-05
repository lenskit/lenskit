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


import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

class ItemRecommenderCompatWrapper implements org.grouplens.lenskit.ItemRecommender {
    private final ItemRecommender delegate;

    public ItemRecommenderCompatWrapper(ItemRecommender rec) {
        delegate = rec;
    }

    private List<ScoredId> makeResultList(ResultList results) {
        ScoredIdListBuilder bld = ScoredIds.newListBuilder(results.size());
        for (Result r: results) {
            bld.add(r.getId(), r.getScore());
        }
        return bld.build();
    }

    @Override
    public List<ScoredId> recommend(long user) {
        return makeResultList(delegate.recommend(user));
    }

    @Override
    public List<ScoredId> recommend(long user, int n) {
        return makeResultList(delegate.recommend(user, n));
    }

    @Override
    public List<ScoredId> recommend(long user, @Nullable Set<Long> candidates) {
        return makeResultList(delegate.recommend(user, candidates));
    }

    @Override
    public List<ScoredId> recommend(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return makeResultList(delegate.recommend(user, n, candidates, exclude));
    }
}
