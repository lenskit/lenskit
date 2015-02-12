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
package org.grouplens.lenskit.data.dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MapItemNameDAOTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemNameDAO dao;

    @Before
    public void createFile() throws IOException {
        File f = folder.newFile("titles.csv");
        PrintStream str = new PrintStream(f);
        try {
            str.println("42,Iron Man");
            str.println("67,Star Wars");
        } finally {
            str.close();
        }
        dao = MapItemNameDAO.fromCSVFile(f);
    }

    @Test
    public void testMissingItem() {
        assertThat(dao.getItemName(5), nullValue());
    }

    @Test
    public void testPresentItem() {
        assertThat(dao.getItemName(42), equalTo("Iron Man"));
    }

    @Test
    public void testItemIds() {
        assertThat(dao.getItemIds(), containsInAnyOrder(42L, 67L));
    }
}
