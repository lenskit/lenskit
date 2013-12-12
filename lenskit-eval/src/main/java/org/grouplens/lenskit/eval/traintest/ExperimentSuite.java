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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;

import java.util.List;
import java.util.Set;

/**
 * A suite of experiments (algorithms and data sets).
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ExperimentSuite {
    private final List<AlgorithmInstance> algorithms;
    private final List<ExternalAlgorithm> externalAlgorithms;
    private final List<TTDataSet> dataSets;

    public ExperimentSuite(List<AlgorithmInstance> algos,
                           List<ExternalAlgorithm> externalAlgos,
                           List<TTDataSet> data) {
        algorithms = algos;
        externalAlgorithms = externalAlgos;
        dataSets = data;
    }

    /**
     * Get the list of algorithms to be tested.
     * @return The list of algorithms.
     */
    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }

    /**
     * Get the list of external algorithms to be tested.
     * @return The list of external algorithms.
     */
    public List<ExternalAlgorithm> getExternalAlgorithms() {
        return externalAlgorithms;
    }

    public Iterable<Attributed> getAllAlgorithms() {
        return Iterables.concat(algorithms, externalAlgorithms);
    }

    /**
     * Get the list of data sets to be tested.
     * @return The list of data sets.
     */
    public List<TTDataSet> getDataSets() {
        return dataSets;
    }

    /**
     * Get the set of all algorithmInfo attribute names.
     * @return The set of all algorithmInfo attribute names.
     */
    public Set<String> getAlgorithmAttributes() {
        Set<String> attrs = Sets.newLinkedHashSet();
        for (Attributed algo: getAllAlgorithms()) {
            attrs.addAll(algo.getAttributes().keySet());
        }
        return attrs;
    }

    /**
     * Get the set of all data set attribute names.
     * @return The set of all data set attribute names.
     */
    public Set<String> getDataAttributes() {
        Set<String> attrs = Sets.newLinkedHashSet();
        for (TTDataSet ds: dataSets) {
            attrs.addAll(ds.getAttributes().keySet());
        }
        return attrs;
    }
}
