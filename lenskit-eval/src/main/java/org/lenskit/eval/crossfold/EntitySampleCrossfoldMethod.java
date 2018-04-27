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
package org.lenskit.eval.crossfold;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.output.RatingWriter;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Partition ratings into outputs.
 */
class EntitySampleCrossfoldMethod implements CrossfoldMethod {
    private static final Logger logger = LoggerFactory.getLogger(EntitySampleCrossfoldMethod.class);
    private final int sampleSize;

    EntitySampleCrossfoldMethod(int size) {
        sampleSize = size;
    }

    @Override
    public void crossfold(DataAccessObject input, CrossfoldOutput output, EntityType type) throws IOException {
        final int count = output.getCount();
        logger.info("splitting {} data from {} to {} partitions by ratings", type, input, count);

        // make a list ourselves so we can shuffle it, makeList lists are immutable
        List<Rating> ratings;
        try (ObjectStream<Rating> stream = input.query(type).asType(Rating.class).stream()) {
            ratings = Lists.newArrayList(stream);
        }
        Collections.shuffle(ratings);

        final int n = ratings.size();
        for (int i = 0; i < count; i++) {
            int start = i * sampleSize;
            int stop = (i + 1) * sampleSize;
            RatingWriter trainWriter = output.getTrainWriter(i);
            RatingWriter testWriter = output.getTestWriter(i);
            for (int j = 0; j < n; j++) {
                Rating r = ratings.get(j);
                if (j < start) {
                    trainWriter.writeRating(r);
                } else if (j < stop) {
                    testWriter.writeRating(r);
                } else {
                    trainWriter.writeRating(r);
                }
            }
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof EntitySampleCrossfoldMethod) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
