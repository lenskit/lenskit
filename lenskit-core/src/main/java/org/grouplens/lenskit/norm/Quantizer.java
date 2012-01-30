package org.grouplens.lenskit.norm;

/**
 * Quantize real values into discrete values. Used to do things like map floating point
 * ratings or predictions to discrete rating values.
 *
 * @review Is this the right package for this interface?
 *
 * @author Michael Ekstrand
 */
public interface Quantizer {
    /**
     * Get the possible values into which this quantizer will map input values. These
     * are the values corresponding to each “bin” into which the quantizer will put
     * values.
     * @return The values of the discrete bins.
     */
    double[] getValues();

    /**
     * Get the value corresponding to a quantized value.
     * @param i The quantized value number, in the range [0,n) where n is the number of
     *          possible discrete values (see {@link #getCount()}).
     * @return The value corresponding to quantum {@code i}.
     */
    double getValue(int i);

    /**
     * Get the number of discrete values the output can take.
     * @return The number of possible discrete values.
     */
    int getCount();

    /**
     * Convert a value into a discrete, quantized value.
     * @param val A value to quantize.
     * @return The index of the discrete value to which {@code val} is mapped.
     */
    int apply(double val);
}
