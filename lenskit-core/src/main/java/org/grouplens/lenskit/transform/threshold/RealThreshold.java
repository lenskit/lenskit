/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.transform.threshold;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.params.ThresholdValue;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Checks similarity values to ensure their real values are
 * over the {@link ThresholdValue}.
 */
@Shareable
public class RealThreshold implements Threshold, Serializable {
    private static final long serialVersionUID = 1L;

    private final double thresholdValue;

    @Inject
    public RealThreshold(@ThresholdValue double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    /**
     * Get the threshold value.
     * @return The threshold value.
     */
    public double getValue() {
        return thresholdValue;
    }

    @Override
    public boolean retain(double sim) {
        return sim > thresholdValue;
    }
}
