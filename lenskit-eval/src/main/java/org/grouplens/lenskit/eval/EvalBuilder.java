/*
 * LensKit, an open source recommender systems toolkit.
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

import java.util.Properties;

import org.grouplens.lenskit.util.dtree.DataNode;
import org.grouplens.lenskit.util.spi.ServiceFinder;

/**
 * Primary interface for evaluators — methods for evaluating recommenders.
 * 
 * <p>
 * Evaluators should be usable as singletons (they will typically be loaded via
 * a {@link ServiceFinder}). The {@link #configure(Properties, DataNode)} method
 * takes a configuration and creates the jobs which, when run, will execute all
 * evaluations configured for this evaluator.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface EvalBuilder {
    /**
     * Get a human-readable name for this evaluator. This name shouldn't be
     * long, but should be suitable for display in user interfaces.
     * 
     * @return A name for the evaluator.
     */
    String getName();
    
    /**
     * Process a configuration and make the jobs required to run the specified
     * configuration.
     * 
     * @param properties Properties to control the evaluation.
     * @param config The configuration to be used.
     * @return A collection of job groups to execute this evaluation.
     * @throws EvaluatorConfigurationException if there is an error in the
     *         evaluator configuration.
     */
    Evaluation configure(Properties properties, DataNode config) throws EvaluatorConfigurationException;
}
