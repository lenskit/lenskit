/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.transform.quantize;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A rating predictor wrapper that quantizes predictions.
 * @author Michael Ekstrand
 */
public class QuantizedRatingPredictor extends AbstractItemScorer implements RatingPredictor {
    private RatingPredictor basePredictor;
    private Quantizer quantizer;

    /**
     * Construct a new quantized predictor.
     * @param dao The DAO.
     * @param base The base predictor.
     * @param q The quantizer.
     */
    @Inject
    public QuantizedRatingPredictor(DataAccessObject dao, RatingPredictor base, Quantizer q) {
        super(dao);
        basePredictor = base;
        quantizer = q;
    }

    private void quantize(MutableSparseVector scores) {
        for (VectorEntry e: scores.fast()) {
            scores.set(e, quantizer.getIndexValue(quantizer.index(e.getValue())));
        }
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        basePredictor.score(user, scores);
        quantize(scores);
    }

    @Override
    public void score(@Nonnull UserHistory<? extends Event> profile, @Nonnull MutableSparseVector scores) {
        basePredictor.score(profile, scores);
        quantize(scores);
    }
}
