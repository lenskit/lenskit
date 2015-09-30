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
package org.lenskit.eval.traintest

import org.grouplens.lenskit.data.source.TextDataSourceBuilder
import org.grouplens.lenskit.eval.script.ConfigTestBase
import org.lenskit.util.table.TableImpl
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.eval.crossfold.Crossfolder

import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

/**
 * Test the result returned by the trainTest
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 *
 */
class TrainTestResultTest extends ConfigTestBase {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder()

    File sourceFile = null

    @Before
    void prepareFile() {
        sourceFile = workDir.newFile("ratings.csv")
        sourceFile.append('19,242,3,881250949\n')
        sourceFile.append('296,242,3.5,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
        sourceFile.append('196,242,3,881250949\n')
    }

    @Test
    void testResult() {
        def cf = new Crossfolder("tempRatings")
        cf.outputDir = workDir.newFolder().toPath()
        cf.partitionCount = 5
        cf.source = new TextDataSourceBuilder().setFile(sourceFile)
                                               .setDelimiter(",")
                                               .build()
        cf.execute()
        def exp = new TrainTestExperiment()
        exp.addDataSets(cf.dataSets)
        def aib = new AlgorithmInstanceBuilder("ItemUserMean")
        aib.config.bind(ItemScorer).to(UserMeanItemScorer)
        aib.config.bind(BaselineScorer, ItemScorer).to(ItemMeanRatingItemScorer)
        exp.addAlgorithm(aib.build())
        def result = exp.execute()
        assertThat(result, instanceOf(TableImpl))
    }
}
