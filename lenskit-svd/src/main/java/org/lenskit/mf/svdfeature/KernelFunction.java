package org.lenskit.mf.svdfeature;

public interface KernelFunction {
    public double getValue(double[] left, double[] right);
    public double[] getGradient(double[] left, double[] right, boolean side);
}
