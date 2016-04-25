package org.lenskit.mf.svdfeature;

import org.apache.commons.lang3.StringUtils;
import org.lenskit.featurizer.Feature;
import org.lenskit.solver.LearningInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstance implements LearningInstance {
    double weight;
    double label;
    List<Feature> gfeas;
    List<Feature> ufeas;
    List<Feature> ifeas;

    public SVDFeatureInstance() {
        gfeas = new ArrayList<>();
        ufeas = new ArrayList<>();
        ifeas = new ArrayList<>();
        weight = 1.0;
        label = 0.0;
    }

    public SVDFeatureInstance(List<Feature> gfeas, List<Feature> ufeas,
                              List<Feature> ifeas) {
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
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        for (Feature fea : ufeas) {
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        for (Feature fea : ifeas) {
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        return StringUtils.join(fields, " ");
    }
}
