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
package org.grouplens.lenskit.eval;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * Interface for JewelCLI declaring the command line options taken by
 * EvaluationRunner.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
interface EvaluatorOptions extends CrossfoldOptions {
    /**
     * @return The field separator in the data file.
     */
    @Option(longName = "delimiter", shortName = "d", defaultValue = "\t")
    String getDelimiter();

    @Option(longName="input-file", shortName="i", defaultValue="ratings.dat")
    File getInputFile();

    @Option(longName="preload", shortName="p",
            description="Load all ratings into memory once")
    boolean preloadData();

    @Option(longName="output-file", shortName="o", defaultValue="")
    File getOutputFile();

    @Unparsed(name="FILES")
    List<File> getRecommenderSpecs();

    @Option(helpRequest = true)
    boolean getHelp();
}
