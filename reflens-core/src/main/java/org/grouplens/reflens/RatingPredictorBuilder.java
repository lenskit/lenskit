/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
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

package org.grouplens.reflens;

import org.grouplens.reflens.data.RatingDataSource;

public interface RatingPredictorBuilder {

	public RatingPredictor build(RatingDataSource data);
	
	public static class RecEngineBuilderWrapper implements RatingPredictorBuilder {
		private final RecommenderBuilder builder;

		public RecEngineBuilderWrapper(RecommenderBuilder builder) {
			this.builder = builder;
		}
		
		public RatingPredictor build(RatingDataSource data) {
			RecommendationEngine engine = builder.build(data);
			RatingPredictor pred = engine.getRatingPredictor();
			if (pred != null)
				return pred;
			else
				throw new RuntimeException("Recommender does not support rating prediction");
		}
	}
}
