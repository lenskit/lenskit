/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
