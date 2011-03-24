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
package org.grouplens.lenskit;

/**
 * Exception thrown when the configured recommender is incompatible with its
 * usage.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IncompatibleRecommenderException extends UnsupportedOperationException {
    private static final long serialVersionUID = -570479509636551006L;

    /**
     *
     */
    public IncompatibleRecommenderException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public IncompatibleRecommenderException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public IncompatibleRecommenderException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public IncompatibleRecommenderException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
