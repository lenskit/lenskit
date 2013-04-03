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
import org.grouplens.lenskit.GlobalItemRecommender
import org.grouplens.lenskit.GlobalItemScorer
import org.grouplens.lenskit.ItemRecommender
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.knn.item.ItemItemGlobalRecommender
import org.grouplens.lenskit.knn.item.ItemItemGlobalScorer
import org.grouplens.lenskit.knn.item.ItemItemRecommender
import org.grouplens.lenskit.knn.item.ItemItemScorer

dumpGraph {
    output "${config.analysisDir}/item-item.dot"
    algorithm {
        bind ItemScorer to ItemItemScorer
        bind ItemRecommender to ItemItemRecommender
        bind GlobalItemScorer to ItemItemGlobalScorer
        bind GlobalItemRecommender to ItemItemGlobalRecommender
    }
}