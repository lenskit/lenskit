/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
