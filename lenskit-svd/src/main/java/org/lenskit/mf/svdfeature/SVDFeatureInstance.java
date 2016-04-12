package org.lenskit.mf.svdfeature;

import org.apache.commons.lang3.StringUtils;
import org.lenskit.solver.LearningInstance;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstance implements LearningInstance {
    public double weight;
    public double label;
    public ArrayList<Feature> gfeas;
    public ArrayList<Feature> ufeas;
    public ArrayList<Feature> ifeas;

    public SVDFeatureInstance() {
        gfeas = new ArrayList<Feature>();
        ufeas = new ArrayList<Feature>();
        ifeas = new ArrayList<Feature>();
        weight = 1.0;
        label = 0.0;
    }

    public SVDFeatureInstance(ArrayList<Feature> gfeas, ArrayList<Feature> ufeas, 
                              ArrayList<Feature> ifeas) {
        this.gfeas = gfeas;
        this.ufeas = ufeas;
        this.ifeas = ifeas;
        label = 0.0;
        weight = 1.0;
    }

    public String toString() {
        ArrayList<String> fields = new ArrayList<>(5 + (gfeas.size() + ufeas.size() + ifeas.size()) * 2);
        fields.add(Double.toString(weight));
        fields.add(Double.toString(label));
        fields.add(Integer.toString(gfeas.size()));
        fields.add(Integer.toString(ufeas.size()));
        fields.add(Integer.toString(ifeas.size()));
        for (Feature fea : gfeas) {
            fields.add(Integer.toString(fea.index));
            fields.add(Double.toString(fea.value));
        }
        for (Feature fea : ufeas) {
            fields.add(Integer.toString(fea.index));
            fields.add(Double.toString(fea.value));
        }
        for (Feature fea : ifeas) {
            fields.add(Integer.toString(fea.index));
            fields.add(Double.toString(fea.value));
        }
        return StringUtils.join(fields, " ");
    }
}
