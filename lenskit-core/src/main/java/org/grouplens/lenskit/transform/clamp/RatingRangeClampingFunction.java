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
package org.grouplens.lenskit.transform.clamp;

import org.lenskit.inject.Shareable;
import org.lenskit.data.ratings.PreferenceDomain;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Clamp values to the range of valid ratings. This clamping function uses
 * the {@link PreferenceDomain} to clamp values to fall within the minimum
 * and maximum allowable ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@Shareable
public class RatingRangeClampingFunction implements ClampingFunction, Serializable {
    private static final long serialVersionUID = 1L;

    private final PreferenceDomain domain;

    @Inject
    public RatingRangeClampingFunction(PreferenceDomain dom) {
        domain = dom;
    }

    public double apply(long user, long item, double value) {
        return domain.clampValue(value);
    }
}
