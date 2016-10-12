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
package org.lenskit.data.dao.file;

import org.junit.Test;
import org.lenskit.data.entities.*;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class DelimitedColumnEntityFormatTest {
    @Test
    public void testDefaults() {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();

        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getEntityBuilder(), equalTo((Class) BasicEntityBuilder.class));
    }

    @Test
    public void testBasicParseLine() {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(",");

        EntityType pcType = EntityType.forName("pop_count");
        format.setEntityType(pcType);
        format.addColumn(CommonAttributes.ITEM_ID);
        format.addColumn(CommonAttributes.COUNT);

        LineEntityParser parser = format.makeParser(Collections.<String>emptyList());
        assertThat(parser, notNullValue());

        Entity pc = parser.parse("42,10");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(1L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(42L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(10));

        // make sure the ID (row count) advances
        pc = parser.parse("78,2");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(2L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(78L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(2));
    }

    @Test
    public void testHeaderParseLine() {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(",");
        format.setHeader(true);
        assertThat(format.usesHeader(), equalTo(true));
        assertThat(format.getHeaderLines(), equalTo(1));

        EntityType pcType = EntityType.forName("pop_count");
        format.setEntityType(pcType);

        format.addColumn("song", CommonAttributes.ITEM_ID);
        format.addColumn("plays", CommonAttributes.COUNT);

        LineEntityParser parser = format.makeParser(Collections.singletonList("song,plays"));
        assertThat(parser, notNullValue());

        Entity pc = parser.parse("42,10");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(1L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(42L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(10));

        // make sure the ID (row count) advances
        pc = parser.parse("78,2");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(2L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(78L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(2));
    }

    @Test
    public void testFileWithIds() {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(",");

        EntityType pcType = EntityType.forName("pop_count");
        format.setEntityType(pcType);

        format.addColumn(CommonAttributes.ENTITY_ID);
        format.addColumn(CommonAttributes.ITEM_ID);
        format.addColumn(CommonAttributes.COUNT);

        LineEntityParser parser = format.makeParser(Collections.<String>emptyList());
        assertThat(parser, notNullValue());

        Entity pc = parser.parse("1001,42,10");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(1001L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(42L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(10));

        // make sure the ID (row count) advances
        pc = parser.parse("1010,78,2");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(1010L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(78L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(2));
    }
}
