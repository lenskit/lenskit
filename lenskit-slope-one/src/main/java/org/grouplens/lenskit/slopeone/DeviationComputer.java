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