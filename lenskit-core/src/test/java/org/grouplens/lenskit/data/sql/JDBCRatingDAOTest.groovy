package org.grouplens.lenskit.data.sql

import groovy.sql.Sql
import org.grouplens.lenskit.cursors.Cursors
import org.grouplens.lenskit.data.dao.SortOrder
import org.grouplens.lenskit.data.event.Rating
import org.grouplens.lenskit.data.event.Ratings
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.sql.Connection
import java.sql.DriverManager

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JDBCRatingDAOTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    private Connection cxn
    private JDBCRatingDAO dao

    @BeforeClass
    public static void configureDerby() {
        System.setProperty("derby.stream.error.field", "java.lang.System.out")
        System.setProperty("derby.stream.error.logSeverityLevel", "30000")
    }

    @Before
    public void makeConnection() {
        cxn = DriverManager.getConnection("jdbc:derby:" + folder.root.absolutePath + "/db;create=true")
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
