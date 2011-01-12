package org.grouplens.reflens.bench;

import uk.co.flamingpenguin.jewel.cli.Option;

interface CrossfoldOptions {

	/**
	 * @return The number of folds to use (where 10 is 10-fold, 90/10 train/test
	 *         split).
	 */
	@Option(longName = "num-folds", shortName = "n", defaultValue = "10")
	public abstract int getNumFolds();

	@Option(longName = "holdout-fraction", defaultValue = "0.3333333")
	public abstract double getHoldoutFraction();
}