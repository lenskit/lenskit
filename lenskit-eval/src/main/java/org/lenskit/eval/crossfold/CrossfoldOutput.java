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
