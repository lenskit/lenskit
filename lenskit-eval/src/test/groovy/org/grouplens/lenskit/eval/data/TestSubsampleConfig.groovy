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
package org.grouplens.lenskit.eval.data

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import java.util.List;

import com.google.common.io.Files
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test
import org.grouplens.lenskit.eval.data.CSVDataSource
import org.junit.Ignore
import org.grouplens.lenskit.eval.data.subsample.SubsampleCommand
import org.junit.Before
import org.junit.After
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;

/**
 * Test subsample configuration
 * @author Lingfei He
 */
class TestSubsampleConfig extends ConfigTestBase {

    def file = File.createTempFile("tempRatings", "csv")
    def trainTestDir = Files.createTempDir()

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
        trainTestDir.deleteDir()
    }

    @Test
    void testBasicSubsample() {
        def obj = eval {
            subsample("tempRatings") {
                source file
                subsampleFraction 0.2
                output trainTestDir.getAbsolutePath() + "/subsample.csv"
            }
        }
        
        DAOFactory factory = obj.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        Cursor<Rating> events = daoSnap.getEvents(Rating.class);
        List<Rating> ratings = Cursors.makeList(events);
        
        assertThat(ratings.size(), equalTo(2))
        daoSnap.close();
    }

}
