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
package org.grouplens.lenskit.knn.user;

import javax.inject.Inject;

import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * A recommender and scorer using user-user collaborative filtering.
 * Neighbor user are aggregated using weighted averaging.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @deprecated Just use {@link org.grouplens.lenskit.basic.TopNItemRecommender}.
 */
@Deprecated
public class UserUserRecommender extends TopNItemRecommender {
    protected final UserUserItemScorer predictor;

    @Inject
    public UserUserRecommender(DataAccessObject dao, UserUserItemScorer pred) {
        super(dao, pred);
        predictor = pred;
    }
}
