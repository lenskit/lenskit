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
package org.lenskit.data.dao.file;

import org.junit.Test;
import org.lenskit.data.entities.BasicEntityBuilder;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
        assertThat(format.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.COUNT));

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
    public void testParseLineWithBaseId() {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(",");
        format.setBaseId(42);

        EntityType pcType = EntityType.forName("pop_count");
        format.setEntityType(pcType);
        format.addColumn(CommonAttributes.ITEM_ID);
        format.addColumn(CommonAttributes.COUNT);
        assertThat(format.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.COUNT));

        LineEntityParser parser = format.makeParser(Collections.<String>emptyList());
        assertThat(parser, notNullValue());

        Entity pc = parser.parse("42,10");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(43L));
        assertThat(pc.get(CommonAttributes.ITEM_ID), equalTo(42L));
        assertThat(pc.get(CommonAttributes.COUNT), equalTo(10));

        // make sure the ID (row count) advances
        pc = parser.parse("78,2");
        assertThat(pc, notNullValue());
        assertThat(pc.getId(), equalTo(44L));
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
        assertThat(format.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.COUNT));

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

        assertThat(format.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.COUNT));

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
