package org.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Feature {
    public double value;
    public int index;

    public Feature() {
        value = 0;
        index = 0;
    }

    public Feature(int ind, double val) {
        value = val;
        index = ind;
    }
}
