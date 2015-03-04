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

package org.grouplens.lenskit.data.dao.packed;

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.UserHistory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.lenskit.org">Lenskit Research</a>
 */

public class LimitedBinaryRatingDAOTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public  BinaryRatingDAO dao;
    List<Rating> ratings;

    @Before
    public void createDao() throws IOException {
        ImmutableList.Builder<Rating> bld = ImmutableList.builder();
        bld .add(Ratings.make(12, 102, 2.5, 1050L))
                .add(Ratings.make(12, 120, 4.5, 1650L))
                .add(Ratings.make(13, 102, 3.5, 1000L))
                .add(Ratings.make(13, 111, 4.5, 1200L))
                .add(Ratings.make(39, 105, 3.5, 1000L))
                .add(Ratings.make(39, 120, 4.5, 1650L))
                .add(Ratings.make(40, 111, 4.5, 1050L))
                .add(Ratings.make(40, 120, 2.5, 1650L))
                .add(Ratings.make(41, 105, 2.5, 1400L))
                .add(Ratings.make(41, 111, 3.5, 1700L))
                .add(Ratings.make(42, 115, 3.5, 1700L))
                .add(Ratings.make(42, 120, 2.5, 1650L));
        ratings = bld.build();

        File file = folder.newFile("ratings.bin");
        BinaryRatingPacker packer = BinaryRatingPacker.open(file, BinaryFormatFlag.TIMESTAMPS);
        try {
            packer.writeRatings(ratings);
        } finally {
            packer.close();
        }

        dao = BinaryRatingDAO.open(file);

    }

    /**
     * Test with the minimum timestamp in DAO,
     * it should return empty DAO
     * @throws IOException
     */
    @Test
    public void testLimitBelowMin() throws IOException {
        BinaryRatingDAO brDao = dao.createWindowedView(500L);
        assertThat(Cursors.makeList(brDao.streamEvents()),
                hasSize(0));
        assertThat(brDao.getUserIds(), hasSize(0));
        assertThat(brDao.getItemIds(), hasSize(0));
    }

    /**
     * Test for timestamp greater than highest timestamp in ratings list,
     * all the values in ratings list should match
     * @throws IOException
     */
    @Test
    public void testLimitAboveMax() throws IOException {

        dao = dao.createWindowedView(2000L);
        assertThat(Cursors.makeList(dao.streamEvents()),
                hasSize(14));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class)),
                equalTo(ratings));
    }


    /**
     * Test for the items who have ratings '<' or '>='  timestamp,
     * it should not return item which have any rating >= timestamp
     * @throws IOException
     */
    @Test
    public void testLimitItems() throws IOException {
        BinaryRatingDAO brDao  = dao.createWindowedView(1650L);
        assertThat(Cursors.makeList(brDao.streamEvents()),
                hasSize(6));
        assertThat(brDao.getItemIds(), containsInAnyOrder(102L, 105L, 111L));
        assertFalse(brDao.getItemIds().contains(120));/*check if it is correct syntax*/
        assertThat(brDao.getEventsForItem(120), nullValue());
        assertThat(brDao.getEventsForItem(105), hasSize(1));
        assertThat(brDao.getEventsForItem(105, Rating.class),
                contains(Ratings.make(41, 105, 2.5)));
        assertThat(brDao.getEventsForItem(111, Rating.class),
                containsInAnyOrder(Ratings.make(13, 111, 4.5), Ratings.make(40, 111, 4.5)));

        /*check if assertNotEqual works properly*/
        assertNotEquals(brDao.getUsersForItem(111), containsInAnyOrder(41L));

    }


    /**
     * Test for the users who have ratings '<' or '>='  timestamp,
     * it should not return user which have any rating >= timestamp
     * @throws IOException
     */
    @Test
    public void testLimitUsers() throws IOException {
        BinaryRatingDAO brDao = dao.createWindowedView(1400L);
        assertThat(Cursors.makeList(brDao.streamEvents()),
                hasSize(5));
        assertThat(brDao.getUserIds(),  containsInAnyOrder(12L, 13L, 39L, 40L));
        assertFalse(brDao.getUserIds().contains(42));/*check if it is correct syntax*/
        assertThat(brDao.getEventsForUser(42), nullValue());
        assertThat(brDao.getEventsForUser(41), hasSize(1));
        assertThat(brDao.getEventsForUser(41), nullValue());
        assertThat(brDao.getEventsForUser(13, Rating.class),
                containsInAnyOrder(Ratings.make(13, 102, 3.5),
                        Ratings.make(13, 111, 4.5)));
    }

    /**
     * Test to create a windowed view with lower timestamp than current,
     * it should create DAO with new timestamp < current timestamp
     * @throws IOException
     */
    @Test
    public void testDecreasedTimestamp() throws IOException {
        BinaryRatingDAO highLimitDao = dao.createWindowedView(1700L);
        BinaryRatingDAO lowLimitDao = highLimitDao.createWindowedView(1200L);
        assertThat(Cursors.makeList(lowLimitDao.streamEvents()),
                hasSize(4));
        assertThat(lowLimitDao.getEventsForUser(12), hasSize(1));
        assertThat(lowLimitDao.getUserIds(), containsInAnyOrder(12L,13L,39L,40L));
        assertThat(lowLimitDao.getItemIds(), containsInAnyOrder(102L,105L,111L));

    }

    /**
     * Test to create a windowed view with higher timestamp than current,
     * it should create DAO with timestamp = current timestamp
     * @throws IOException
     */

    @Test
    public void testIncreasedTimestamp() throws IOException {
        BinaryRatingDAO lowLimitDao = dao.createWindowedView(1650L);
        BinaryRatingDAO highLimitDao = lowLimitDao.createWindowedView(1700L);

        assertThat(Cursors.makeList(highLimitDao.streamEvents()),
                hasSize(7));
        assertThat(highLimitDao.getEventsForItem(120), nullValue());
        assertThat(highLimitDao.getEventsForUser(42), hasSize(1));
        assertFalse(highLimitDao.getEventsForItem(111).contains(1700L));
        assertThat(highLimitDao.getUsersForItem(111), hasSize(2));

    }

    /**
     *Test all the events  after limit and verify data
     * @throws IOException
     */
    @Test
    public void testEventsAfterLimit() throws IOException{
        BinaryRatingDAO brDao  = dao.createWindowedView(1650L);
        assertThat(Cursors.makeList(brDao.streamEvents()),
                hasSize(6));

        //test getters for user
        assertThat(dao.getUserIds(), containsInAnyOrder(12L,13L,39L,40L));
        assertThat(dao.getUsersForItem(105), containsInAnyOrder(39L,41L));
        assertThat(dao.getUsersForItem(120), nullValue());
        assertThat(dao.getEventsForUser(39, Rating.class),
                contains(Ratings.make(39, 105, 3.5)));
        assertThat(dao.getEventsForUser(13, Rating.class),
                containsInAnyOrder(Ratings.make(13, 102, 3.5),
                        Ratings.make(13, 111, 4.5)));
        assertThat(dao.getEventsForUser(42), nullValue());

        //test getters for item
        assertThat(dao.getItemIds(), containsInAnyOrder(102L, 105L, 111L));
        assertThat(dao.getEventsForItem(12, Rating.class),
                contains(Ratings.make(12, 102, 2.5)));
        assertThat(dao.getEventsForItem(105, Rating.class),
                containsInAnyOrder(Ratings.make(39, 105, 3.5),
                        Ratings.make(41, 105, 2.5)));
        assertThat(dao.getEventsForItem(120), nullValue());

        //test streamEventsByUser
        List<UserHistory<Event>> histories = Cursors.makeList(dao.streamEventsByUser());
        assertThat(histories, hasSize(6));
        assertThat(histories.get(0).getUserId(), equalTo(12L));
        assertThat(histories.get(0),
                equalTo(dao.getEventsForUser(12L)));
        assertThat(histories.get(1).getUserId(), equalTo(13L));
        assertThat(histories.get(1),
                equalTo(dao.getEventsForUser(13)));

        //test streamEvents
        List<Rating> events = Cursors.makeList(dao.streamEvents(Rating.class, SortOrder.USER));
        assertThat(events, hasSize(6));
        assertThat(events.get(0).getUserId(), equalTo(12L));

        events = Cursors.makeList(dao.streamEvents(Rating.class, SortOrder.ITEM));
        assertThat(events, hasSize(6));
        assertThat(events.get(0).getUserId(), equalTo(12L));
        assertThat(events.get(0).getItemId(), equalTo(102L));
        assertThat(events.get(2).getItemId(), equalTo(105L));
    }
}