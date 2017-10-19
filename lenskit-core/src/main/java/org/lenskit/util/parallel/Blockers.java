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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

/**
 * Utility classes for blocking computations.
 */
public class Blockers {
    /**
     * Enter a monitor, releasing coordinating with the fork-join pool.
     * @param m The monitor to enter.
     */
    public static void enterMonitor(Monitor m) throws InterruptedException {
        ForkJoinPool.managedBlock(new MonitorBlocker(m));
    }

    /**
     * Acquire a semaphore, coordinating with the fork-join pool if one is running.
     * @param s The semaphore to acquire.
     */
    public static void acquireSemaphore(Semaphore s) throws InterruptedException {
        ForkJoinPool.managedBlock(new SemaphoreBlocker(s));
    }

    private static class MonitorBlocker implements ForkJoinPool.ManagedBlocker {
        private final Monitor monitor;

        public MonitorBlocker(Monitor m) {
            monitor = m;
        }

        @Override
        public boolean block() throws InterruptedException {
            if (!monitor.isOccupiedByCurrentThread())
                monitor.enter();
            return true;
        }

        @Override
        public boolean isReleasable() {
            return monitor.isOccupiedByCurrentThread() || monitor.tryEnter();
        }
    }

    private static class SemaphoreBlocker implements ForkJoinPool.ManagedBlocker {
        private final Semaphore semaphore;
        private boolean acquired = false;

        public SemaphoreBlocker(Semaphore s) {
            semaphore = s;
        }

        @Override
        public boolean block() throws InterruptedException {
            // wait for the semaphore to be available
            if (!acquired) {
                semaphore.acquire();
                acquired = true;
            }
            return acquired;
        }

        @Override
        public boolean isReleasable() {
            // can release if we have the semaphore, or if we can immediately acquire it
            return acquired || (acquired = semaphore.tryAcquire());
        }
    }
}
