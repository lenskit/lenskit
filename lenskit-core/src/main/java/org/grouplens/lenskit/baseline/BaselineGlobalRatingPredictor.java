/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

package org.grouplens.lenskit.baseline;

import java.util.Collection;

import org.grouplens.lenskit.GlobalRatingPredictor;
import org.grouplens.lenskit.core.AbstractGlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.SparseVector;

public class BaselineGlobalRatingPredictor extends AbstractGlobalItemScorer implements GlobalRatingPredictor{
	private BaselineGlobalPredictor predictor;
	
    /**
     * Construct a new baseline rating predictor.
     * 
     * @param baseline The baseline predictor to use.
     * @param dao The DAO.
     */
    public BaselineGlobalRatingPredictor(BaselineGlobalPredictor baseline, DataAccessObject dao) {
        super(dao);
        predictor = baseline;
    }

    /**
     * Delegate to {@link BaselineGlobalPredictor#predict(long, Collection)}.
     */
    @Override
    public SparseVector globalScore(long queryItem, Collection<Long> items) {
        return predictor.globalPredict(queryItem, items);
    }

}
