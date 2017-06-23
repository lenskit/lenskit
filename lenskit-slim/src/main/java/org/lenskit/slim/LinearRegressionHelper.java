/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.math.Scalars;
import java.util.Map;
import static java.lang.Math.abs;


/**
 * Several simple vector algebra used by SLIM learning process
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class LinearRegressionHelper {
    /**
     * soft-Thresholding for coordinate descent
     * @param z
     * @param gamma
     * @return
     */
    public static double softThresholding(double z, double gamma) {
        if (z > 0 && gamma < abs(z)) return z - gamma;
        if (z < 0 && gamma < abs(z)) return z + gamma; // comment this line to get non-negative weights during learning process
        return 0.0;
    }


}
