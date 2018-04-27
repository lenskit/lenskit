/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.transform.quantize;

import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;

/**
 * Quantize real values into discrete values. Used to do things like map floating point
 * ratings or predictions to discrete rating values.  By default, if there is a preference
 * domain available, a {@link PreferenceDomainQuantizer} will be used to implement this interface.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(PreferenceDomainQuantizer.AutoProvider.class)
public interface Quantizer {
    /**
     * Get the possible values into which this quantizer will map input values. These
     * are the values corresponding to each “bin” into which the quantizer will put
     * values.
     *
     * @return The values of the discrete bins.
     */
    RealVector getValues();

    /**
     * Get the value corresponding to a quantized value, based on the index into
     * the list of possible values.
     *
     * @param i The quantized value number, in the range [0,n) where n is the number of
     *          possible discrete values (see {@link #getCount()}).
     * @return The value corresponding to quantum {@code i}.
     * @throws IllegalArgumentException if {@code i} is an invalid discrete value.
     */
    double getIndexValue(int i);

    /**
     * Get the number of discrete values the output can take.
     *
     * @return The number of possible discrete values.
     */
    int getCount();

    /**
     * Convert a value into a discrete, quantized value.
     *
     * @param val A value to quantize.
     * @return The index of the discrete value to which {@code val} is mapped.
     */
    int index(double val);

    /**
     * Convert a value into a quantized value, returning the quantized value.
     * @param val A value to quantize.
     * @return The quantized value.
     */
    double quantize(double val);
}
