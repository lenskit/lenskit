package org.lenskit.mf.hmmsvd;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lenskit.mf.svdfeature.Feature;
import org.lenskit.solver.objective.LearningInstance;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureInstance extends LearningInstance {
    public ArrayList<Feature> gfeas;
    public ArrayList<Feature> ufeas;
    public ArrayList<ArrayList<Feature> > pos2ifeas;
    public IntArrayList obs;
    public int numPos;
    public int numObs;

    public HmmSVDFeatureInstance() {
        gfeas = new ArrayList<Feature>();
        ufeas = new ArrayList<Feature>();
        pos2ifeas = new ArrayList<ArrayList<Feature> >();
        obs = new IntArrayList();
        numPos = 0;
        numObs = 0;
    }
}
