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
	
	@Option(longName="progress-bar")
	boolean showProgress();

	/**
	 * @return A list of files to use as input data.
	 */
	@Unparsed
	List<String> getFiles();

	@Option(helpRequest = true)
	boolean getHelp();
}