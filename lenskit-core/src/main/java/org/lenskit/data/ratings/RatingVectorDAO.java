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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;

import javax.annotation.Nonnull;

/**
 * Interface for user 'rating vectors', which are mappings of items to user preference.  This is used
 * to access use ratings, or to transform other types of data into something that looks like a rating vector.
 */
@DefaultImplementation(StandardRatingVectorDAO.class)
public interface RatingVectorDAO {
    /**
     * Get a user's rating vector.
     * @param user The rating vector summarizing a user's historical preferences.
     * @return The rating vector.  Will return an empty vector for nonexistent users.
     */
    @Nonnull
    Long2DoubleMap userRatingVector(long user);
}
