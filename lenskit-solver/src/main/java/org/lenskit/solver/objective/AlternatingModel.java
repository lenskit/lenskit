package org.grouplens.lenskit.solver.objective;

import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface AlternatingModel implements LearningModel {
    public int getNumOfAlternation();
    public LearningOracle getNextAlternatingOracle(int alter) throws IOException;
}
