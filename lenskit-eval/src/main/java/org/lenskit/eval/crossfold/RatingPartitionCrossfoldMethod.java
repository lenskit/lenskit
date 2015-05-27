package org.lenskit.eval.crossfold;

import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.source.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Partition ratings into outputs.
 */
class RatingPartitionCrossfoldMethod implements CrossfoldMethod {
    private static final Logger logger = LoggerFactory.getLogger(RatingPartitionCrossfoldMethod.class);

    @Override
    public void crossfold(DataSource input, CrossfoldOutput output) throws IOException {
        final int count = output.getCount();
        logger.info("splitting data source {} to {} partitions by ratings",
                    input.getName(), count);
        ArrayList<Rating> ratings = Cursors.makeList(input.getEventDAO().streamEvents(Rating.class));
        Collections.shuffle(ratings);

        final int n = ratings.size();
        for (int i = 0; i < n; i++) {
            for (int f = 0; f < count; f++) {
                int foldNum = i % count;
                if (f == foldNum) {
                    output.getTestWriter(f).writeRating(ratings.get(i));
                } else {
                    output.getTrainWriter(f).writeRating(ratings.get(i));
                }
            }
        }
    }
}
