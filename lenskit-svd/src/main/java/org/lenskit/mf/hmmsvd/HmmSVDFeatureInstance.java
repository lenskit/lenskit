package org.lenskit.mf.hmmsvd;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lenskit.mf.svdfeature.Feature;
import org.lenskit.solver.objective.LearningInstance;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureInstance extends LearningInstance {
    public ArrayList<ArrayList<Feature>> pos2gfeas;
    public ArrayList<Feature> ufeas;
    public ArrayList<ArrayList<Feature>> pos2ifeas;
    public IntArrayList obs;
    public int numPos;
    public int numObs;

    public HmmSVDFeatureInstance() {
        pos2gfeas = new ArrayList<>();
        ufeas = new ArrayList<>();
        pos2ifeas = new ArrayList<>();
        obs = new IntArrayList();
        numPos = 0;
        numObs = 0;
    }

    public void setNumPos(int inNumPos) {
        numPos = inNumPos;
        for (int i=0; i<numPos; i++) {
            pos2gfeas.add(new ArrayList<Feature>());
            pos2ifeas.add(new ArrayList<Feature>());
        }
    }

    public void addGlobalFeas(Feature fea) {
        for (int i=0; i<numPos; i++) {
            pos2gfeas.get(i).add(fea);
        }
    }
}
