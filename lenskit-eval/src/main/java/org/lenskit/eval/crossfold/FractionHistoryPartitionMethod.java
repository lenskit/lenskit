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
package org.lenskit.eval.crossfold;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.data.ratings.Rating;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 * Partition a list by fraction.
 */
public class FractionHistoryPartitionMethod implements HistoryPartitionMethod {

    private double fraction;

    /**
     * The fraction to hold out (put in the second partition).
     *
     * @param f The fraction of users to hold out.
     */
    public FractionHistoryPartitionMethod(double f) {
        fraction = f;
    }

    @Override
    public int partition(List<? extends Rating> data) {
        int n = (int) round(data.size() * fraction);
        return max(0, data.size() - n);
    }

    @Override
    public String toString() {
        return String.format("fraction(%f)", fraction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FractionHistoryPartitionMethod that = (FractionHistoryPartitionMethod) o;

        return new EqualsBuilder()
                .append(fraction, that.fraction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fraction)
                .toHashCode();
    }
}
