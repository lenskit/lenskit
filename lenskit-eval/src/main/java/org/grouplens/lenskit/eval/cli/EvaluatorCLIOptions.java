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
package org.grouplens.lenskit.eval.cli;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * Evaluator CLI options.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@CommandLineInterface(application="lenskit-eval")
public interface EvaluatorCLIOptions {
    @Option(longName="force-prepare", shortName="f",
            description="re-prepare data sets even if up to date")
    boolean getForce();
    
    @Option(longName="threads", shortName="j",
            description="the number of threads to use (0 to use all)",
            defaultValue="0")
    int getThreadCount();
    
    @Option(longName="isolate", description="isolate job groups")
    boolean isolate();
    
    @Option(longName="cache-dir", shortName="c", description="directory for cache files",
            defaultValue="")
    File getCacheDir();
    
    @Option(longName="prepare-only", description="only prepare eval, do not run")
    boolean prepareOnly();
    
    @Option(longName="throw-errors", description="Throw exceptions rather than exiting")
    boolean throwErrors();
    
    @Unparsed(name="CONFIGS")
    List<File> configFiles();
    
    @Option(helpRequest=true)
    boolean getHelp();
}
