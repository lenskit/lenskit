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
package org.lenskit.bias;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model with the global average rating.
 */
public class GlobalAverageRatingBiasModelProvider implements Provider<GlobalBiasModel> {
    private final DataAccessObject dao;

    @Inject
    public GlobalAverageRatingBiasModelProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    @Override
    public GlobalBiasModel get() {
        double sum = 0;
        int n = 0;
        try (ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
            for (Rating r: ratings) {
                sum += r.getValue();
                n += 1;
            }
        }
        double mean = n > 0 ? sum / n : 0;
        return new GlobalBiasModel(mean);
    }
}
