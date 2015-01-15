package org.grouplens.lenskit.predict;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.basic.AbstractRatingPredictor;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Tajinder on 1/6/2015.
 * 'KnownRatingRatingPredictor' predicts the known ratings of each item and the user given in the predictions
 */
public class KnownRatingRatingPredictor extends AbstractRatingPredictor {
    private final UserEventDAO userEvent;

    @Inject
    public KnownRatingRatingPredictor(UserEventDAO user) {
        this.userEvent = user;
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector predictions) {
        LongSortedSet tempPredictions = predictions.keyDomain();
        predictions.clear();

        List<Rating> ratings = userEvent.getEventsForUser(user, Rating.class);
        if (ratings !=null) {
            for (Rating r : ratings) {
                if (tempPredictions.contains(r.getItemId())) {
                    if (r.hasValue()) {
                        predictions.set(r.getItemId(), r.getValue());
                    } else {
                        predictions.unset(r.getItemId());
                    }
                }
            }
        }
    }
}
