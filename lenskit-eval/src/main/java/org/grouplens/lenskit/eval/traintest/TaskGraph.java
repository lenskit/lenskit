package org.grouplens.lenskit.eval.traintest;

import java.util.concurrent.Callable;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TaskGraph {
    public static Node groupNode() {
        return new NoopNode();
    }
    public static Node jobNode(TrainTestEvalJob job) {
        return new JobNode(job);
    }
    public static Edge edge() {
        return Edge.NONE;
    }

    public abstract static interface Node extends Callable<Void> {}

    static class NoopNode implements Node {
        @Override
        public Void call() throws Exception {
            return null;
        }

        @Override
        public String toString() {
            return "no-op";
        }
    }

    static class JobNode implements Node {
        TrainTestEvalJob job;

        JobNode(TrainTestEvalJob job) {
            this.job = job;
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
