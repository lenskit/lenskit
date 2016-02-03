package org.lenskit.solver.objective;

import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LearningModel {
    protected HashMap<String, RealVector> scalarVars;
    protected HashMap<String, RealMatrix> vectorVars;

    protected void assignVariables();
    protected LearningOracle getStochasticOracle() throws IOException;
    protected void startNewIteration() throws IOException;
}
