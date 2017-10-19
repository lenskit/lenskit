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

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

/**
 * Description writer that computes a hash of the description on the fly.  Instances of this class
 * can be created with the methods in {@link Descriptions}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class HashDescriptionWriter extends AbstractDescriptionWriter {
    private final Hasher hasher;

    HashDescriptionWriter(Hasher hash) {
        hasher = hash;
    }

    @Override
    public DescriptionWriter putField(String name, String value) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8)
              .putInt(value.length())
              .putString(value, Charsets.UTF_8);
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, long value) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8)
              .putLong(value);
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, double value) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8)
              .putDouble(value);
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, byte[] value) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8)
              .putBytes(value);
        return this;
    }

    @Override
    public <T> DescriptionWriter putField(String name, T value, Describer<? super T> describer) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8);
        describer.describe(value, this);
        return this;
    }

    @Override
    public <T> DescriptionWriter putList(String name, Iterable<T> objects, Describer<? super T> describer) {
        hasher.putInt(name.length())
              .putString(name, Charsets.UTF_8);
        for (T obj: objects) {
            describer.describe(obj, this);
        }
        return this;
    }

    /**
     * Finish the description, producing a hash code.
     * @return The hash code.
     */
    public HashCode finish() {
        return hasher.hash();
    }
}
