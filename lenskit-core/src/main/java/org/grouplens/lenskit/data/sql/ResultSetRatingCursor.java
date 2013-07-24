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
package org.grouplens.lenskit.data.sql;

import org.grouplens.lenskit.cursors.AbstractPollingCursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cursor to extract ratings from a result set.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ResultSetRatingCursor extends AbstractPollingCursor<Rating> {
    private ResultSet resultSet;
    private boolean hasTimestampColumn;
    private MutableRating rating;

    public ResultSetRatingCursor(PreparedStatement stmt) throws SQLException {
        rating = new MutableRating();
        resultSet = stmt.executeQuery();
        try {
            // SUPPRESS CHECKSTYLE MagicNumber
            hasTimestampColumn = resultSet.getMetaData().getColumnCount() > 4;
        } catch (SQLException e) {
            resultSet.close();
            throw e;
        } catch (RuntimeException e) {
            resultSet.close();
            throw e;
        }
    }

    @Override
    public Rating poll() {
        try {
            if (!resultSet.next()) {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }

        try {
            rating.setUserId(resultSet.getLong(JDBCRatingDAO.COL_USER_ID));
            if (resultSet.wasNull()) {
                throw new DatabaseAccessException("Unexpected null user ID");
            }
            rating.setItemId(resultSet.getLong(JDBCRatingDAO.COL_ITEM_ID));
            if (resultSet.wasNull()) {
                throw new DatabaseAccessException("Unexpected null item ID");
            }
            rating.setRating(resultSet.getDouble(JDBCRatingDAO.COL_RATING));
            if (resultSet.wasNull()) {
                rating.setRating(Double.NaN);
            }
            long ts = -1;
            if (hasTimestampColumn) {
                ts = resultSet.getLong(JDBCRatingDAO.COL_TIMESTAMP);
                if (resultSet.wasNull()) {
                    ts = -1;
                }
            }
            rating.setTimestamp(ts);
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }

        return rating;
    }

    @Override
    public Rating copy(Rating r) {
        return Ratings.copyBuilder(r).build();
    }

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }
}
