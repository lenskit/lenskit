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

import java.util.concurrent.Callable;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TaskGraph {
    public static Node groupNode() {
        return new NoopNode();
    }
    public static Node jobNode(TrainTestJob job) {
        return new JobNode(job);
    }
    public static Edge edge() {
        return Edge.NONE;
    }

    public abstract static interface Node extends Callable<Void> {
        TrainTestJob getJob();
    }

    static class NoopNode implements Node {
        @Override
        public Void call() throws Exception {
            return null;
        }

        @Override
        public String toString() {
            return "no-op";
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
    }

    public static enum Edge {
        NONE
    }
}
