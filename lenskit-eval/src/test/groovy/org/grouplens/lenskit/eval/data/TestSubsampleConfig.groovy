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

import com.google.common.io.Files
import it.unimi.dsi.fastutil.longs.LongSet
import org.grouplens.lenskit.cursors.Cursors
import org.grouplens.lenskit.data.dao.*
import org.grouplens.lenskit.data.event.Rating
import org.grouplens.lenskit.data.event.SimpleRating
import org.grouplens.lenskit.data.pref.SimplePreference
import org.grouplens.lenskit.eval.data.subsample.SubsampleMode
import org.grouplens.lenskit.eval.script.ConfigTestBase
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat;

/**
 * Test subsample configuration
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TestSubsampleConfig extends ConfigTestBase {

    def GenericDataSource dataSource = null;
    def trainTestDir = Files.createTempDir();
    def pref = [
        new SimplePreference(1, 1, 3),
        new SimplePreference(1, 2, 2),
        new SimplePreference(1, 3, 5),
        new SimplePreference(2, 2, 6),
        new SimplePreference(2, 4, 8),
        new SimplePreference(3, 1, 1),
        new SimplePreference(3, 3, 4),
        new SimplePreference(3, 4, 0),
        new SimplePreference(4, 2, 7),
        new SimplePreference(4, 4, 9),
        ];
    
    def ratings = [
        new SimpleRating(1, pref[0]),
        new SimpleRating(2, pref[1]),
        new SimpleRating(3, pref[2]),
        new SimpleRating(4, pref[3]),
        new SimpleRating(5, pref[4]),
        new SimpleRating(6, pref[5]),
        new SimpleRating(7, pref[6]),
        new SimpleRating(8, pref[7]),
        new SimpleRating(9, pref[8]),
        new SimpleRating(10, pref[9]),
        ];

    @Before
    void prepareDataSource() {
        dataSource = new GenericDataSource("sampleSource", new EventCollectionDAO(ratings));
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
                output new File(trainTestDir, "subsample.csv")
            }
        }
        
        EventDAO dao = obj.getEventDAO();
        List<Rating> ratings = Cursors.makeList(dao.streamEvents(Rating.class));

        assertThat(ratings.size(), equalTo(2))
    }
    
    @Test
    void testSubsampleUser() {
        def obj = eval {
            subsample("sampleSource") {
                source dataSource
                fraction 0.5
                mode SubsampleMode.USER
                output new File(trainTestDir, "subsample.csv")
            }
        }
        
        UserDAO dao = obj.getUserDAO();
        UserEventDAO userEvents = obj.getUserEventDAO();
        UserEventDAO sourceDAO = dataSource.getUserEventDAO();

        LongSet userList = dao.getUserIds();

        assertThat(userList.size(), equalTo(2))

        for (Long user: userList) {
            List<Rating> userRating = userEvents.getEventsForUser(user, Rating.class);
            List<Rating> sourceUserRating = sourceDAO.getEventsForUser(user, Rating.class);
            assertThat(userRating.size(), equalTo(sourceUserRating.size()));
        }
    }
    
    @Test
    void testSubsampleItem() {
        def obj = eval {
            subsample("sampleSource") {
                source dataSource
                fraction 0.75
                mode SubsampleMode.ITEM
                output new File(trainTestDir, "subsample.csv")
            }
        }

        ItemDAO dao = obj.getItemDAO();
        ItemEventDAO itemEvents = obj.getItemEventDAO();
        ItemEventDAO sourceDAO = dataSource.getItemEventDAO();

        LongSet itemSet = dao.getItemIds();

        assertThat(itemSet, hasSize(3))

        for (Long item: itemSet) {
            List<Rating> itemRating = itemEvents.getEventsForItem(item, Rating.class);
            List<Rating> sourceItemRating = sourceDAO.getEventsForItem(item, Rating.class);
            assertThat(itemRating.size(), equalTo(sourceItemRating.size()));
        }
    }
}
