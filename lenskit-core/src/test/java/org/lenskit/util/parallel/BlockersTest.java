/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.parallel;

import com.google.common.util.concurrent.Monitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlockersTest {
    @Test
    public void testAcquireMonitor() {
        Monitor monitor = new Monitor();
        ForkJoinPool.commonPool().invoke(new RecursiveAction() {
            AtomicInteger running = new AtomicInteger();

            @Override
            protected void compute() {
                List<RecursiveAction> actions = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    actions.add(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            try {
                                Blockers.enterMonitor(monitor);
                                try {
                                    assertThat(monitor.isOccupiedByCurrentThread(), equalTo(true));
                                    int n = running.incrementAndGet();
                                    assertThat(n, equalTo(1));
                                    Thread.sleep(100);
                                    running.decrementAndGet();
                                } finally {
                                    monitor.leave();
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                invokeAll(actions);
                for (RecursiveAction action: actions) {
                    action.join();
                }
            }
        });
    }

    @Test
    public void testAcquireSem() {
        Semaphore sem = new Semaphore(2);
        ForkJoinPool.commonPool().invoke(new RecursiveAction() {
            AtomicInteger running = new AtomicInteger();

            @Override
            protected void compute() {
                List<RecursiveAction> actions = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    actions.add(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            try {
                                Blockers.acquireSemaphore(sem);
                                try {
                                    int n = running.incrementAndGet();
                                    assertThat(n, greaterThan(0));
                                    assertThat(n, lessThanOrEqualTo(2));
                                    Thread.sleep(100);
                                    running.decrementAndGet();
                                } finally {
                                    sem.release();
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                invokeAll(actions);
                for (RecursiveAction action: actions) {
                    action.join();
                }
            }
        });
    }
}