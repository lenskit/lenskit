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
