package org.lenskit.eval.traintest;


import org.grouplens.lenskit.data.history.UserHistory;
import org.lenskit.data.events.Event;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Measures the performance of a single experimental condition.
 */
public interface ConditionEvaluator {
    /**
     * Measure the performance for a single user.
     * @param testUser The user to test.
     * @return The per-user performance measurements.
     */
    @Nonnull
    Map<String,Object> measureUser(UserHistory<Event> testUser);

    /**
     * Finish measuring the performance for the algorithm and data set.
     * @return The aggregate performance measurements.
     */
    @Nonnull
    Map<String,Object> finish();
}
