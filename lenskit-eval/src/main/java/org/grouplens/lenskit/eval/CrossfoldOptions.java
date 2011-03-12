/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.eval;

import uk.co.flamingpenguin.jewel.cli.Option;

public interface CrossfoldOptions {

    /**
     * @return The number of folds to use (where 10 is 10-fold, 90/10 train/test
     *         split).
     */
    @Option(longName = "num-folds", shortName = "n", defaultValue = "10")
    public abstract int getNumFolds();

    @Option(longName = "holdout-fraction", defaultValue = "0.3333333")
    public abstract double getHoldoutFraction();

    @Option(longName="write-predictions", defaultValue="",
            description="Write predictions to CSV file")
    public String predictionFile();
}
