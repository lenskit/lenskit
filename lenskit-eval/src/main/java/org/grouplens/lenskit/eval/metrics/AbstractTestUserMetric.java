package org.grouplens.lenskit.eval.metrics;

import org.grouplens.lenskit.eval.traintest.TrainTestEvalTask;

/**
 * Abstract base implementation of {@link TestUserMetric}.
 * @author Michael Ekstrand
 * @since 0.10
 */
public abstract class AbstractTestUserMetric
        extends AbstractMetric<TrainTestEvalTask>
        implements TestUserMetric {
}
