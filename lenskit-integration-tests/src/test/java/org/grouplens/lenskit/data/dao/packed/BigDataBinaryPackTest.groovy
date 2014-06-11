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
package org.grouplens.lenskit.data.dao.packed

import org.grouplens.lenskit.cursors.Cursor
import org.grouplens.lenskit.data.dao.ItemDAO
import org.grouplens.lenskit.data.dao.PrefetchingItemDAO
import org.grouplens.lenskit.data.dao.PrefetchingUserDAO
import org.grouplens.lenskit.data.dao.SortOrder
import org.grouplens.lenskit.data.dao.UserDAO
import org.grouplens.lenskit.data.event.Event
import org.grouplens.lenskit.data.event.Rating
import org.grouplens.lenskit.test.ML100KTestSuite
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

    UserDAO getUserDAO() {
        return new PrefetchingUserDAO(dao)
    }
    ItemDAO getItemDAO() {
        return new PrefetchingItemDAO(dao)
    }

    @Test
    public void testPackWithoutTimestamps() {
        def file = tempDir.newFile()
        BinaryRatingPacker packer = BinaryRatingPacker.open(file)
        try {
            Cursor<Rating> ratings = dao.streamEvents(Rating)
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
            Cursor<Rating> ratings = dao.streamEvents(Rating, SortOrder.TIMESTAMP)
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
            Cursor<Rating> ratings = dao.streamEvents(Rating)
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

        long lastTS = Long.MIN_VALUE;
        for (Event r: binDao.streamEvents()) {
            long ts = r.timestamp
            assertThat ts, greaterThanOrEqualTo(lastTS)
        }
        // try sorted!
        lastTS = Long.MIN_VALUE
        for (Rating r: binDao.streamEvents(Rating, SortOrder.TIMESTAMP)) {
            long ts = r.timestamp
            assertThat ts, greaterThanOrEqualTo(lastTS)
        }
    }
}
