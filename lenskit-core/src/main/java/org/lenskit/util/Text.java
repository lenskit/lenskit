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
package org.lenskit.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Utf8;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Arrays;

/**
 * UTF-8 encoded text string.
 */
public final class Text implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] data;

    /**
     * Create a new Text instance from bytes.
     * @param bytes The bytes (must be valid UTF-8).  The array of bytes is copied.
     */
    public Text(byte[] bytes) {
        Preconditions.checkArgument(Utf8.isWellFormed(bytes), "input bytes must be UTF-8");
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Create a new Text instance from a string.
     * @param string The string.
     */
    @FromString
    public static Text fromString(String string) {
        return new Text(Normalizer.normalize(string, Normalizer.Form.NFC)
                                  .getBytes(Charsets.UTF_8));
    }

    public int size() {
        return data.length;
    }

    @Override
    @ToString
    public String toString() {
        return new String(data, Charsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Text text = (Text) o;

        return Arrays.equals(data, text.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
