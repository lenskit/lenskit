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
package org.grouplens.lenskit;

import java.util.Collection;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.dao.RatingDataSession;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

public abstract class AbstractRatingPredictor implements RatingPredictor {

    private RatingDataAccessObject dao;

    public AbstractRatingPredictor(RatingDataAccessObject dao) {
        this.dao = dao;
    }

    protected RatingDataAccessObject getDAO() {
        return dao;
    }
    
    protected Collection<Rating> getUserRatings(long user) {
        RatingDataSession session = getDAO().getSession();
        try {
            return Cursors.makeList(session.getUserRatings(user));
        } finally {
            session.release();
        }
    }

    /**
     * Delegate to {@link #predict(long, java.util.Collection)}.
     */
    public ScoredId predict(long user, long item) {
        LongSet items = new LongSortedArraySet(new long[]{item});
        SparseVector p = predict(user, items);
        double pred = p.get(item);
        if (Double.isNaN(pred))
            return null;
        else
            return new ScoredId(item, pred);
    }

}