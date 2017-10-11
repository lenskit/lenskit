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
package org.lenskit;

import org.lenskit.api.RecommenderBuildException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Error thrown when an error occurs resolving the recommender configuration graph.
 *
 * @since 1.0
 */
public class RecommenderConfigurationException extends RecommenderBuildException {
    private static final long serialVersionUID = 1L;
    private final List<String> hints = new ArrayList<>();

    public RecommenderConfigurationException() {
    }

    public RecommenderConfigurationException(String message) {
        super(message);
    }

    public RecommenderConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecommenderConfigurationException(Throwable cause) {
        super(cause);
    }

    public void addHint(String hint, Object... args) {
        hints.add(String.format(hint, args));
    }

    public List<String> getHints() {
        return Collections.unmodifiableList(hints);
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder(super.toString());

        for (String hint: hints) {
            msg.append("\nHINT: ").append(hint);
        }

        return msg.toString();
    }
}
