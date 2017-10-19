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

import com.google.common.base.Throwables;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LineCountMatcher extends BaseMatcher<File> {
    private final Matcher<? extends Integer> lineCount;

    public LineCountMatcher(Matcher<? extends Integer> m) {
        lineCount = m;
    }

    @Override
    public boolean matches(Object o) {
        try {
            return o instanceof File && lineCount.matches(getLineCount((File) o));
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private int getLineCount(File file) throws IOException {
        try (Reader reader = new FileReader(file);
             BufferedReader lines = new BufferedReader(reader)) {
            int n = 0;
            while (lines.readLine() != null) {
                n++;
            }
            return n;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("file with line count ")
                   .appendDescriptionOf(lineCount);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (item instanceof File) {
            try {
                int lines = getLineCount((File) item);
                description.appendText("had " + lines + " lines");
            } catch (FileNotFoundException e) {
                description.appendText("did not exist");
            } catch (IOException e) {
                description.appendText("could not be read");
            }
        } else {
            description.appendText("was non-file object " + item);
        }
    }
}
