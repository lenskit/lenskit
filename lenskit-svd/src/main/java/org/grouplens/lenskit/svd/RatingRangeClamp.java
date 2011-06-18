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
package org.grouplens.lenskit.svd;

import java.io.Serializable;

import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MinRating;
import org.grouplens.lenskit.util.DoubleFunction;

public final class RatingRangeClamp implements DoubleFunction, Serializable {

    private static final long serialVersionUID = 1012447846494918355L;
    private final double minRating, maxRating;
    
    public RatingRangeClamp() {
        this(1, 5);
    }

    public RatingRangeClamp(@MinRating double min, @MaxRating double max) {
        minRating = min;
        maxRating = max;
    }

    /**
     * @return the minRating
     */
    public double getMinRating() {
        return minRating;
    }
    
    /**
     * @return the maxRating
     */
    public double getMaxRating() {
        return maxRating;
    }

        @Override
    public double apply(double v) {
        if (v < minRating) return minRating;
        else if (v > maxRating) return maxRating;
        else return v;
    }

}
