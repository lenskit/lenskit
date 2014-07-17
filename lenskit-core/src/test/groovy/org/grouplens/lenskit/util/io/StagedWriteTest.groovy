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
package org.grouplens.lenskit.util.io

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.*

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
            stage.stagingFile.text = "hello, world"
            stage.commit()
        } finally {
            stage.close()
        }
        assertThat file.exists(), equalTo(true)
        assertThat file.text, equalTo("hello, world")
        assertThat folder.root.listFiles().toList()*.name, contains('foo.txt')
    }

    @Test
    public void testAbortedWrite() {
        def file = new File(folder.root, "foo.txt")
        assertThat file.exists(), equalTo(false)
        def stage = StagedWrite.begin(file)
        try {
            stage.stagingFile.text = "hello, world"
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
            stage.stagingFile.text = "goodnight, moon"
            stage.commit()
        } finally {
            stage.close()
        }
        assertThat file.exists(), equalTo(true)
        assertThat file.text, equalTo("goodnight, moon")
        assertThat folder.root.listFiles().toList()*.name, contains('foo.txt')
    }
}
