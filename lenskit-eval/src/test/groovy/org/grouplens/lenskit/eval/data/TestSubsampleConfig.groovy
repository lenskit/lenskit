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
import org.grouplens.lenskit.eval.data.GenericDataSource;
import org.junit.Ignore
import org.grouplens.lenskit.eval.data.subsample.SubsampleCommand
import org.junit.Before
import org.junit.After
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.pref.SimplePreference;

/**
 * Test subsample configuration
 * @author Lingfei He
 */
class TestSubsampleConfig extends ConfigTestBase {

    def GenericDataSource dataSource = null;
    def SimplePreference pref = new SimplePreference(1, 2, 3); 
    def trainTestDir = Files.createTempDir()
    def ratings = [
        new SimpleRating(1, pref),
        new SimpleRating(2, pref),
        new SimpleRating(3, pref),
        new SimpleRating(4, pref),
        new SimpleRating(5, pref),
        new SimpleRating(6, pref),
        new SimpleRating(7, pref),
        new SimpleRating(8, pref),
        new SimpleRating(9, pref),
        new SimpleRating(10, pref),
        ];
    @Before
    void prepareFile() {
        DAOFactory factory = new EventCollectionDAO.Factory(ratings);
        dataSource = new GenericDataSource("sampleSource",factory);
    }
   
    @After
    void cleanUpFiles() {
        trainTestDir.deleteDir()
    }

    @Test
    void testBasicSubsample() {
        def obj = eval {
            subsample("sampleSource") {
                source dataSource
                fraction 0.2
                output trainTestDir.getAbsolutePath() + "/subsample.csv"
            }
        }
        
        DAOFactory factory = obj.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        try{
            Cursor<Rating> events = daoSnap.getEvents(Rating.class);
            List<Rating> ratings = Cursors.makeList(events);
     
            assertThat(ratings.size(), equalTo(2))
        } finally{
            daoSnap.close();
        }
    }

}
