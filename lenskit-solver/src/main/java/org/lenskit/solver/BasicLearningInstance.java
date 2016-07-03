package org.lenskit.solver;

import org.lenskit.featurizer.Feature;

import java.util.ArrayList;
import java.util.List;

public class BasicLearningInstance implements LearningInstance {
    private double weight;
    private double label;
    private final List<Feature> features = new ArrayList<>();

    public BasicLearningInstance() {}
}
