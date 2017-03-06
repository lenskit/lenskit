package org.lenskit.slim;

import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.inject.Shareable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.io.Serializable;


@Shareable
public final class SLIMUpdateParameters implements Serializable {
    private static final long serialVersionUID = 2L;
    private final double beta;
    private final double lambda;
    private final boolean intercept;
    private final StoppingCondition stoppingCondition;


    @Inject
    public SLIMUpdateParameters(@RidgeRegressionTerm double beta, @RegularizationTerm double lambda, @Intercept boolean intercept, StoppingCondition stop) {
        this.beta = beta;
        this.lambda = lambda;
        this.intercept = intercept;
        stoppingCondition = stop;
    }

    public double getBeta() { return beta; }

    public double getLambda() { return lambda; }

    public boolean getIntercept() { return intercept; }

    public StoppingCondition getStoppingCondition() { return stoppingCondition; }

    public TrainingLoopController getTrainingLoopController()  { return stoppingCondition.newLoop(); }
}
