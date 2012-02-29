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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A context for preparing jobs and data sources.  It is used to provide options
 * and to allow preparation to be memoized.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class PreparationContext {
    private static final Logger logger = LoggerFactory.getLogger(PreparationContext.class);
    private boolean force;
    private File cacheDirectory;
    private Map<Preparable,Throwable> seen =
            new HashMap<Preparable,Throwable>();
    
    /**
     * Query whether this is an unconditional preparation. If it is, then
     * objects should do on-disk preparations of themselves regardless of
     * whether they have been prepared already.
     * 
     * @return <tt>true</tt> if objects should prepare themselves even if they
     *         have already been prepared by a previous preparation run.
     */
    public boolean isUnconditional() {
        return force;
    }
    
    /**
     * Set whether this is an unconditional preparation.
     * @param force If <tt>true</tt>, the preparation is unconditional.
     * @return This object for chaining.
     * @see #isUnconditional()
     */
    public PreparationContext setUnconditional(boolean force) {
        this.force = force;
        return this;
    }
    
    /**
     * Get the directory used for caching data.
     * @return The directory storing cached data.
     */
    public File getCacheDirectory() {
        return cacheDirectory;
    }
    
    /**
     * Set the directory to be used for caching data.
     * @param dir The directory to store cached data.
     */
    public PreparationContext setCacheDirectory(File dir) {
        cacheDirectory = dir;
        return this;
    }
    
    /**
     * Set the directory to be used for caching data.
     * @param dir The directory to store cached data.
     */
    public PreparationContext setCacheDirectory(String dir) {
        setCacheDirectory(new File(dir));
        return this;
    }
    
    /**
     * Prepare and memoize an object. If the object has already been prepared in
     * this context, it is not prepared again.
     * 
     * @param obj The object to prepare
     * @throws PreparationException if the prepration failed. This is memoized
     *         and re-thrown if preparation is attempted of the same object
     *         again.
     */
    public void prepare(Preparable obj) throws PreparationException {
        if (seen.containsKey(obj)) {
            logger.debug("{} already prepared", obj);
            Throwable thr = seen.get(obj);
            if (thr instanceof PreparationException) {
                throw (PreparationException) thr;
            } else if (thr instanceof RuntimeException) {
                throw (RuntimeException) thr;
            } else if (thr != null) {
                throw new RuntimeException("Unexpected exception", thr);
            }
        } else {
            try {
                logger.debug("Preparing {}", obj);
                obj.prepare(this);
            } catch (PreparationException e) {
                seen.put(obj, e);
                throw e;
            } catch (RuntimeException e) {
                seen.put(obj, e);
                throw e;
            }
            seen.put(obj, null);
        }
    }
}
