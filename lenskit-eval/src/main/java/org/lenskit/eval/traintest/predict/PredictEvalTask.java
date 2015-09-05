package org.lenskit.eval.traintest.predict;

import org.grouplens.lenskit.data.history.UserHistory;
import org.lenskit.api.Recommender;
import org.lenskit.data.events.Event;
import org.lenskit.eval.traintest.EvalTask;
import org.lenskit.eval.traintest.Measurement;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * An eval task that attempts to predict the user's test ratings.
 */
public class PredictEvalTask implements EvalTask {
    @Override
    public Set<String> getGlobalColumns() {
        // TODO Implement this method
        return null;
    }

    @Override
    public Set<String> getUserColumns() {
        // TODO Implement this method
        return null;
    }

    @Override
    public Measurement startMeasurement(Recommender rec) {
        // TODO Implement this method
        return null;
    }

    class PredictMeasurement implements Measurement {
        @Nonnull
        @Override
        public Map<String, Object> measureUser(UserHistory<Event> testUser) {
            // TODO Implement this method
            return null;
        }

        @Nonnull
        @Override
        public Map<String, Object> finish() {
            // TODO Implement this method
            return null;
        }
    }
}
