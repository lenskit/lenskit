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
package org.lenskit.eval.crossfold;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.grouplens.lenskit.eval.data.RatingWriter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Manager for outputs for a crossfold operation.
 */
class CrossfoldOutput implements Closeable {
    private final Random random;
    private final Closer closer;
    private final int count;
    private final List<RatingWriter> trainWriters, testWriters;

    public CrossfoldOutput(Crossfolder cf, Random rng) throws IOException {
        random = rng;
        closer = Closer.create();
        count = cf.getPartitionCount();
        trainWriters = Lists.newArrayListWithCapacity(count);
        testWriters = Lists.newArrayListWithCapacity(count);
        try {
            for (Path path: cf.getTrainingFiles()) {
                trainWriters.add(closer.register(cf.openWriter(path)));
            }
            for (Path path: cf.getTestFiles()) {
                testWriters.add(closer.register(cf.openWriter(path)));
            }
        } catch (Exception ex) {
            // this funny logic is needed to make the closer add any close exceptions as suppressed exceptions
            // to the exception we're failing with
            try {
                throw closer.rethrow(ex);
            } finally {
                closer.close();
            }
        }
        // the constructor has succeeded - closing will be handled by the close method
    }

    /**
     * Get the RNG for this output.
     * @return The output's RNG.
     */
    public Random getRandom() {
        return random;
    }

    public int getCount() {
        return count;
    }

    public RatingWriter getTrainWriter(int i) {
        return trainWriters.get(i);
    }

    public RatingWriter getTestWriter(int i) {
        return testWriters.get(i);
    }

    @Override
    public void close() throws IOException {
        closer.close();
    }
}
