/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nonnull;

/**
 * Proxy DAO for user 'rating vectors', which are mappings of items to user preference.  This is used
 * to access use ratings, or to transform other types of data into something that looks like a rating vector.
 *
 * This DAO interface is generally *proxy DAO*: that is, it provides a different view of data typically implemented
 * on top of the base {@link org.lenskit.data.dao.DataAccessObject} interface.  However, an application can specialize
 * it with some other kind of optimized implementation if desired; for example, if user rating vectors are stored in
 * a Redis cache.
 */
@DefaultImplementation(StandardRatingVectorPDAO.class)
public interface RatingVectorPDAO {
    /**
     * Get a user's rating vector.
     * @param user The rating vector summarizing a user's historical preferences.
     * @return The unnormmalized user rating vector.  Will return an empty vector for nonexistent users.
     */
    @Nonnull
    Long2DoubleMap userRatingVector(long user);

    /**
     * Stream all users in the data set.
     * @return A stream over the users in the data set.
     */
    ObjectStream<IdBox<Long2DoubleMap>> streamUsers();
}
