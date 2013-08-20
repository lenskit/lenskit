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


import org.grouplens.lenskit.GlobalItemScorer
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.knn.NeighborhoodSize
import org.grouplens.lenskit.knn.item.ItemItemGlobalScorer
import org.grouplens.lenskit.knn.item.ItemItemScorer
import org.grouplens.lenskit.knn.user.UserUserItemScorer
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer

dumpGraph {
    output "${config.analysisDir}/item-item.dot"
    algorithm {
        bind ItemScorer to ItemItemScorer
        bind GlobalItemScorer to ItemItemGlobalScorer
        bind BaselinePredictor to ItemUserMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    }
}

dumpGraph {
    output "${config.analysisDir}/user-user.dot"
    algorithm {
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to 30
        bind BaselinePredictor to ItemUserMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        within(UserVectorNormalizer) {
            bind BaselinePredictor to UserMeanItemScorer
        }
    }
}