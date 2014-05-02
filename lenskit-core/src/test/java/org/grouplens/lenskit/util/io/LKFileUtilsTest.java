package org.grouplens.lenskit.util.io;

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
}
