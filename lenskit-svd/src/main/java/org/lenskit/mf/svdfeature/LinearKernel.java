package org.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LinearKernel implements KernelFunction {
    public LinearKernel() {}

    public double getValue(double[] left, double[] right) {
        return ArrayHelper.innerProduct(left, right);
    }

    public double[] getGradient(double[] left, double[] right, boolean side) {
        double[] grad = new double[left.length];
        if (side == true) {
            ArrayHelper.copy(grad, right);
        } else {
            ArrayHelper.copy(grad, left);
        }
        return grad;
    }
}
