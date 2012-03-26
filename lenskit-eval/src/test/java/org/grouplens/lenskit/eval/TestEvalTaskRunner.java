package org.grouplens.lenskit.eval;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Ekstrand
 */
public class TestEvalTaskRunner {
    EvalTaskRunner runner;

    private static class Flag {
        boolean reached = false;
        String name;

        public Flag(String n) {
            name = n;
        }

        public void set() {
            reached = true;
        }

        public void assertReached() {
            assertTrue(String.format("flag '%s' not set", name), reached);
        }

        public void assertNotReached() {
            assertFalse(String.format("flag '%s' set unexpectedly", name), reached);
        }
    }

    private static class FlagSetTaskBuilder extends AbstractEvalTaskBuilder<EvalTask> {
        private Flag flag;
        private List<Runnable> before = new LinkedList<Runnable>();

        public FlagSetTaskBuilder setFlag(Flag f) {
            flag = f;
            return this;
        }
        
        public FlagSetTaskBuilder runBefore(Runnable r) {
            before.add(r);
            return this;
        }

        public EvalTask build() {
            return new AbstractEvalTask(getName(), getDependencies()) {
                public void execute(EvalOptions opts) {
                    for (Runnable r: before) {
                        r.run();
                    }
                    flag.assertNotReached();
                    flag.set();
                }
            };
        }
    }

    @Before
    public void createRunner() {
        runner = new EvalTaskRunner(new EvalOptions());
    }

    @Test
    public void testRunTask() throws EvalTaskFailedException {
        final Flag flag = new Flag("ran");
        EvalTask task = new FlagSetTaskBuilder()
                .setFlag(flag)
                .setName("test")
                .build();
        runner.execute(task);
        flag.assertReached();
    }

    @Test
    public void testRunDeps() throws EvalTaskFailedException {
        final Flag flagA = new Flag("ran A");
        final Flag flagB = new Flag("ran B");
        EvalTask taskA = new FlagSetTaskBuilder()
                .setFlag(flagA)
                .setName("testA")
                .build();
        EvalTask taskB = new FlagSetTaskBuilder()
                .setFlag(flagB)
                .runBefore(new Runnable() {
                    public void run() {
                        flagA.assertReached();
                    }
                })
                .setName("testB")
                .addDepends(taskA)
                .build();
        runner.execute(taskB);
        flagB.assertReached();
    }

    @Test
    public void testDiamondDeps() throws EvalTaskFailedException {
        final Flag flagA = new Flag("ran A");
        final Flag flagB = new Flag("ran B");
        final Flag flagC = new Flag("ran C");
        final Flag flagD = new Flag("ran D");
        EvalTask taskA = new FlagSetTaskBuilder()
                .setFlag(flagA)
                .setName("testA")
                .build();
        EvalTask taskB = new FlagSetTaskBuilder()
                .setFlag(flagB)
                .setName("testB")
                .addDepends(taskA)
                .build();
        EvalTask taskC = new FlagSetTaskBuilder()
                .setFlag(flagC)
                .setName("testC")
                .addDepends(taskA)
                .build();
        EvalTask taskD = new FlagSetTaskBuilder()
                .setFlag(flagD)
                .setName("testD")
                .addDepends(taskB).addDepends(taskC)
                .build();
        runner.execute(taskD);
        flagB.assertReached();
        flagC.assertReached();
        flagD.assertReached();
    }

    @Test(expected = EvalTaskFailedException.class)
    public void testCatchRuntimeError() throws EvalTaskFailedException {
        runner.execute(new AbstractEvalTask("fail", Collections.<EvalTask>emptySet()) {
            @Override
            public void execute(EvalOptions options) throws EvalTaskFailedException {
                throw new RuntimeException("Failed!");
            }
        });
    }
}
