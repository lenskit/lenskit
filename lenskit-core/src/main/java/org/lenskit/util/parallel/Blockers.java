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
