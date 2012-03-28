/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
