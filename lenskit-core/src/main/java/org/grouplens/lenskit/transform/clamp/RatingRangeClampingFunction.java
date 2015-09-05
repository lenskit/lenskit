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
package org.grouplens.lenskit.transform.clamp;

import org.grouplens.lenskit.core.Shareable;
import org.lenskit.data.ratings.PreferenceDomain;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Clamp values to the range of valid ratings. This clamping function uses
 * the {@link PreferenceDomain} to clamp values to fall within the minimum
 * and maximum allowable ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@Shareable
public class RatingRangeClampingFunction implements ClampingFunction, Serializable {
    private static final long serialVersionUID = 1L;

    private final PreferenceDomain domain;

    @Inject
    public RatingRangeClampingFunction(PreferenceDomain dom) {
        domain = dom;
    }

    public double apply(long user, long item, double value) {
        return domain.clampValue(value);
    }
}
