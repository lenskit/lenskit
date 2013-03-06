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
package org.grouplens.lenskit.knn.item;

import static java.lang.Math.abs;

import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.ids.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Neighborhood scorer that computes the weighted average of neighbor scores.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@Shareable
@Singleton
public class WeightedAverageNeighborhoodScorer implements NeighborhoodScorer, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public double score(List<ScoredId> neighbors, SparseVector scores) {
        double sum = 0;
        double weight = 0;
        Iterator<ScoredId> nIter = neighbors.iterator();
        while (nIter.hasNext()) {
            ScoredId id = nIter.next();
            long oi = id.getId();
            double sim = id.getScore();
            weight += abs(sim);
            sum += sim * scores.get(oi);
        }
        if (weight > 0) {
            return sum / weight;
        } else {
            return Double.NaN;
        }
    }
}
