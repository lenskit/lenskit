/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.api;

/**
 * Exception thrown when there is an error building a recommender.
 *
 * @since 1.0
 */
public class RecommenderBuildException extends RuntimeException {
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
