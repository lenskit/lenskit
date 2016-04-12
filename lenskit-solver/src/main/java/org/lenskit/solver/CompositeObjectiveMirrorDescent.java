package org.lenskit.solver;

public class CompositeObjectiveMirrorDescent implements OnlineOptimizationMethod {

    private final BregmanDivergence bregmanDivergence;

    public CompositeObjectiveMirrorDescent(BregmanDivergence bregmanDivergence) {
        this.bregmanDivergence = bregmanDivergence;
    }

    public double minimize(LearningModel learningModel, LearningData learningData) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objVal = 0;
        while (termCrit.keepIterate()) {
            learningData.startNewIteration();
            objVal = update(learningModel, LearningData);
            termCrit.addIteration("CompositeObjectiveMirrorDescent", objVal);
        }
        return objVal;
    }

    public double update(LearningModel learningModel, LearningData learningData) {
        ObjectiveFunction objFunc = learningModel.getObjectiveFunction();
        LearningInstance ins;
        while ((ins = learningData.getLearningInstance()) != null) {
            StochasticOracle orc = model.getStochasticOracle(ins);
            objFunc.wrapOracle(orc);
        }
    }
}
