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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LKFileUtilsTest {
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testUncompressedFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.NONE);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testGzipFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt.gz");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.GZIP);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testXZFile() throws IOException {
        File file = tmpdir.newFile("uncompressed.txt.xz");
        Writer out = LKFileUtils.openOutput(file);
        try {
            out.write("hello\n");
        } finally {
            out.close();
        }

        Reader in = LKFileUtils.openInput(file, CompressionMode.XZ);
        try {
            char[] buffer = new char[6];
            in.read(buffer);
            assertThat(new String(buffer), equalTo("hello\n"));
        } finally {
            out.close();
        }
    }

    @Test
    public void testNoopBasename() {
        assertThat(LKFileUtils.basename("foo", true),
                   equalTo("foo"));
        assertThat(LKFileUtils.basename("foo", false),
                   equalTo("foo"));
    }

    @Test
    public void testNoPathPreservesExtension() {
        assertThat(LKFileUtils.basename("readme.txt", true),
                   equalTo("readme.txt"));
    }

    @Test
    public void testNoPathDropsExtension() {
        assertThat(LKFileUtils.basename("readme.txt", false),
                   equalTo("readme"));
    }

    @Test
    public void testPathRemoved() {
        assertThat(LKFileUtils.basename(String.format("foo%cbar.txt", File.separatorChar), true),
                   equalTo("bar.txt"));
        assertThat(LKFileUtils.basename(String.format("foo%cbar.txt", File.separatorChar), false),
                   equalTo("bar"));
    }

    @Test
    public void testSlashPathRemoved() {
        assertThat(LKFileUtils.basename("foo/bar.txt", true),
                   equalTo("bar.txt"));
        assertThat(LKFileUtils.basename("foo/bar.txt", false),
                   equalTo("bar"));
    }

    @Test
    public void testKeepDotfileName() {
        // dotfiles should not have extensions stripped, because they do have names
        assertThat(LKFileUtils.basename(".dotfile", false),
                   equalTo(".dotfile"));
    }
}
