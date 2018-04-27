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
