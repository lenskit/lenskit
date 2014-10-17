/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit;

/**
 * Exception thrown when there is an error building a recommender.
 *
 * @since 1.0
 */
public class RecommenderBuildException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Consruct a new recommender build exception.
     */
    public RecommenderBuildException() {
    }

    /**
     * Construct exception with a message.
     * @param message The exception's message.
     */
    public RecommenderBuildException(String message) {
        super(message);
    }

    /**
     * Construct exception with a message and cause.
     * @param message The exception message.
     * @param cause The exception cause.
     */
    public RecommenderBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct an exception with a cause.
     * @param cause The underlying cause of the build exception.
     */
    public RecommenderBuildException(Throwable cause) {
        super(cause);
    }
}
