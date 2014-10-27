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
package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.data.event.EventBuilder;

import java.util.Set;

/**
 * Definition for an event type that can be used to load it from a file.  Implementations of this
 * class should register themselves with the Java {@link java.util.ServiceLoader} framework to be
 * usable from the command line, data specification files, etc.
 *
 * @since 2.2
 */
public interface EventTypeDefinition<B extends EventBuilder> {
    /**
     * A simple name for this event
     * @return The name of this event type (used to refer to it from various UI entry points).
     */
    String getName();

    /**
     * Construct a new builder for this event type.
     * @return The builder.
     */
    B newBuilder();

    /**
     * Get the set of required fields for this event type.
     * @return The fields without which events cannot be built.
     */
    Set<Field<? super B>> getRequiredFields();

    /**
     * Get the default field list for loading this event from text fields.
     * @return The default field list for parsing events of this type.
     */
    FieldList<B> getDefaultFields();
}
