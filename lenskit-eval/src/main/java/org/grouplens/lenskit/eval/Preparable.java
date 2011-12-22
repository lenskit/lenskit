/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.eval;

/**
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Preparable {
    /**
     * Get the update timestamp of this data source, used to determine if
     * derived data needs to be updated. If a preparable object should only have
     * derived objects re-prepared when preparation is forced, then it can
     * return 0L.
     * 
     * @return The last modification timestamp of this preparable, or -1L if the
     *         object is not prepared at all.
     */
    long lastUpdated(PreparationContext context);
    
    /**
     * Prepare the object. This may involve pre-processing input data, splitting
     * a data set, or other behavior.
     * 
     * <p>Objects which need to prepare other objects must do so by calling
     * {@link PreparationContext#prepare(Preparable)} rather than this method
     * directly so that preparation is memoized.
     * 
     * <p>The preparation can involve both in-memory object setup (e.g. finding
     * files or configuring databases) and on-disk computations (creating cached
     * databases, etc.).
     * 
     * @param context Context and options in which this preparation is run. Used
     *        to allow shared dependencies to only be prepared once per run.
     * @throws PreparationException if there is an error preparing the object.
     */
    void prepare(PreparationContext context) throws PreparationException;
}
