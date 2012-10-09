package org.grouplens.lenskit.util.io;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the {@link UpToDateChecker}.
 * @author Michael Ekstrand
 */
public class UpToDateCheckerTest {
    UpToDateChecker checker;

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
        File f = File.createTempFile("test", "input");
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
