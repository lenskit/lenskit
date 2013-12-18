/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
