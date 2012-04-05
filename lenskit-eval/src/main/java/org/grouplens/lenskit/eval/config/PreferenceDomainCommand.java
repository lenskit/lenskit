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
package org.grouplens.lenskit.eval.config;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandFailedException;

/**
 * Builder for {@link PreferenceDomain} objects.
 * @review Should this be here, or do we want to provide it in the standard core?
 * @author Michael Ekstrand
 */
public class PreferenceDomainCommand extends AbstractCommand<PreferenceDomain>{
    private Double min;
    private Double max;
    private Double precision;

    public boolean hasMinimum() {
        return min != null;
    }

    public double getMinimum() {
        Preconditions.checkState(min != null, "no minimum set");
        return min;
    }

    public PreferenceDomainCommand setMinimum(double v) {
        min = v;
        return this;
    }

    public boolean hasMaximum() {
        return max != null;
    }

    public double getMaximum() {
        Preconditions.checkState(max != null, "no maximum set");
        return max;
    }

    public PreferenceDomainCommand setMaximum(double v) {
        max = v;
        return this;
    }

    public boolean hasPrecision() {
        return precision != null;
    }

    public double getPrecision() {
        Preconditions.checkState(precision != null, "no precision set");
        return precision;
    }

    public PreferenceDomainCommand setPrecision(double v) {
        precision = v;
        return this;
    }

    @Override
    public PreferenceDomain call() throws CommandFailedException {
        Preconditions.checkState(min != null, "no minimum set");
        Preconditions.checkState(max != null, "no maximum set");
        if (precision == null) {
            return new PreferenceDomain(min, max);
        } else {
            return new PreferenceDomain(min, max, precision);
        }
    }
}
