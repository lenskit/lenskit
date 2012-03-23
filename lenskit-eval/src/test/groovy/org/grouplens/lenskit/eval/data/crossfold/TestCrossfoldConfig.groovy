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

/**
 * Test crossfold configuration
 * @author Michael Ekstrand
 */
class TestCrossfoldConfig extends ConfigTestBase {
    @Test
    void testBasicCrossfold() {
        def obj = eval {
            crossfold("ml-100k") {
                source "ml-100k.csv"
                partitions 10
                holdout 0.5
                order RandomOrder
            }
        }
        def cf = obj as CrossfoldTask
        assertThat(cf.name, equalTo("ml-100k"))
        assertThat(cf.source, instanceOf(CSVDataSource))
        assertThat(cf.partitionCount, equalTo(10))
        assertThat(cf.holdout.order, instanceOf(RandomOrder))
        assertThat(cf.holdout.partitionMethod, instanceOf(FractionPartition))
        assertThat(cf.holdout.partitionMethod.fraction, closeTo(0.5, 1.0e-6))
    }

    @Test @Ignore("wrapper functions not supported")
    void testWrapperFunction() {
        def obj = eval {
            crossfold("ml-100k") {
                source "ml-100k.csv"
                wrapper {
                    it
                }
            }
        }
        def cf = obj as CrossfoldTask
        assertThat(cf.name, equalTo("ml-100k"))
        assertThat(cf.source, instanceOf(CSVDataSource))
        assertThat(cf.getDAOWrapper(), notNullValue())
    }
}
