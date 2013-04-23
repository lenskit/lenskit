/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

/**
 * A single evaluation job.  This is typically building and evaluating a single
 * recommender on a single data set.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public interface Job extends Runnable {
    /**
     * Get a descriptive name for this job.  The name is displayed in UI to let
     * the user know what is being run.  More specific descriptors identifying
     * this job to allow its output to be processed should be output directly
     * to the output handler when the job is run.
     *
     * @return The name for this job.
     */
    String getName();

    /**
     * Run this job.
     */
    @Override
    void run();
}
