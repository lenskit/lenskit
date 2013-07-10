package org.grouplens.lenskit.data.history;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;

import java.util.List;

/**
 * Utility methods for user histories.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class History {
    private History() {}

    /**
     * Create a history for a particular user.
     * @param id The user ID.
     * @param events The events.
     * @param <E> The root type of the events in the history.
     * @return A history object.
     */
    @SuppressWarnings("deprecation")
    public static <E extends Event> UserHistory<E> forUser(long id, List<? extends E> events) {
        return new BasicUserHistory<E>(id, events);
    }
}
