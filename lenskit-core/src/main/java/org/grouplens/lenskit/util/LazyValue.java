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

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

/**
 * A thread-safe lazy value class.  It waits until its value is actually required
 * to compute it, then caches it.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class LazyValue<T> {
    private volatile T value;
    private Callable<T> provider; // set to null once the value is computed
    
    /**
     * Create a lazy value whose value will be provided by a callable.
     * @param f The callable responsible for providing the lazy value.
     */
    public LazyValue(@Nonnull Callable<T> f) {
        provider = f;
    }
    
    /**
     * Get the value, computing it if necessary.
     * @return The value returned by the callable.
     */
    public synchronized T get() {
        if (provider != null) {
            // still have the provider, we need to compute the value
            try {                
                value = provider.call();
            } catch (Exception e) {
                throw new RuntimeException("Error computing lazy value", e);
            }
            // free the provider, mark as computed
            provider = null;
        }
        return value;
    }
}
