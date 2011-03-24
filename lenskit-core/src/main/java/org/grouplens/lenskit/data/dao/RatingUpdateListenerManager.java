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
package org.grouplens.lenskit.data.dao;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.Rating;

/**
 * Helper class to manage rating update listeners.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ThreadSafe
public class RatingUpdateListenerManager {
    private List<RatingUpdateListener> listeners;
    
    public RatingUpdateListenerManager() {
        listeners = new LinkedList<RatingUpdateListener>();
    }
    
    public synchronized void addListener(RatingUpdateListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeListener(RatingUpdateListener l) {
        listeners.remove(l);
    }
    
    public synchronized void invoke(Rating oldRating, Rating newRating) {
        for (RatingUpdateListener l: listeners) {
            l.ratingUpdated(oldRating, newRating);
        }
    }
}
