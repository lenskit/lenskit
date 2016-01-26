package org.grouplens.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Feature {
    private double value;
    private int index;

    public Feature() {
        value = 0;
        index = 0;
    }

    public Feature(int ind, double val) {
        value = val;
        index = ind;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }

    public void setIndex(int ind) {
        index = ind;
    }

    public void setValue(double val) {
        value = val;
    }
}
