package org.grouplens.lenskit.eval.traintest;

import com.google.common.collect.Lists;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MockTestUser extends AbstractTestUser {
    private final UserHistory<Event> trainHistory;
    private final UserHistory<Event> testHistory;
    private final SparseVector predictions;
    private final List<ScoredId> recommendations;

    public MockTestUser(UserHistory<Event> train, UserHistory<Event> test,
                        SparseVector preds, List<ScoredId> recs) {
        trainHistory = train;
        testHistory = test;
        predictions = preds;
        recommendations = recs;
    }

    @Override
    public UserHistory<Event> getTrainHistory() {
        return trainHistory;
    }

    @Override
    public UserHistory<Event> getTestHistory() {
        return testHistory;
    }

    @Nullable
    @Override
    public SparseVector getPredictions() {
        return predictions;
    }

    @Nullable
    @Override
    public List<ScoredId> getRecommendations(int n, ItemSelector candSel, ItemSelector exclSel) {
        return recommendations;
    }

    @Nullable
    @Override
    public Recommender getRecommender() {
        return null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private long userId;
        private List<Event> trainEvents = Lists.newArrayList();
        private List<Event> testEvents = Lists.newArrayList();
        private SparseVector predictions;
        private List<ScoredId> recommendations;

        public Builder setUserId(long uid) {
            userId = uid;
            return this;
        }

        public Builder addTrainEvent(Event evt) {
            trainEvents.add(evt);
            return this;
        }

        public Builder addTrainRating(long item, double value) {
            return addTrainEvent(Ratings.make(userId, item, value));
        }

        public Builder addTestEvent(Event evt) {
            testEvents.add(evt);
            return this;
        }

        public Builder addTestRating(long item, double value) {
            return addTestEvent(Ratings.make(userId, item, value));
        }

        public SparseVector getPredictions() {
            return predictions;
        }

        public Builder setPredictions(SparseVector predictions) {
            this.predictions = predictions;
            return this;
        }

        public List<ScoredId> getRecommendations() {
            return recommendations;
        }

        public Builder setRecommendations(List<ScoredId> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public MockTestUser build() {
            return new MockTestUser(History.forUser(userId, trainEvents),
                                    History.forUser(userId, testEvents),
                                    predictions, recommendations);
        }
    }
}
