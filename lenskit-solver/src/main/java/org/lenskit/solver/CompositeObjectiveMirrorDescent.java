package org.lenskit.solver;

public class CompositeObjectiveMirrorDescent extends AbstractOnlineOptimizationMethod {

    private final BregmanDivergence bregmanDivergence;

    public CompositeObjectiveMirrorDescent(BregmanDivergence bregmanDivergence,
                                           double tol,
                                           int maxIter) {
        super();
        this.bregmanDivergence = bregmanDivergence;
        this.tol = tol;
        this.maxIter = maxIter;
    }

    public double update(LearningModel learningModel, LearningData learningData) {
        ObjectiveFunction objFunc = learningModel.getObjectiveFunction();
        LearningInstance ins;
        double objVal = 0.0;
        while ((ins = learningData.getLearningInstance()) != null) {
            StochasticOracle orc = learningModel.getStochasticOracle(ins);
            objFunc.wrapOracle(orc);
            //main algorithm here
        }
        return objVal;
    }
}
