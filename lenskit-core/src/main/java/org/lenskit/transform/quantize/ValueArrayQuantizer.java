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

import com.google.common.base.Preconditions;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.inject.Shareable;

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
            throw new IllegalArgumentException("could not quantize value");
        } else {
            return closest;
        }
    }

    @Override
    public double quantize(double val) {
        return getIndexValue(index(val));
    }
}
