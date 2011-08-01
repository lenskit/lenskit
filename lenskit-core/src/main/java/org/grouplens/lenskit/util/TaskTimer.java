/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.util;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TaskTimer {
    private long startTime;
    private long stopTime;

    public TaskTimer() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        stopTime = -1;
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
    }

    public long elapsedMillis() {
        long stop = stopTime;
        if (stop < 0)
            stop = System.currentTimeMillis();
        return stop - startTime;
    }

    public double elapsed() {
        return elapsedMillis() * 0.001;
    }

    public String elapsedPretty() {
        long elapsed = elapsedMillis();
        long secs = elapsed / 1000;
        long mins = secs / 60;
        long hrs = mins / 60;
        StringBuilder s = new StringBuilder();
        if (hrs > 0)
            s.append(String.format("%dh", hrs));
        if (mins > 0)
            s.append(String.format("%dm", mins % 60));
        s.append(String.format("%ds", secs % 60));
        return s.toString();
    }
    
    @Override
    public String toString() {
        return elapsedPretty();
    }
}
