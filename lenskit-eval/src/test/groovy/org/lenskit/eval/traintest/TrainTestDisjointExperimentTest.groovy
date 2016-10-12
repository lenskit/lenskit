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
package org.lenskit.eval.traintest

import org.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.eval.crossfold.CrossfoldMethods
import org.lenskit.eval.crossfold.Crossfolder
import org.lenskit.eval.traintest.predict.PredictEvalTask
import org.lenskit.knn.item.ItemItemScorer

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * This test attempts to reproduce <a href="https://github.com/lenskit/lenskit/issues/838">#838</a>.
 */
class TrainTestDisjointExperimentTest {
    TrainTestExperiment experiment

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    File file = null

    @Before
    void prepareFile() {
        file = folder.newFile("ratings.csv");
        file.append('1,1,3,881250949\n')
        file.append('2,2,3.5,881250949\n')
        file.append('3,3,3,881250949\n')
        file.append('4,4,3,881250949\n')
        file.append('5,5,3,881250949\n')
        file.append('6,6,3,881250949\n')
        file.append('7,7,3,881250949\n')
        file.append('8,8,3,881250949\n')
        file.append('9,9,3,881250949\n')
        file.append('10,10,3,881250949\n')
        experiment = new TrainTestExperiment()
    }

    @Test
    void testRun() {
        List<DataSet> sets = crossfoldRatings()
        experiment.addAlgorithm("ItemItem") {
            bind ItemScorer to ItemItemScorer
            bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
            within(UserVectorNormalizer) {
                bind(BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
            }
        }
        experiment.addDataSets(sets)
        experiment.addTask(new PredictEvalTask())
        def result = experiment.execute()
        def column = result.column("RMSE.ByUser")

        assertThat(result, notNullValue())
        assertTrue(column.contains(null))
    }

    private List<DataSet> crossfoldRatings() {
        def cf = new Crossfolder()
        cf.source = StaticDataSource.csvRatingFile(file.toPath())
        cf.setMethod(CrossfoldMethods.partitionEntities())
        cf.outputDir = folder.getRoot().toPath().resolve("splits")
        cf.partitionCount = 10
        cf.execute()
        cf.dataSets
    }
}
