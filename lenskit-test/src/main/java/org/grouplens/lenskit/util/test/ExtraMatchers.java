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
package org.grouplens.lenskit.util.test;

import com.google.common.base.Equivalence;
import org.hamcrest.Matcher;

import java.io.File;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;

/**
 * Entry point for extra matchers used by LensKit tests.
 */
public final class ExtraMatchers {
    private ExtraMatchers() {}

    /**
     * Match {@link Double#NaN}.
     * @return A matcher that accepts {@link Double#NaN}.
     */
    public static Matcher<Double> notANumber() {
        return new NotANumberMatcher();
    }

    public static Matcher<File> existingFile() {
        return new FileExistsMatcher();
    }

    public static Matcher<File> lineCount(int n) {
        return hasLineCount(equalTo(n));
    }

    public static Matcher<File> hasLineCount(Matcher<? extends Integer> m) {
        return new LineCountMatcher(m);
    }

    /**
     * Match a string against a regular expression.
     * @param pattern The regular expression to match.
     * @return A matcher that tests strings against the specified regular expression.
     */
    public static Matcher<CharSequence> matchesPattern(String pattern) {
        return new RegexMatcher(Pattern.compile(pattern));
    }

    /**
     * Test if the object is equivalent to object object.
     * @param obj The expected object.
     * @param equiv An equivalence relation.
     * @param <T> The type of object to compare.
     * @return A matcher that accepts objects equivalent to `obj`.
     */
    public static <T> Matcher<T> equivalentTo(T obj, Equivalence<T> equiv) {
        return new EquivalenceMatcher<>(obj, equiv);
    }
}
