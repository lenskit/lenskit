/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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