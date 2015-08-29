package org.lenskit.eval.traintest;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Measures the performance of a single algorithm over a single data set on a particular eval task.
 */
public interface Measurement {
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
