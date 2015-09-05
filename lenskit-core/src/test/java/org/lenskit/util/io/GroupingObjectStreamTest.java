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
