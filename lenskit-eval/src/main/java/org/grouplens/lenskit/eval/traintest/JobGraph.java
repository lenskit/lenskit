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

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.CachedSatisfaction;
import org.grouplens.grapht.solver.DesireChain;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JobGraph {
    public static Node noopNode(String label) {
        return new NoopNode(label);
    }
    public static Node jobNode(TrainTestJob job) {
        return new JobNode(job);
    }
    public static Edge edge() {
        return edge(ImmutableSet.<DAGNode<CachedSatisfaction, DesireChain>>of());
    }
    public static Edge edge(Set<DAGNode<CachedSatisfaction, DesireChain>> deps) {
        return new Edge(deps);
    }

    public static void writeGraphDescription(DAGNode<Node, Edge> jobGraph, File taskGraphFile) throws IOException {
        Files.createParentDirs(taskGraphFile);
        PrintWriter print = new PrintWriter(taskGraphFile);
        try {
            for (DAGNode<Node,Edge> node: jobGraph.getSortedNodes()) {
                print.print("- ");
                print.println(node.getLabel());
                Set<DAGNode<Node,Edge>> deps = node.getAdjacentNodes();
                if (!deps.isEmpty()) {
                    print.println("  dependencies:");
                    for (DAGNode<Node,Edge> dep: deps) {
                        print.print("  - ");
                        print.println(dep.getLabel());
                    }
                }
            }
        } finally {
            print.close();
        }
    }

    public abstract static interface Node extends Callable<Void> {
        TrainTestJob getJob();
    }

    static class NoopNode implements Node {
        private final String label;

        public NoopNode(String lbl) {
            label = lbl;
        }

        @Override
        public Void call() throws Exception {
            return null;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public TrainTestJob getJob() {
            return null;
        }
    }

    static class JobNode implements Node {
        TrainTestJob job;

        JobNode(TrainTestJob job) {
            this.job = job;
        }

        @Override
        public TrainTestJob getJob() {
            return job;
        }

        @Override
        public Void call() throws Exception {
            return job.call();
        }

        @Override
        public String toString() {
            return job.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JobNode jobNode = (JobNode) o;

            if (job != null ? !job.equals(jobNode.job) : jobNode.job != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return job != null ? job.hashCode() : 0;
        }
    }

    public static class Edge implements Set<DAGNode<CachedSatisfaction,DesireChain>> {
        private final Set<DAGNode<CachedSatisfaction,DesireChain>> dependencies;

        public Edge(Set<DAGNode<CachedSatisfaction, DesireChain>> deps) {
            this.dependencies = ImmutableSet.copyOf(deps);
        }

        @Override
        public int size() {
            return dependencies.size();
        }

        @Override
        public boolean isEmpty() {
            return dependencies.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return dependencies.contains(o);
        }

        @Override
        public Iterator<DAGNode<CachedSatisfaction, DesireChain>> iterator() {
            return dependencies.iterator();
        }

        @Override
        public Object[] toArray() {
            return dependencies.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return dependencies.toArray(a);
        }

        @Override
        public boolean add(DAGNode<CachedSatisfaction, DesireChain> cachedSatisfactionDesireChainDAGNode) {
            return dependencies.add(cachedSatisfactionDesireChainDAGNode);
        }

        @Override
        public boolean remove(Object o) {
            return dependencies.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return dependencies.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends DAGNode<CachedSatisfaction, DesireChain>> c) {
            return dependencies.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return dependencies.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return dependencies.removeAll(c);
        }

        @Override
        public void clear() {
            dependencies.clear();
        }

        @Override
        public boolean equals(Object o) {
            return dependencies.equals(o);
        }

        @Override
        public int hashCode() {
            return dependencies.hashCode();
        }
    }
}
