/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

public class SharedDAO implements RatingDataAccessObject, DataAccessObjectManager<SharedDAO> {
    private final RatingDataAccessObject dao;

    public SharedDAO(RatingDataAccessObject openedDao) {
        dao = openedDao;
    }
    
    @Override
    public LongCursor getUsers() {
        return dao.getUsers();
    }

    @Override
    public int getUserCount() {
        return dao.getUserCount();
    }

    @Override
    public LongCursor getItems() {
        return dao.getItems();
    }

    @Override
    public int getItemCount() {
        return dao.getItemCount();
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public SharedDAO open() {
        return this;
    }

    @Override
    public Cursor<Rating> getRatings() {
        return dao.getRatings();
    }

    @Override
    public Cursor<Rating> getRatings(SortOrder order) {
        return dao.getRatings(order);
    }

    @Override
    public Cursor<UserRatingProfile> getUserRatingProfiles() {
        return dao.getUserRatingProfiles();
    }

    @Override
    public Cursor<Rating> getUserRatings(long userId) {
        return dao.getUserRatings(userId);
    }

    @Override
    public Cursor<Rating> getUserRatings(long userId, SortOrder order) {
        return dao.getUserRatings(userId, order);
    }

    @Override
    public Cursor<Rating> getItemRatings(long itemId) {
        return dao.getItemRatings(itemId);
    }

    @Override
    public Cursor<Rating> getItemRatings(long itemId, SortOrder order) {
        return dao.getItemRatings(itemId, order);
    }
}