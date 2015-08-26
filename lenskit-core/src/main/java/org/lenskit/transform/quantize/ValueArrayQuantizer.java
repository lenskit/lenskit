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

import com.google.common.base.Preconditions;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.core.Shareable;

import java.io.Serializable;

/**
 * Abstract quantizer implementation using a pre-generated array of possible
 * values. Values are quantized to their closest discrete possibility.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class ValueArrayQuantizer implements Quantizer, Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The values to quantize to.  Subclasses must not modify this array after the object
     * has been constructed.
     */
    protected final ArrayRealVector values;

    /**
     * Construct a new quantizer using the specified array of values.
     *
     * @param vs The discrete values to quantize to.
     */
    public ValueArrayQuantizer(double[] vs) {
        Preconditions.checkArgument(vs.length > 0, "must have at least one value");
        values = new ArrayRealVector(vs);
    }

    public ValueArrayQuantizer(RealVector vs) {
        Preconditions.checkArgument(vs.getDimension() > 0, "must have at least one value");
        values = new ArrayRealVector(vs);
    }

    @Override
    public RealVector getValues() {
        // TODO Make this return an immutable view
        return values;
    }

    @Override
    public int getCount() {
        return values.getDimension();
    }

    @Override
    public double getIndexValue(int i) {
        try {
            return values.getEntry(i);
        } catch (IndexOutOfBoundsException e) { // have to catch and rethrow to avoid RuntimeException
            throw new IllegalArgumentException("invalid discrete value", e);
        }
    }

    @Override
    public int index(double val) {
        final int n = values.getDimension();
        assert n > 0;
        int closest = -1;
        double closev = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double diff = Math.abs(val - values.getEntry(i));
            if (diff <= closev) { // <= to round up
                closev = diff;
                closest = i;
            }
        }
        if (closest < 0) {
            throw new RuntimeException("could not quantize value");
        } else {
            return closest;
        }
    }

    @Override
    public double quantize(double val) {
        return getIndexValue(index(val));
    }
}
