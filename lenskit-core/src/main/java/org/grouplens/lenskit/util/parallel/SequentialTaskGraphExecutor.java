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
package org.grouplens.lenskit.util.parallel;

import org.grouplens.grapht.graph.DAGNode;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Single-threaded (sequential) task graph executor.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SequentialTaskGraphExecutor extends TaskGraphExecutor {
    @Override
    public <T extends Callable<?>,E> void execute(DAGNode<T,E> graph) throws ExecutionException, InterruptedException {
        for (DAGNode<T,?> node: graph.getSortedNodes()) {
            try {
                node.getLabel().call();
            } catch (InterruptedException ex) {
                throw ex;
            } catch (Throwable th) {
                throw new ExecutionException("error in graph task", th);
            }
        }
    }
}
