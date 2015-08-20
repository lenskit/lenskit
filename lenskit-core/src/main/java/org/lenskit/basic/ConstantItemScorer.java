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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.core.Shareable;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.io.Serializable;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Item scorer that returns a fixed score for all items.
 */
@Shareable
public class ConstantItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double fixedScore;

    /**
     * Create a new constant item scorer.
     * @param score The score to return.
     */
    @Inject
    public ConstantItemScorer(@Value double  score) {
        fixedScore = score;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            results.add(Results.create(item, fixedScore));
        }
        return Results.newResultMap(results);
    }

    /**
     * The value used by the constant scorer.
     */
    @Documented
    @DefaultDouble(0.0)
    @Qualifier
    @Parameter(Double.class)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Value {
    }
}
