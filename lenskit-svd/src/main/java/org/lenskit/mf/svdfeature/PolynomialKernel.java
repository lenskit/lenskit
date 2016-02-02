package org.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PolynomialKernel implements KernelFunction {
    private double p;

    public PolynomialKernel(double outP) {
        p = outP; 
    }

    public double getValue(double[] left, double[] right) {
        double prod = ArrayHelper.innerProduct(left, right);
        return Math.pow(prod + 1, p);
    }

    public double[] getGradient(double[] left, double[] right, boolean side) {
        double[] grad = new double[left.length];
        if (side == true) {
            ArrayHelper.copy(grad, right);
        } else {
            ArrayHelper.copy(grad, left);
        }
        double prod = ArrayHelper.innerProduct(left, right);
        ArrayHelper.scale(grad, p * Math.pow(prod + 1, p - 1));
        return grad;
    }
}
