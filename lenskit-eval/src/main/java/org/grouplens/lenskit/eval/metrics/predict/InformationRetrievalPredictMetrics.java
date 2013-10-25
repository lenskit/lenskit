/**
 * 
 */
package org.grouplens.lenskit.eval.metrics.predict;

import java.util.List;

import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;


/**
 * This class calculates the IR metrics: precision, recall and F1. 
 * To use this IR based metrics we must use <code>TestUser.setGuessCandidates(true)</code>
 * in order no to use the set of test items in the recommendation/ranking.
 * 
 * TODO: Additional metrics to be included. See http://en.wikipedia.org/wiki/Information_retrieval
 * TODO: Allow configuration of the combined use of one or more of these metrics. 
 * TODO: calculate standard deviation, max, min of all metrics
 * TODO: review NDCGPredictMetric
 * 
 * @author hugof
 * 
 * @see org.grouplens.lenskit.eval.traintest.SwitchedSuppliers
 * @see org.grouplens.lenskit.eval.traintest.PredictionSupplier
 * @see org.grouplens.lenskit.eval.traintest.RecommendationSupplier
 * @see org.grouplens.lenskit.eval.traintest.TestUser
 * @see org.grouplens.lenskit.eval.traintest.TestTrainTestTask
 * @see org.grouplens.lenskit.eval.traintest.SimpleEvaluator
 * @see org.grouplens.lenskit.eval.traintest.TrainTestEvalJob
 */
public class InformationRetrievalPredictMetrics extends AbstractTestUserMetric {
  private static final                Logger logger = LoggerFactory.getLogger(InformationRetrievalPredictMetrics.class);
  private static final ImmutableList<String> COLUMNS= ImmutableList.of("NUsers", "NumRec", "NumPred", "Correct",  "Precision", "Recall", "F1");
  private static final ImmutableList<String> USER_COLUMNS = ImmutableList.of("NumRec", "NumPred", "Correct", "Precision", "Recall", "F1");


  @Override
  public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm,
      TTDataSet dataSet) {
    return new Accum();
  }

  @Override
  public List<String> getColumnLabels() {
    return COLUMNS;
  }

  @Override
  public List<String> getUserColumnLabels() {
    return USER_COLUMNS;
  }
  
  class Accum implements TestUserMetricAccumulator {
    private int npreds = 0;
    private int ngood = 0;
    private int nitems = 0;
    private int nusers = 0;

    /**
     * We assume that the number of ratings were set for example by the 
     * CrossFold class. For example we set aside a fixed number of items
     * per user. These are the hidden items. 
     * 
     * We will request a given number of recommendations. This value is set by 
     * TrainTestEvalTask.setNumRecs(numRecs). Default was set at 5. This values
     * is passed on to the TrainTestEvalJob via its constructor. In this class
     * the objects TestUser, PredictionSupplier and RecommendationSupplier are 
     * created with a request for numRecs. We can get this value from 
     * TestUser.getNumRecs()
     * 
     * 
     * Precision = number of matches / number of ratings requested 
     *           = number of matches / TestUser.getNumRecs()
     * Recall    = number of matches / number of ratings or recommendations made
     *           = number of matches / ratings.size()
     *           = mumber of matches / predictions.size()
     * 
     * Important Note 1: Information retrieval (IR) based metrics such as precision, 
     * recall and F1 assume that k ratings were requested and p recommendations were
     * made. The basic RMSE/MAE tests provide a set of test (hidden) items and request
     * for the rank value to be calculated. The difference between the predicted ranking
     * and the hidden items ranking generate the results (not the way I would do it, but
     * thats what's done in LensKit). In order to force recommendations/ratings to 
     * choose from all possible items, we must use TestUser.setGuessCandidates(true).
     * 
     * Important Note 2: the ratings use a baseline rater that is used to provide a 
     * rating for items that were not rated by the recommendation phase. This means that
     * in the case of perfect data were all items are ranked seen (1) or not seen (0),
     * we will end up by including all items in ranking list, possibly with the same
     * rating value. 
     */
    @Override
    public Object[] evaluate(TestUser user) {
      // Make sure recommendations uses all existing items as candidates
      user.setGuessCandidates(true);
      //List<ScoredId> scores = user.getRecommendations();
      SparseVector ratings = user.getTestRatings();
      // Predictions = recommendations + baseLine + domain clamping
      SparseVector predictions = user.getPredictions();
      //logger.info("#ratings = "+ratings.size());
      //logger.info("#predictions = "+predictions.size());
      //logger.info("#scores = "+scores.size());
      int good = 0; 
      for (VectorEntry e : ratings.fast()) {
          if (predictions.containsKey(e.getKey())){
            double pv = predictions.get(e.getKey());
            if (!Double.isNaN(pv)) {
                good += 1;
            }
          }
      }
      npreds += user.getNumRecs();              // number of requested recommendations
      ngood += good;                            // number of correct recommendations
      nitems += ratings.size();                 // number of train/hidden items
      nusers++;                                 // number of users tested
      double precision = (good * 1.0) / user.getNumRecs();
      double recall = (good * 1.0) / ratings.size();
      double f1 = (2.0 * precision * recall) / (precision + recall);
      return new Object[]{ user.getNumRecs(), ratings.size(), good, precision, recall, f1 };
    }
    
    @Override
    public Object[] finalResults() {
      double precision = (ngood * 1.0) / npreds;
      double recall = (ngood * 1.0) / nitems;
      double f1 = (2.0 * precision * recall) / (precision + recall);
      logger.info("Good: {}, Requested: {}, Predicted: {}, Precision: {}, Recall {}, F1 {}", 
                   ngood, npreds, nitems, precision, recall, f1);

      return new Object[]{nusers, npreds, nitems, ngood, precision, recall, f1};
    }
  }

}
