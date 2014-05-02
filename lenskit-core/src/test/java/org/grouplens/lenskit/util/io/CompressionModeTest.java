package org.grouplens.lenskit.util.io;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CompressionModeTest {
    @Test
    public void testNoneNone() {
        assertThat(CompressionMode.NONE.getEffectiveCompressionMode("foo.txt"),
                   equalTo(CompressionMode.NONE));
        assertThat(CompressionMode.NONE.getEffectiveCompressionMode("foo.gz"),
                   equalTo(CompressionMode.NONE));
    }

    @Test
    public void testAutoGZ() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.gz"),
                   equalTo(CompressionMode.GZIP));
    }

    @Test
    public void testAutoXZ() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.xz"),
                   equalTo(CompressionMode.XZ));
    }

    @Test
    public void testAutoNone() {
        assertThat(CompressionMode.AUTO.getEffectiveCompressionMode("foo.txt"),
                   equalTo(CompressionMode.NONE));
    }
}
