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
package org.grouplens.lenskit.data.dao.packed

import com.google.common.collect.Iterables
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongSet
import org.grouplens.lenskit.collections.LongUtils
import org.grouplens.lenskit.cursors.Cursor
import org.grouplens.lenskit.data.dao.*
import org.grouplens.lenskit.data.event.Event
import org.grouplens.lenskit.data.event.Rating
import org.grouplens.lenskit.data.event.Ratings
import org.grouplens.lenskit.test.ML100KTestSuite
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BigDataBinaryPackTest extends ML100KTestSuite {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    private UserDAO udao
    private ItemDAO idao

    UserDAO getUserDAO() {
        if (udao == null) {
            udao = new PrefetchingUserDAO(ratingDAO)
        }
        return udao
    }
    ItemDAO getItemDAO() {
        if (idao == null) {
            idao = new PrefetchingItemDAO(ratingDAO)
        }
        return idao
    }

    @After
    public void clearCaches() {
        udao = null
        idao = null
    }

    @Test
    public void testPackWithoutTimestamps() {
        def file = tempDir.newFile()
        BinaryRatingPacker packer = BinaryRatingPacker.open(file)
        try {
            Cursor<Rating> ratings = ratingDAO.streamEvents(Rating)
            try {
                packer.writeRatings(ratings)
            } finally {
                ratings.close()
            }
        } finally {
            packer.close()
        }

        def users = userDAO
        def items = itemDAO

        def binDao = BinaryRatingDAO.open(file)
        assertThat binDao.itemIds, hasSize(items.itemIds.size())
        assertThat binDao.userIds, hasSize(users.userIds.size())
    }

    @Test
    public void testPackWithTimestamps() {
        def file = tempDir.newFile()
        BinaryRatingPacker packer = BinaryRatingPacker.open(file, BinaryFormatFlag.TIMESTAMPS)
        try {
            Cursor<Rating> ratings = ratingDAO.streamEvents(Rating, SortOrder.TIMESTAMP)
            try {
                packer.writeRatings(ratings)
            } finally {
                ratings.close()
            }
        } finally {
            packer.close()
        }

        def users = userDAO
        def items = itemDAO

        def binDao = BinaryRatingDAO.open(file)
        assertThat binDao.itemIds, hasSize(items.itemIds.size())
        assertThat binDao.userIds, hasSize(users.userIds.size())
    }

    @Test
    public void testPackWithOutOfOrderTimestamps() {
        def file = tempDir.newFile()
        BinaryRatingPacker packer = BinaryRatingPacker.open(file, BinaryFormatFlag.TIMESTAMPS)
        try {
            Cursor<Rating> ratings = ratingDAO.streamEvents(Rating)
            try {
                packer.writeRatings(ratings)
            } finally {
                ratings.close()
            }
        } finally {
            packer.close()
        }

        def binDao = BinaryRatingDAO.open(file)
        assertThat binDao.userIds, equalTo(userDAO.userIds)
        assertThat binDao.itemIds, equalTo(itemDAO.itemIds)

        checkSorted(binDao.streamEvents());
        // try sorted!
        checkSorted(binDao.streamEvents(Rating, SortOrder.TIMESTAMP));

        def uedao = new PrefetchingUserEventDAO(ratingDAO)
        def iedao = new PrefetchingItemEventDAO(ratingDAO)

        // and scan users
        for (long user: binDao.getUserIds()) {
            checkSorted(binDao.getEventsForUser(user))
            for (Event e: binDao.getEventsForUser(user)) {
                assertThat e.userId, equalTo(user)
            }
            assertThat(binDao.getEventsForUser(user).toSet(),
                       equalTo(uedao.getEventsForUser(user).toSet()))
        }

        // and items
        for (long item: binDao.getItemIds()) {
            checkSorted(binDao.getEventsForItem(item))
            for (Event e: binDao.getEventsForItem(item)) {
                assertThat e.itemId, equalTo(item)
            }
            assertThat(binDao.getEventsForItem(item).toSet(),
                       equalTo(iedao.getEventsForItem(item).toSet()))
        }
    }

    @Test
    public void testPackUpgrade() {
        def rng = new Random()
        def users = userDAO.userIds
        def items = itemDAO.itemIds
        def userMap = new Long2LongOpenHashMap(users.size())
        for (long u in users) {
            long up = u
            if (rng.nextFloat() < 0.05) {
                up += Integer.MAX_VALUE
            }
            userMap.put(u, up)
        }
        def itemMap = new Long2LongOpenHashMap(items.size())
        for (long i in items) {
            long ip = i
            if (rng.nextFloat() < 0.05) {
                ip += Integer.MAX_VALUE
            }
            itemMap.put(i, ip)
        }
        LongSet newUserSet = LongUtils.packedSet(userMap.values())
        LongSet newItemSet = LongUtils.packedSet(itemMap.values())

        int n = 0
        def file = tempDir.newFile()
        BinaryRatingPacker packer = BinaryRatingPacker.open(file)
        try {
            Cursor<Rating> ratings = ratingDAO.streamEvents(Rating)
            try {
                for (Rating r: ratings) {
                    packer.writeRating(Ratings.make(userMap[r.userId],
                                                    itemMap[r.itemId],
                                                    r.value))
                    n += 1
                }
            } finally {
                ratings.close()
            }
        } finally {
            packer.close()
        }

        def binDao = BinaryRatingDAO.open(file)

        assertThat(Iterables.size(binDao.streamEvents()), equalTo(n))
        assertThat binDao.userIds, equalTo(newUserSet)
        assertThat binDao.itemIds, equalTo(newItemSet)

        // and scan users
        for (long user: binDao.getUserIds()) {
            checkSorted(binDao.getEventsForUser(user))
            for (Event e: binDao.getEventsForUser(user)) {
                assertThat e.userId, equalTo(user)
                assertThat e.itemId, isIn(newItemSet)
            }
        }

        // and items
        for (long item: binDao.getItemIds()) {
            checkSorted(binDao.getEventsForItem(item))
            for (Event e: binDao.getEventsForItem(item)) {
                assertThat e.itemId, equalTo(item)
                assertThat e.userId, isIn(newUserSet)
            }
        }
    }

    private static void checkSorted(Iterable<? extends Event> events) {
        long last = Long.MIN_VALUE;
        for (Event e: events) {
            long ts = e.timestamp
            assertThat ts, greaterThanOrEqualTo(last)
        }
    }
}
