/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
import org.grouplens.lenskit.baseline.GlobalMeanPredictor
import org.grouplens.lenskit.baseline.UserMeanPredictor
import org.grouplens.lenskit.baseline.ItemMeanPredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric

def buildDir = System.getProperty("project.build.directory", ".")

def baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

def ml100k = crossfold("ml-100k") {
    source csvfile {
        file "${buildDir}/ml-100k/u.data"
        delimiter "\t"
        domain {
            minimum 1.0
            maximum 5.0
            precision 1.0
        }
    }
    train "${buildDir}/ml-100k.train.%d.csv"
    test "${buildDir}/ml-100k.test.%d.csv"
}

trainTest {
    depends ml100k

    output "${buildDir}/eval-output/baselines.csv"
    dataset ml100k

    numRecs 5
    
    metric CoveragePredictMetric
    metric MAEPredictMetric
    metric RMSEPredictMetric

    for (bl in baselines) {
        algorithm(bl.simpleName) {
            bind RatingPredictor to BaselineRatingPredictor
            bind BaselinePredictor to bl
        }
    }
}
