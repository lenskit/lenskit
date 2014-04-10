/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.io;

/**
 * Accumulate a description of an object (or objects).  Object descriptions should reflect the
 * complete identity of an object - enough to uniquely distinguish it from non-equivalent objects -
 * but are not (usually) a full serialization.  They are more verbose than {@link Object#toString()}, and
 * are used for things like generating deterministic keys for naming cache files.
 * <p>
 * Descriptions are composed of fields with values.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public interface DescriptionWriter {
    /**
     * Put a field into the description.
     * @param name The field name. It cannot begin with an underscore.
     * @param value The field value.
     */
    DescriptionWriter putField(String name, String value);

    /**
     * Put an integer field.
     * @param name The field name.
     * @param value The field value.
     * @see #putField(String, String)
     */
    DescriptionWriter putField(String name, long value);

    /**
     * Put a floating-point field.
     * @param name The field name.
     * @param value The field value.
     * @see #putField(String, String)
     */
    DescriptionWriter putField(String name, double value);

    /**
     * Put a byte array field.
     * @param name The field name.
     * @param value The field value.
     * @see #putField(String, String)
     */
    DescriptionWriter putField(String name, byte[] value);

    /**
     * Put an object field into the description.
     * @param name The field name. It cannot begin with an underscore.
     * @param value The field value.  It is described with {@link org.grouplens.lenskit.util.io.Descriptions#defaultDescriber()}}.
     */
    DescriptionWriter putField(String name, Object value);

    /**
     * Put a field with a list of values, using the default describer.
     * @param name The field name.
     * @param objects The list of objects.
     * @return The description writer (for chaining).
     */
    DescriptionWriter putList(String name, Iterable<?> objects);

    /**
     * Put a field with a list of values.
     * @param name The field name.
     * @param objects The list of objects.
     * @param describer A describer for the objects.
     * @param <T> The type of objects in the list.
     * @return The description writer (for chaining).
     */
    <T> DescriptionWriter putList(String name, Iterable<T> objects,
                                  Describer<? super T> describer);

    /**
     * Put an object field into the description.
     * @param name The field name. It cannot begin with an underscore.
     * @param value The field value.  If it implements {@link org.grouplens.lenskit.util.io.Describable}, then it is asked to write
     *              its description; otherwise, some default behavior is used.
     * @param describer A describer to describe the value.
     */
    <T> DescriptionWriter putField(String name, T value, Describer<? super T> describer);
}
