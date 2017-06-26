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
package org.lenskit.pf;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class HPFItemScorer extends AbstractItemScorer {
    private final HPFModel model;
    private final boolean isProbPredition;

    @Inject
    public HPFItemScorer(HPFModel mod,
                         @IsProbabilityPrediciton boolean probPred) {
        model = mod;
        isProbPredition = probPred;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        RealVector uvec = model.getUserVector(user);
        if (uvec == null) {
            return Results.newResultMap();
        }

        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            RealVector ivec = model.getItemVector(item);
            if (ivec != null) {
                double score = uvec.dotProduct(ivec);
                if (isProbPredition) {
                    score = 1 - Math.exp(-score);
                }
                results.add(Results.create(item, score));
            }
        }
        return Results.newResultMap(results);
    }
}
