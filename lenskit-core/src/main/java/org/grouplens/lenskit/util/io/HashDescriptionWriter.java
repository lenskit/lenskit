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
