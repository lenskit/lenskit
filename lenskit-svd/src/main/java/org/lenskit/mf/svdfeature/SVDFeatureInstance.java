package org.lenskit.mf.svdfeature;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstance extends LearningInstance {
    private double label;
    private ArrayList<Feature> gfeas;
    private ArrayList<Feature> ufeas;
    private ArrayList<Feature> ifeas;

    public SVDFeatureInstance() {
        gfeas = new ArrayList<Feature>();
        ufeas = new ArrayList<Feature>();
        ifeas = new ArrayList<Feature>();
    }

    public void addGlobalFea(Feature fea) {
        gfeas.add(fea);
    }

    public void addUserFea(Feature fea) {
        ufeas.add(fea);
    }

    public void addItemFea(Feature fea) {
        ifeas.add(fea);
    }

    public ArrayList<Feature> getGlobalFeas() {
        return gfeas;
    }

    public ArrayList<Feature> getUserFeas() {
        return ufeas;
    }

    public ArrayList<Feature> getItemFeas() {
        return ifeas;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public double getLabel() {
        return label;
    }
}
