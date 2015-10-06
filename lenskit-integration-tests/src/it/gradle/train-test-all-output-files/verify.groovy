/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import static com.xlson.groovycsv.CsvParser.parseCsv
import org.lenskit.knn.item.model.SimilarityMatrixModel

import java.util.zip.GZIPInputStream

import static org.grouplens.lenskit.util.test.ExtraMatchers.existingFile
import static org.grouplens.lenskit.util.test.ExtraMatchers.hasLineCount
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.isEmptyOrNullString
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.equalTo

File resultsFile = new File("results.csv")
File userFile = new File("users.csv")
File predictFile = new File("predictions.csv")
File recommendFile = new File("recommendations.csv.gz")

assertThat("output file exists",
           resultsFile, allOf(existingFile(),
                              hasLineCount(equalTo(11))));
assertThat("output file exists",
           userFile, existingFile());
assertThat("output file exists",
           predictFile, existingFile());
assertThat("output file exists",
           recommendFile, existingFile());
resultsFile.withReader { rdr ->
    def results = parseCsv(rdr)
    for (row in results) {
        assertThat(row.Partition, not(isEmptyOrNullString()))
        assertThat(row.PredRankAcc, not(equalTo(true)))
        assertThat(row.getProperty('RMSE.ByUser'), not(equalTo(true)))
        assertThat(row.getProperty('RMSE.ByRating'), not(equalTo(true)))
    }
}

assertThat(new File('build/crossfold.out/part01.train.pack'),
           existingFile())

// Verify that we have 5 distinct item-item models
File cacheDir = new File("cache")
def objects = new HashMap<String,Integer>()
cacheDir.eachFile { file ->
    if (!file.name.matches(/^\./)) {
        def obj = file.withInputStream {
            def stream = new GZIPInputStream(it)
            stream.withObjectInputStream(getClass().classLoader) {
                it.readObject()
            }
        }
        def cls = obj.class.name
        objects[cls] = objects.get(cls, 0) + 1
    }
}
// FIXME Re-enable this assertion when sharing is fixed
// assertThat objects[SimilarityMatrixModel.name], equalTo(5)
