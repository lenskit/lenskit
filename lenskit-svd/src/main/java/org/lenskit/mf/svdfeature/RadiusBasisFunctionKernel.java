package org.grouplens.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RadiusBasisFunctionKernel implements KernelFunction {
    public RadiusBasisFunctionKernel() {}

    public double getValue(double[] left, double[] right) {
        double[] sub = new double[left.length];
        ArrayHelper.copy(sub, left);
        ArrayHelper.subtraction(sub, right);
        double normv = ArrayHelper.innerProduct(sub, sub);
        return Math.exp(-normv);
    }

    public double[] getGradient(double[] left, double[] right, boolean side) {
        double[] grad = new double[left.length];
        double[] sub = new double[left.length];
        ArrayHelper.copy(sub, left);
        ArrayHelper.subtraction(sub, right);
        double normv = ArrayHelper.innerProduct(sub, sub);
        double val = Math.exp(-normv);
        ArrayHelper.copy(grad, sub);
        if (side == true) {
            ArrayHelper.scale(grad, -2 * val);
        } else {
            ArrayHelper.scale(grad, 2 * val);
        }
        return grad;
    }
}
