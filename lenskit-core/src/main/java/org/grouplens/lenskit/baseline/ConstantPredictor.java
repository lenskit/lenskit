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
/**
 *
 */
package org.grouplens.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.grapht.annotation.Parameter;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.*;

import static org.grouplens.lenskit.vectors.VectorEntry.State;

/**
 * Rating scorer that predicts a constant rating for all items.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@Shareable
@Singleton
public class ConstantPredictor extends AbstractBaselinePredictor {
    /**
     * Parameter: the value used by the constant scorer.
     */
    @Documented
    @DefaultDouble(0.0)
    @Qualifier
    @Parameter(Parameter.PrimitiveType.DOUBLE)
    @Target({ ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Value { }

    private static final long serialVersionUID = 1L;

    private final double value;

    /**
     * Construct a new constant scorer.  This is exposed so other code
     * can use it as a fallback.
     * @param value The value to use.
     */
    @Inject
    public ConstantPredictor(@Value double value) {
        this.value = value;
    }

    @Override
    public void predict(long user, SparseVector ratings,
                        MutableSparseVector output, boolean predictSet) {
        if (predictSet) {
            output.fill(value);
        } else {
            for (VectorEntry e: output.fast(State.UNSET)) {
                output.set(e, value);
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s(%.3f)", getClass().getCanonicalName(), value);
    }
}
