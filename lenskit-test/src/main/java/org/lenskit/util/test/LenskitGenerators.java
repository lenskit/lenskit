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
package org.lenskit.util.test;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;

/**
 * Generator classes for LensKit objects.
 */
public class LenskitGenerators {
    public static Generator<Rating> ratings() {
        return new RatingGenerator();
    }

    public static Generator<Rating> ratingsWithoutTimestamps() {
        RatingGenerator rg = new RatingGenerator();
        rg.timestamps = null;
        return rg;
    }

    private static class RatingGenerator implements Generator<Rating> {
        private final LongSet usedIds = new LongOpenHashSet();
        private final Generator<Long> ids = PrimitiveGenerators.positiveLongs();
        private final Generator<Long> userIds = PrimitiveGenerators.positiveLongs(1024);
        private final Generator<Long> itemIds = PrimitiveGenerators.positiveLongs(1024);
        private final Generator<Double> values = PrimitiveGenerators.doubles(1, 5);
        private Generator<Long> timestamps = PrimitiveGenerators.positiveLongs();

        @Override
        public Rating next() {
            long id = ids.next();
            while (usedIds.contains(id)) {
                id = ids.next();
            }
            usedIds.add(id);

            RatingBuilder rb = Rating.newBuilder()
                                     .setId(id)
                                     .setUserId(userIds.next())
                                     .setItemId(itemIds.next())
                                     .setRating(values.next());
            if (timestamps != null) {
                rb.setTimestamp(timestamps.next());
            }

            return rb.build();
        }
    }
}
