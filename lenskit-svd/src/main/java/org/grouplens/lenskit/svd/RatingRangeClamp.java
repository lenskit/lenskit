/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.util.DoubleFunction;

import java.io.Serializable;

public final class RatingRangeClamp implements DoubleFunction, Serializable {

    private static final long serialVersionUID = 1012447846494918355L;
    private PreferenceDomain domain;

    public RatingRangeClamp(PreferenceDomain dom) {
        domain = dom;
    }

    public RatingRangeClamp(double min, double max) {
        this(new PreferenceDomain(min, max));
    }

    /**
     * @return the minRating
     */
    public double getMinRating() {
        return domain.getMinimum();
    }

    /**
     * @return the maxRating
     */
    public double getMaxRating() {
        return domain.getMaximum();
    }

    @Override
    public double apply(double v) {
        return domain.clampValue(v);
    }
}
