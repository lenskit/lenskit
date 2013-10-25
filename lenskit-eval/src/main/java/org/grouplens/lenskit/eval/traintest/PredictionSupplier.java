/**
 * 
 */
package org.grouplens.lenskit.eval.traintest;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.eval.algorithm.RecommenderInstance;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * @author hugof
 *
 */
public class PredictionSupplier implements SwitchedSupplier<SparseVector> {
    private final RecommenderInstance predictor;
    private final long user;
    private final LongSet items;
    private final int numRecs;
    private boolean guessCandidates;
    

    public PredictionSupplier(RecommenderInstance pred, int numRecs, long id, LongSet is) {
        this.numRecs = numRecs;
        predictor = pred;
        user = id;
        items = is;
    }

    /**
     * @see com.google.common.base.Supplier#get()
     * @see org.grouplens.lenskit.eval.traintest.SwitchedSupplier#get()
     */
    @Override
    public SparseVector get() {
        if (predictor == null) {
            throw new IllegalArgumentException("cannot compute predictions without a predictor");
        }
        Recommender rec = predictor.getRecommender();
        ItemRecommender irec = rec.getItemRecommender();
        LongSet candidates = items;
        LongSet exclude;
        if (guessCandidates) {
          candidates = irec.getPredictableItems(user);
          exclude = irec.getDefaultExcludes(user);
          if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(candidates, exclude);
          }
        }
        SparseVector preds = predictor.getPredictions(user, candidates);
        if (preds == null) {
            throw new IllegalArgumentException("no predictions");
        }
        // just get the interesting stuff just in case no baseline predictor is used
        //System.out.println(preds);
        LongArrayList l = preds.keysByValue(true);
        //System.out.println(l);
        int sz = Math.min(l.size(), numRecs);
        //System.out.println("sz = "+sz);
        //System.out.println(preds.keyDomain());
        MutableSparseVector preds_ =  MutableSparseVector.create(preds.keyDomain());
        for (int c = 0; c < sz; c++){
          //System.out.println(l.get(c)+":"+preds.get(l.get(c)));
          preds_.set(l.get(c),preds.get(l.get(c)));
        }
        //return preds;
        //System.out.println(preds_);
        return preds_;
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
