package org.grouplens.lenskit.data.dao.packed;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryFormatTest {
    @Test
    public void testNoFlags() {
        BinaryFormat format = BinaryFormat.create();
        assertThat(format.getFlagWord(), equalTo((short) 0));
        assertThat(format.hasTimestamps(), equalTo(false));
        assertThat(format.getRatingSize(), equalTo(24));
    }

    @Test
    public void testTimestampFlag() {
        BinaryFormat format = BinaryFormat.create(BinaryFormatFlag.TIMESTAMPS);
        assertThat(format.getFlagWord(), equalTo((short) 1));
        assertThat(format.hasTimestamps(), equalTo(true));
        assertThat(format.getRatingSize(), equalTo(32));
    }

    @Test
    public void testEqual() {
        assertThat(BinaryFormat.create(), equalTo(BinaryFormat.create()));
        assertThat(BinaryFormat.create(BinaryFormatFlag.TIMESTAMPS),
                   equalTo(BinaryFormat.create(BinaryFormatFlag.TIMESTAMPS)));
        assertThat(BinaryFormat.create(BinaryFormatFlag.TIMESTAMPS),
                   not(equalTo(BinaryFormat.create())));
    }
}
