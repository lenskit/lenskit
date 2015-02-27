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
package org.grouplens.lenskit.eval.data.traintest

import org.grouplens.lenskit.data.text.TextEventDAO
import org.grouplens.lenskit.specs.SpecificationContext
import org.grouplens.lenskit.util.test.MiscBuilders
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

class GenericTTSpecHandlerTest {
    @Test
    public void testLoadFromSpec() {
        def cfg = MiscBuilders.configObj {
            train {
                type "text"
                file "train.csv"
                domain {
                    minimum 1.0
                    maximum 5.0
                    precision 1.0
                }
            }
            test {
                type "text"
                file "test.csv"
                domain {
                    minimum 1.0
                    maximum 5.0
                    precision 1.0
                }
            }
        }
        def set = SpecificationContext.create().build(TTDataSet, cfg)
        assertThat(set.trainingDAO, instanceOf(TextEventDAO))
        assertThat(set.testDAO, instanceOf(TextEventDAO))
        assertThat(set.trainingData.file.name, equalTo("train.csv"))
        assertThat(set.testData.file.name, equalTo("test.csv"))
    }
}
