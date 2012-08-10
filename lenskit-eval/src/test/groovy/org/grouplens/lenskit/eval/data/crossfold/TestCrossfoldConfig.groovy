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
package org.grouplens.lenskit.eval.data.crossfold

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test
import org.grouplens.lenskit.eval.data.CSVDataSource
import org.junit.Ignore
import org.grouplens.lenskit.eval.data.traintest.TTDataSet
import org.junit.Before
import org.junit.After
import org.grouplens.lenskit.data.dao.DAOFactory

/**
 * Test crossfold configuration
 * @author Michael Ekstrand
 */
class TestCrossfoldConfig extends ConfigTestBase {

    def buildDir = System.getProperty("project.build.directory", ".")
	def file = File.createTempFile("tempRatings", "csv")
	
    @Before
    void prepareFile() {
        file.deleteOnExit()
		file.append('19,242,3,881250949\n')
        file.append('296,242,3.5,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
    }
	
	@After
	void cleanUpFiles() {
		file.delete()
		new File("${buildDir}/temp").deleteDir()
	}

    @Test
    void testBasicCrossfold() {
        def obj = eval{
            crossfold("tempRatings") {
                source file
                partitions 10
                holdout 0.5
                order RandomOrder
				train "${buildDir}/temp/ratings.train.%d.csv"
				test "${buildDir}/temp/ratings.test.%d.csv"
            }
        }
        assertThat(obj.size(), equalTo(10))
        assertThat(obj[1], instanceOf(TTDataSet))
        def tt = obj as List<TTDataSet>
        assertThat(tt[1].name, equalTo("TTData"))
        assertThat(tt[2].attributes.get("Partition"), equalTo(2))
        assertThat(tt[3].testFactory, instanceOf(DAOFactory))
    }

    @Test @Ignore("wrapper functions not supported")
    void testWrapperFunction() {
        def obj = eval {
            crossfold("tempRatings") {
                source file
                wrapper {
                    it
                }
				train "${buildDir}/temp/ratings.train.%d.csv"
				test "${buildDir}/temp/ratings.test.%d.csv"
            }
        }
        obj = obj as List<TTDataSet>
        assertThat(obj.size(), equalTo(10))
        assertThat(obj[1], instanceOf(TTDataSet))
    }
}
