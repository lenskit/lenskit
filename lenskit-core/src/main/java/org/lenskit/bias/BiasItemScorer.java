/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Score items using a user-item bias model.  This scorer is good as a baseline scorer for many situations.
 */
public class BiasItemScorer extends AbstractItemScorer {
    private final BiasModel model;

    /**
     * Construct a new scorer.
     * @param bias The bias model to use.
     */
    @Inject
    public BiasItemScorer(BiasModel bias) {
        model = bias;
    }

    @Override
    public Result score(long user, long item) {
        return Results.create(item, model.getIntercept() + model.getUserBias(user) + model.getItemBias(item));
    }

    @Nonnull
    @Override
    public Map<Long, Double> score(long user, @Nonnull Collection<Long> items) {
        LongSet itemSet = LongUtils.frozenSet(items);
        double base = model.getIntercept() + model.getUserBias(user);
        return LongUtils.flyweightMap(itemSet, iid -> base + model.getItemBias(iid));
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> results = new ArrayList<>();
        double base = model.getIntercept() + model.getUserBias(user);
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            results.add(Results.create(item, base + model.getItemBias(item)));
        }
        return Results.newResultMap(results);
    }
}
