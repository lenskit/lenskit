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

import com.google.common.io.BaseEncoding;
import org.apache.commons.text.StringEscapeUtils;

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
