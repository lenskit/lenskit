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
package org.lenskit.specs;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Resolve dynamic specifications (pure JSON trees) into objects in an extensible fashion.  The heart of this interface
 * is the {@link #resolveAndBuild(Class, JsonNode)}.  Instances of this interface are loaded via the {@link java.util.ServiceLoader}
 * facility, and queried in turn for one that can turn a particular specification into the requested type.  Handlers
 * should be selective in the types they handle, to minimize the chance of conflicts.
 *
 * @see SpecUtils#buildObject(Class, AbstractSpec)
 */
public interface DynamicSpecHandler {
    /**
     * Build an object from a specification.
     * @param type The type of object desired.
     * @param spec The specification.
     * @param <T> The type of object.
     * @return The object, or {@code null} if this handler cannot build an object.
     */
    <T> T resolveAndBuild(Class<T> type, JsonNode spec);
}
