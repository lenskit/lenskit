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
package org.lenskit.util.io

import com.google.common.base.Charsets
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Files

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * Tests for the staged write facility.
 */
class StagedWriteTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Test
    public void testSuccessfulWrite() {
        def file = new File(folder.root, "foo.txt")
        assertThat file.exists(), equalTo(false)
        def stage = StagedWrite.begin(file)
        try {
            Files.write(stage.stagingFile,
                        ["hello, world"],
                        Charsets.UTF_8)
            stage.commit()
        } finally {
            stage.close()
        }
        assertThat file.exists(), equalTo(true)
        assertThat file.text.trim(), equalTo("hello, world")
        assertThat folder.root.listFiles().toList()*.name, contains('foo.txt')
    }

    @Test
    public void testAbortedWrite() {
        def file = new File(folder.root, "foo.txt")
        assertThat file.exists(), equalTo(false)
        def stage = StagedWrite.begin(file)
        try {
            Files.write(stage.stagingFile,
                        ["hello, world"],
                        Charsets.UTF_8)
        } finally {
            stage.close()
        }
        assertThat file.exists(), equalTo(false)
        assertThat folder.root.listFiles().toList(), hasSize(0)
    }

    @Test
    public void testOverwrite() {
        def file = new File(folder.root, "foo.txt")
        assertThat file.exists(), equalTo(false)
        file.text = "hello, world"
        def stage = StagedWrite.begin(file)
        try {
            Files.write(stage.stagingFile,
                        ["goodnight, moon"],
                        Charsets.UTF_8)
            stage.commit()
        } finally {
            stage.close()
        }
        assertThat file.exists(), equalTo(true)
        assertThat file.text.trim(), equalTo("goodnight, moon")
        assertThat folder.root.listFiles().toList()*.name, contains('foo.txt')
    }

    @Test
    public void stressTest() {
        // beat on the stager for a while to stress-test it
        def threads = []
        Exception err = null
        for (i in 1..4) {
            threads << Thread.start {
                try {
                    for (j in 1..50) {
                        def uuid = UUID.randomUUID()
                        def file = new File(folder.root, "${uuid}.txt")
                        def stage = StagedWrite.begin(file)
                        try {
                            stage.stagingFile.text = "${uuid}\n"
                            stage.commit()
                        } finally {
                            stage.close()
                        }
                        assertThat file.exists(), equalTo(true)
                        assertThat file.text.trim(), equalTo(uuid.toString())
                    }
                } catch (InterruptedIOException ex) {
                    return
                } catch (Exception ex) {
                    err = ex
                    threads.each { it.interrupt() }
                }
            }
        }
        threads.each { it.join() }
        if (err != null) {
            throw err;
        }
    }
}
