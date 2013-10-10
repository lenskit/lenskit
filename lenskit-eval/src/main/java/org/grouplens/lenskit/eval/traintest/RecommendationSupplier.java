/**
 * 
 */
package org.grouplens.lenskit.eval.traintest;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import org.grouplens.lenskit.eval.algorithm.RecommenderInstance;
import org.grouplens.lenskit.scored.ScoredId;

/**
 * @author hugof
 *
 */
public class RecommendationSupplier implements SwitchedSupplier<List<ScoredId>> {
    private final RecommenderInstance recommender;
    private final long user;
    private final LongSet items;
    private final int numRecs;
    private boolean guessCandidates;

    public RecommendationSupplier(RecommenderInstance rec, int numRecs, long id, LongSet is) {
        this.numRecs = numRecs;
        recommender = rec;
        user = id;
        items = is;
    }

    /**
     * @see com.google.common.base.Supplier#get()
     * @see org.grouplens.lenskit.eval.traintest.SwitchedSupplier#get()
     */
    @Override
    public List<ScoredId> get() {
        if (recommender == null) {
            throw new IllegalArgumentException("cannot compute recommendations without a recommender");
        }
        LongSet items_ = guessCandidates ? null : items;
        List<ScoredId> recs = recommender.getRecommendations(user, items_, numRecs);
        if (recs == null) {
            throw new IllegalArgumentException("no recommendations");
        }
        return recs;
    }

    @Override
    public void setGuessCandidates(boolean t) {
      guessCandidates = t;
    }

    @Override
    public boolean guessCandidates() {
      return guessCandidates;
    }
}
