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
package org.lenskit.util.describe;

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
     * @param value The field value.  It is described with {@link Descriptions#defaultDescriber()}}.
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
     * @param value The field value.  If it implements {@link Describable}, then it is asked to write
     *              its description; otherwise, some default behavior is used.
     * @param describer A describer to describe the value.
     */
    <T> DescriptionWriter putField(String name, T value, Describer<? super T> describer);
}
