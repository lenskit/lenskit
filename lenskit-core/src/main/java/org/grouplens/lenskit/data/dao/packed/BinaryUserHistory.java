package org.grouplens.lenskit.data.dao.packed;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.AbstractUserHistory;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryUserHistory extends AbstractUserHistory<Rating> {
    private final long userId;
    private final BinaryRatingList ratings;

    BinaryUserHistory(long user, BinaryRatingList rs) {
        userId = user;
        ratings = rs;
    }

    @Override
    public Rating get(int index) {
        return ratings.get(index);
    }

    @Override
    public int size() {
        return ratings.size();
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public <T extends Event> UserHistory<T> filter(Class<T> type) {
        if (type.isAssignableFrom(Rating.class)) {
            return (UserHistory<T>) this;
        } else {
            return History.forUser(userId);
        }
    }

    @Override
    public UserHistory<Rating> filter(Predicate<? super Rating> pred) {
        return History.forUser(userId, FluentIterable.from(ratings).filter(pred).toList());
    }
}
