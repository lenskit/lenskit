package org.grouplens.lenskit.solver.objective;

import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface LearningModel {
    public int getNumOfVariables();
    public int getNumOfAlternation();
    public LearningOracle getNextOracle() throws IOException;
    public LearningOracle getNextAlternatingOracle(int alter) throws IOException;
    public void startNewIteration() throws IOException;
    public double getVariable(int idx);
    public void setVariable(int idx, double var);
}
