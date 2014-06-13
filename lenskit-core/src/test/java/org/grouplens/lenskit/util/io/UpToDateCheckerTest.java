/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.io;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the {@link UpToDateChecker}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UpToDateCheckerTest {
    UpToDateChecker checker;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void newChecker() {
        checker = new UpToDateChecker();
    }

    @Test
    public void testInitialUpToDate() {
        assertThat(checker.isUpToDate(),
                   equalTo(true));
        assertThat(checker.isUpToDate(1000),
                   equalTo(true));
    }

    @Test
    public void testCheckTimestamp() {
        checker.addInput(100);
        assertThat(checker.isUpToDate(50),
                   equalTo(false));
        assertThat(checker.isUpToDate(101),
                   equalTo(true));
    }

    @Test
    public void testTSAccum() {
        checker.addInput(100);
        checker.addInput(200);
        checker.addOutput(500);
        assertThat(checker.isUpToDate(),
                   equalTo(true));
        checker.addOutput(150);
        assertThat(checker.isUpToDate(),
                   equalTo(false));
    }

    @Test
    public void testFileAccum() throws InterruptedException, IOException {
        long time = System.currentTimeMillis();
        File f = folder.newFile("test.input");
        try {
            if (!f.setLastModified(time - 2500)) {
                throw new IOException("could not set last-modified");
            }
            checker.addInput(f);
            checker.addOutput(time);
            assertThat(checker.isUpToDate(),
                       equalTo(true));
            checker.addOutput(time - 3600 * 24);
            assertThat(checker.isUpToDate(),
                       equalTo(false));
        } finally {
            f.delete();
        }
    }
}
