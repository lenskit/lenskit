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

import org.grouplens.lenskit.collections.FastIterable;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.ItemEventCollection;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BinaryItemCollection extends AbstractCollection<Rating> implements ItemEventCollection<Rating>, FastIterable<Rating> {
    private final long itemId;
    private final BinaryRatingList ratings;

    BinaryItemCollection(long item, BinaryRatingList rs) {
        itemId = item;
        ratings = rs;
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    @Override
    public Iterator<Rating> fastIterator() {
        return ratings.fastIterator();
    }

    @Override
    public Iterator<Rating> iterator() {
        return ratings.iterator();
    }

    @Override
    public int size() {
        return ratings.size();
    }
}
