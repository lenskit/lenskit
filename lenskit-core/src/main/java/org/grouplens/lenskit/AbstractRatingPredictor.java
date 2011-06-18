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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.Collection;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Base class to make rating predictors easier to implement.  Delegates 
 * {@link #predict(long, long)} to {@link #predict(long, Collection)}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRatingPredictor implements RatingPredictor {

    /**
     * Delegate to {@link #predict(long, Collection)}.
     */
    @Override
    public double predict(long user, long item) {
        LongList l = new LongArrayList(1);
        l.add(item);
        SparseVector v = predict(user, l);
        return v.get(item);
    }
}