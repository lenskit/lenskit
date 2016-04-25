package org.lenskit.featurize;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Feature {
    private final double value;
    private final int index;

    public Feature() {
        value = 0;
        index = 0;
    }

    public Feature(int index, double value) {
        this.value = value;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}
