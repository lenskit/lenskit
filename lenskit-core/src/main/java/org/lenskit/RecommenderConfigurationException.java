/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
