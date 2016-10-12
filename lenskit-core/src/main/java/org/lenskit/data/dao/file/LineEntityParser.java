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
package org.lenskit.data.dao.file;

import com.google.common.base.Function;
import org.lenskit.data.entities.Entity;

import javax.annotation.Nullable;

/**
 * Interface for parsers that parse an entity from a line of text.  A fresh line parser
 * must be created for each pass through a file, as it may be stateful (e.g. tracking
 * line numbers).
 */
public abstract class LineEntityParser implements Function<String,Entity> {
    /**
     * Parse an entity from a line of text.
     * @param line The entity to parse.
     * @return The line of text.
     */
    public abstract Entity parse(String line);

    @Nullable
    @Override
    public Entity apply(@Nullable String input) {
        return parse(input);
    }
}
