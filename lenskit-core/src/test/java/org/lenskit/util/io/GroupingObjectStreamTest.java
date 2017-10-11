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
package org.lenskit.util.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GroupingObjectStreamTest {
    public static class FirstLetterObjectStream extends GroupingObjectStream<List<String>, String> {
        private boolean inGroup;
        private char firstChar;
        private List<String> strings;

        public FirstLetterObjectStream(ObjectStream<String> base) {
            super(base);
        }

        public FirstLetterObjectStream(Iterable<String> vals) {
            this(ObjectStreams.wrap(vals.iterator()));
        }

        @Override
        protected boolean handleItem(@Nonnull String item) {
            if (!inGroup) {
                assert strings == null;
                strings = Lists.newArrayList(item);
                firstChar = item.charAt(0);
                inGroup = true;
                return true;
            } else if (firstChar == item.charAt(0)) {
                strings.add(item);
                return true;
            } else {
                return false;
            }

        }

        @Nonnull
        @Override
        protected List<String> finishGroup() {
            List<String> ret = strings;
            inGroup = false;
            strings = null;
            return ret;
        }

        @Override
        protected void clearGroup() {
            strings = null;
            inGroup = false;
        }

        public Character getFirstChar() {
            return firstChar;
        }

        public void setFirstChar(Character firstChar) {
            this.firstChar = firstChar;
        }

        public List<String> getStrings() {
            return strings;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }
    }

    @Test
    public void testEmptyStream() {
        FirstLetterObjectStream cur = new FirstLetterObjectStream(new ArrayList<String>());
        assertThat(cur.readObject(), nullValue());
    }

    @Test
    public void testSingleton() {
        FirstLetterObjectStream cur = new FirstLetterObjectStream(Arrays.asList("foo"));
        assertThat(cur.readObject(),
                   equalTo(Arrays.asList("foo")));
        assertThat(cur.readObject(), nullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSameLetter() {
        List<String> items = Lists.newArrayList("foo", "frob", "fizzle");
        FirstLetterObjectStream cur = new FirstLetterObjectStream(items);
        assertThat(cur, Matchers.<List<String>>contains(ImmutableList.copyOf(items)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSeveralGroups() {
        FirstLetterObjectStream cur = new FirstLetterObjectStream(Arrays.asList("foo", "frob", "bar", "wombat", "woozle"));
        assertThat(cur, contains(Arrays.asList("foo", "frob"),
                                 Arrays.asList("bar"),
                                 Arrays.asList("wombat", "woozle")));
    }
}
