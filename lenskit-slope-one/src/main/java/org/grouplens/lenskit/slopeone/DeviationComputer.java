/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.slopeone.params.DeviationDamping;


/**
 * Computes the deviation between two items based on their shared ratings.
 */
public class DeviationComputer {
	
	private double damping;
	
	public DeviationComputer(@DeviationDamping double damping) {
		this.damping = damping;
	}
		
	/**
	 * Calculates the deviation between two items.
	 * @param totalDiff The total difference between the two items' mutual ratings
	 * @param nusers The number of users who provided these ratings
	 * @return The deviation between the two relevant items
	 */
	public double findDeviation(double totalDiff, int nusers) {
		return totalDiff/(nusers+damping);
	}

}