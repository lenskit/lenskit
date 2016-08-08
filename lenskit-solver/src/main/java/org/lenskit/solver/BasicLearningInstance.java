package org.lenskit.solver;

import org.lenskit.featurizer.Feature;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BasicLearningInstance implements LearningInstance {
    private double weight;
    private double label;
    private final List<Feature> features = new ArrayList<>();

    @Inject
    public BasicLearningInstance() {}
}
