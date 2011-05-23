package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.slopeone.params.DeviationDamping;

public class DeviationComputer {
	
	private double damping;
	
	public DeviationComputer(@DeviationDamping double damping) {
		this.damping = damping;
	}
		
	public double findDeviation(double totalDiff, int nusers) {
		return totalDiff/(nusers+damping);
	}

}
