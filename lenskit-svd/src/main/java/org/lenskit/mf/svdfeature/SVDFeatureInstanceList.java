package org.lenskit.mf.svdfeature;

import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;

import java.util.List;

public class SVDFeatureInstanceList implements LearningData {
    private int iter = 0;
    private final List<SVDFeatureInstance> insList;

    public SVDFeatureInstanceList(List<SVDFeatureInstance> insList) {
        this.insList = insList;
    }

    public LearningInstance getLearningInstance() {
        if (iter >= insList.size()) {
            return null;
        }
        return insList.get(iter++);
    }

    public void startNewIteration() {
        iter = 0;
    }
}
