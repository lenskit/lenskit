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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.lenskit.data.entities.AttributeSet;
import org.lenskit.data.entities.EntityBuilder;
import org.lenskit.data.entities.EntityType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * Get the set of attributes entities from format may have, if known.
     *
     * @return The set of attributes that entities may have, or `null` if that information is not known.
     */
    @Nullable
    AttributeSet getAttributes();

    /**
     * Get the entity builder that this format will use.
     *
     * @return The entity builder used by this format.
     */
    @Nonnull
    Class<? extends EntityBuilder> getEntityBuilder();

    /**
     * Get the number of header lines this format uses.
     * @return The number of header lines to read at the beginning of the file.
     */
    @Nonnegative
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
