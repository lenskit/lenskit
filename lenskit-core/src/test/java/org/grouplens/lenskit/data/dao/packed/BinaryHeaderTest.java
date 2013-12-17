package org.grouplens.lenskit.data.dao.packed;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class BinaryHeaderTest {
    @Test
    public void testBasicHeader() {
        BinaryFormat format = BinaryFormat.create();
        BinaryHeader header = BinaryHeader.create(format, 100, 42, 12);
        assertThat(header.getFormat(), sameInstance(format));
        assertThat(header.getRatingCount(), equalTo(100));
        assertThat(header.getUserCount(), equalTo(42));
        assertThat(header.getItemCount(), equalTo(12));
    }

    @Test
    public void testWriteReadHeader() {
        BinaryFormat format = BinaryFormat.create();
        BinaryHeader header = BinaryHeader.create(format, 100, 42, 12);
        ByteBuffer buf = ByteBuffer.allocate(BinaryHeader.HEADER_SIZE);
        header.render(buf);
        buf.flip();
        BinaryHeader h2 = BinaryHeader.fromHeader(buf);
        assertThat(h2.getFormat(), equalTo(format));
        assertThat(h2.getRatingCount(), equalTo(header.getRatingCount()));
        assertThat(h2.getUserCount(), equalTo(header.getUserCount()));
        assertThat(h2.getItemCount(), equalTo(header.getItemCount()));
    }

    @Test
    public void testWriteReadHeaderWithTimestamps() {
        BinaryFormat format = BinaryFormat.create(BinaryFormatFlag.TIMESTAMPS);
        BinaryHeader header = BinaryHeader.create(format, 100, 42, 12);
        ByteBuffer buf = ByteBuffer.allocate(BinaryHeader.HEADER_SIZE);
        header.render(buf);
        buf.flip();
        BinaryHeader h2 = BinaryHeader.fromHeader(buf);
        assertThat(h2.getFormat(), equalTo(format));
        assertThat(h2.getFormat().getFlags(),
                   hasItem(BinaryFormatFlag.TIMESTAMPS));
        assertThat(h2.getRatingCount(), equalTo(header.getRatingCount()));
        assertThat(h2.getUserCount(), equalTo(header.getUserCount()));
        assertThat(h2.getItemCount(), equalTo(header.getItemCount()));
    }
}
