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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.lenskit.data.entities.EntityType;

import java.util.List;

/**
 * Entity format interface.  This is used for line-based text files.
 */
public interface EntityFormat {
    /**
     * Get the type of entities returned by this format.
     * @return The type of entities returned by this format.
     */
    EntityType getEntityType();

    /**
     * Get the number of header lines this format uses.
     * @return The number of header lines to read at the beginning of the file.
     */
    int getHeaderLines();

    /**
     * Create an entity parser for a file.
     * @param header The header lines.
     * @return A parser that will make entity lines from entities.
     */
    LineEntityParser makeParser(List<String> header);

    /**
     * Create a JSON description of this entity format.
     * @return The JSON description.
     */
    ObjectNode toJSON();
}
