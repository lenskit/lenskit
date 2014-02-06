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
package org.grouplens.lenskit.cursors

import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class GroupingCursorTest {
    static class FirstLetterCursor extends GroupingCursor<List<String>,String> {
        Character firstChar
        List<String> strings

        FirstLetterCursor(Cursor<String> base) {
            super(base)
        }

        FirstLetterCursor(Iterable<String> vals) {
            this(Cursors.wrap(vals.iterator()))
        }

        @Override
        protected boolean handleItem(String item) {
            if (firstChar == null) {
                assert strings == null
                strings = [item]
                firstChar = item[0]
                return true
            } else if (firstChar == item[0]) {
                strings << item
                return true
            } else {
                return false
            }
        }

        @Override
        protected List<String> finishGroup() {
            List<String> ret = strings
            firstChar = null
            strings = null
            return ret
        }

        @Override
        protected void clearGroup() {
            strings = null
        }
    }

    @Test
    void testEmptyCursor() {
        def cur = new FirstLetterCursor([])
        assertThat(cur.hasNext(), equalTo(false))
    }

    @Test
    void testSingleton() {
        def cur = new FirstLetterCursor(["foo"])
        assertThat(cur.hasNext(), equalTo(true))
        assertThat(cur.next(), equalTo(["foo"]))
        assertThat(cur.hasNext(), equalTo(false))
    }

    @Test
    void testSameLetter() {
        def cur = new FirstLetterCursor(["foo", "frob", "fizzle"])
        assertThat(cur, contains((Object) ["foo", "frob", "fizzle"]))
    }

    @Test
    void testSeveralGroups() {
        def cur = new FirstLetterCursor(["foo", "frob", "bar", "wombat", "woozle"])
        assertThat(cur, contains(["foo", "frob"],
                                 ["bar"],
                                 ["wombat", "woozle"]))
    }
}
