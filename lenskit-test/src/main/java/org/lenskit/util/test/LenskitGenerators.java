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
