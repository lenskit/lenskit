/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit;

/**
 * Exception thrown when a recommender is not available.
 *
 * <p>Recommenders can be unavailable for a variety of reasons: there could be
 * no recommender in a cache and no means to build one, or there could be an
 * error building the recommender, or any of a variety of problems.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderNotAvailableException extends Exception {
    private static final long serialVersionUID = 7518432427712149396L;

    /**
     *
     */
    public RecommenderNotAvailableException() {
    }

    /**
     * @param message
     */
    public RecommenderNotAvailableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public RecommenderNotAvailableException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public RecommenderNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
