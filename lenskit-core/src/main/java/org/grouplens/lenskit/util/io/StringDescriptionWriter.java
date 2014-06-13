/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;

/**
 * Description writer that accumulates a string.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StringDescriptionWriter extends AbstractDescriptionWriter {
    private final StringBuilder builder;
    private final int indent;
    private final String indentString;

    StringDescriptionWriter() {
        this(new StringBuilder(), 0);
    }

    private StringDescriptionWriter(StringBuilder bld, int ind) {
        builder = bld;
        indent = ind;
        indentString = makeIndent(indent);
    }

    private String makeIndent(int n) {
        char[] chars = new char[n];
        Arrays.fill(chars, ' ');
        return String.valueOf(chars);
    }

    /**
     * Get the accumulated string description.
     * @return The accumulated string description.
     */
    public String finish() {
        return builder.toString();
    }

    private String getIndent() {
        return indentString;
    }

    @Override
    public DescriptionWriter putField(String name, String value) {
        builder.append(getIndent())
               .append(name)
               .append(": ")
               .append(StringEscapeUtils.escapeJava(value))
               .append('\n');
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, long value) {
        builder.append(getIndent())
               .append(name)
               .append(": ")
               .append(value)
               .append('\n');
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, double value) {
        builder.append(getIndent())
               .append(name)
               .append(": ")
               .append(value)
               .append('\n');
        return this;
    }

    @Override
    public DescriptionWriter putField(String name, byte[] value) {
        builder.append(getIndent())
               .append(name)
               .append(": <")
               .append(BaseEncoding.base16().encode(value))
               .append(">\n");
        return this;
    }

    @Override
    public <T> DescriptionWriter putList(String name, Iterable<T> objects, Describer<? super T> describer) {
        builder.append(getIndent())
               .append(name)
               .append(": [\n");
        int i = 0;
        String mid = makeIndent(indent + 2);
        for (T obj: objects) {
            builder.append(mid)
                   .append(i)
                   .append(": {\n");
            describer.describe(obj, new StringDescriptionWriter(builder, indent + 4));
            builder.append(mid)
                   .append("}\n");
            i++;
        }
        builder.append(getIndent())
               .append("]\n");
        return this;
    }

    @Override
    public <T> DescriptionWriter putField(String name, T value, Describer<? super T> describer) {
        builder.append(getIndent())
               .append(name)
               .append(": {\n");
        describer.describe(value, new StringDescriptionWriter(builder, indent + 2));
        builder.append(getIndent())
               .append("}\n");
        return this;
    }
}
