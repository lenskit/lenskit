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
package org.grouplens.lenskit.eval.traintest;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.mozilla.javascript.Scriptable;

/**
 * Scriptable class for building evaluation recipes.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ScriptedRecipeBuilder {
    private List<AlgorithmInstance> algorithms = new ArrayList<AlgorithmInstance>();
    @SuppressWarnings("unused")
    private Scriptable scope;

    public ScriptedRecipeBuilder(Scriptable scope) {
        this.scope = scope;
    }

    /**
     * Create a new algorithm and add it to the algorithm list. The script
     * should then fill in the algorithm's details.
     * @return An <tt>AlgorithmInstance</tt> object for the new algorithm.
     */
    public AlgorithmInstance addAlgorithm() {
        AlgorithmInstance a = new AlgorithmInstance();
        algorithms.add(a);
        return a;
    }

    public List<AlgorithmInstance> getAlgorithms() {
        return algorithms;
    }
}