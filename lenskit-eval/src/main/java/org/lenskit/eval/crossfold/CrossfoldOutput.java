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
import com.google.common.io.Closer;
import org.lenskit.data.output.RatingWriter;

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
