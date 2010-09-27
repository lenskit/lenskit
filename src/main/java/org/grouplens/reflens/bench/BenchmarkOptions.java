package org.grouplens.reflens.bench;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * Interface for JewelCLI declaring the command line options taken by
 * BenchmarkRunner.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
interface BenchmarkOptions {
	/**
	 * @return The field separator in the data file.
	 */
	@Option(longName = "delimiter", shortName = "d", defaultValue = "\t")
	String getDelimiter();

	/**
	 * @return The name of the recommender module class to use.
	 */
	@Option(longName = "module", shortName = "m", defaultValue = "")
	String getModule();

	/**
	 * @return The number of folds to use (where 10 is 10-fold, 90/10 train/test
	 *         split).
	 */
	@Option(longName="num-folds", shortName="n", defaultValue="10")
	int getNumFolds();
	
	@Option(longName="holdout-fraction", defaultValue="0.3333333")
	double getHoldoutFraction();

	/**
	 * @return A list of files to use as input data.
	 */
	@Unparsed
	List<String> getFiles();

	@Option(helpRequest = true)
	boolean getHelp();
}