package org.lenskit.featurizer;

import org.lenskit.solver.LearningInstance;

import java.util.ArrayList;
import java.util.List;

public class BasicLearningInstance implements LearningInstance {
    private double weight;
    private double label;
    private final List<Feature> features = new ArrayList<>();

    public BasicLearningInstance() {}
}
