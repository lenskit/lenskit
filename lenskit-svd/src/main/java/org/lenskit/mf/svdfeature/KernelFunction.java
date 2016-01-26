package org.grouplens.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface KernelFunction {
    public double getValue(double[] left, double[] right);
    public double[] getGradient(double[] left, double[] right, boolean side);
}
