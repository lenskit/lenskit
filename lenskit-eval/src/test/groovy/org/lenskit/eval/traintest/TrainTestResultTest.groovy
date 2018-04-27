/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.eval.crossfold.Crossfolder
import org.lenskit.util.table.TableImpl

import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

/**
 * Test the result returned by the trainTest
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 *
 */
class TrainTestResultTest {
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
        cf.source = StaticDataSource.csvRatingFile(sourceFile.toPath())
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
