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
package org.grouplens.lenskit.data.sql

import groovy.sql.Sql
import org.grouplens.lenskit.cursors.Cursors
import org.grouplens.lenskit.data.dao.SortOrder
import org.lenskit.data.ratings.Rating
import org.lenskit.data.ratings.Ratings
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.sql.Connection
import java.sql.DriverManager

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JDBCRatingDAOTest {
    private Connection cxn
    private JDBCRatingDAO dao

    @Before
    public void makeConnection() {
        cxn = DriverManager.getConnection("jdbc:h2:mem:")
        def sql = new Sql(cxn)
        sql.execute('CREATE TABLE ratings (userId INTEGER NOT NULL, itemId INTEGER NOT NULL, rating REAL NOT NULL, ratingTime BIGINT NOT NULL)')
        def ratings = sql.dataSet('ratings')
        ratings.add userId: 1, itemId: 1, rating: 4.5, ratingTime: 50
        ratings.add userId: 1, itemId: 2, rating: 3.5, ratingTime: 51
        ratings.add userId: 3, itemId: 1, rating: 2.5, ratingTime: 40

        dao = JDBCRatingDAO.newBuilder()
                           .setTableName('ratings')
                           .setUserColumn('userId')
                           .setItemColumn('itemId')
                           .setRatingColumn('rating')
                           .setTimestampColumn('ratingTime')
                           .build(cxn)
    }

    @After
    public void closeConnection() {
        dao.close()
    }

    @Test
    public void testGetUserRatings() {
        assertThat dao.getEventsForUser(1)*.itemId, contains(1l, 2l)
        assertThat dao.getEventsForUser(3)*.itemId, contains(1l)
        assertThat dao.getEventsForUser(2), nullValue()
    }

    @Test
    public void testGetItemRatings() {
        assertThat dao.getEventsForItem(1)*.userId, contains(1l, 3l)
        assertThat dao.getEventsForItem(2)*.userId, contains(1l)
        assertThat dao.getEventsForItem(3), nullValue()
    }

    @Test
    public void testGetAllRatings() {
        def ratings = Cursors.makeList(dao.streamEvents(Rating, SortOrder.TIMESTAMP))
        assertThat ratings, contains(Ratings.make(3, 1, 2.5, 40),
                                     Ratings.make(1, 1, 4.5, 50),
                                     Ratings.make(1, 2, 3.5, 51))
    }
}
