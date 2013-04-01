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

import it.unimi.dsi.fastutil.longs.LongArrayList;

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
import org.grouplens.lenskit.cursors.LongCursor;
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
    def trainTestDir = Files.createTempDir();
    def pref = [
        new SimplePreference(1, 1, 3),
        new SimplePreference(2, 2, 3),
        new SimplePreference(3, 3, 3),
        new SimplePreference(4, 4, 3),
        new SimplePreference(5, 5, 3),
        ];
    
    def ratings = [
        new SimpleRating(1, pref[1]),
        new SimpleRating(2, pref[1]),
        new SimpleRating(3, pref[2]),
        new SimpleRating(4, pref[2]),
        new SimpleRating(5, pref[3]),
        new SimpleRating(6, pref[3]),
        new SimpleRating(7, pref[4]),
        new SimpleRating(8, pref[4]),
        new SimpleRating(9, pref[0]),
        new SimpleRating(10, pref[0]),
        ];
    @Before
    void prepareDataSource() {
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
//                type "Rating"
                output new File(trainTestDir, "subsample.csv")
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
    
    @Test
    void testSubsampleUser() {
        def obj = eval {
            subsample("sampleSource") {
                source dataSource
                fraction 0.4
                type "User"
                output new File(trainTestDir, "subsample.csv")
            }
        }
        
        DAOFactory factory = obj.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        try{
            LongCursor users = daoSnap.getUsers();
            LongArrayList userList = Cursors.makeList(users);
            
            Cursor<Rating> userEvent = daoSnap.getUserEvents(userList.getLong(1),Rating.class);
            List<Rating> userRating = Cursors.makeList(userEvent);
            
            Cursor<Rating> events = daoSnap.getEvents(Rating.class);
            List<Rating> ratings = Cursors.makeList(events);
     
            assertThat(userList.size(), equalTo(2))
            assertThat(userRating.size(), equalTo(2))
            assertThat(ratings.size(), equalTo(4))
        } finally{
            daoSnap.close();
        }
    }
    
    @Test
    void testSubsampleItem() {
        def obj = eval {
            subsample("sampleSource") {
                source dataSource
                fraction 0.6
                type "Item"
                output new File(trainTestDir, "subsample.csv")
            }
        }
        
        DAOFactory factory = obj.getDAOFactory();
        DataAccessObject daoSnap = factory.snapshot();
        try{
            LongCursor items = daoSnap.getItems();
            LongArrayList itemList = Cursors.makeList(items);
            
            Cursor<Rating> itemEvent = daoSnap.getItemEvents(itemList.getLong(1),Rating.class);
            List<Rating> itemRating = Cursors.makeList(itemEvent);
            
            Cursor<Rating> events = daoSnap.getEvents(Rating.class);
            List<Rating> ratings = Cursors.makeList(events);
            
            assertThat(itemRating.size(), equalTo(2))
            assertThat(itemList.size(), equalTo(3))
            assertThat(ratings.size(), equalTo(6))
        } finally{
            daoSnap.close();
        }
    }
}
