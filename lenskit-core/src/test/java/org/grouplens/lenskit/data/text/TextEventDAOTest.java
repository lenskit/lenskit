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
package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.cursors.Cursors;
import org.lenskit.data.ratings.Rating;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TextEventDAOTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File createBasicCSV() throws IOException {
        File file = folder.newFile("ratings.csv");
        PrintWriter pw = new PrintWriter(file);
        try {
            pw.println("1,10,3.5");
            pw.println("1,4,2.5");
            pw.println("2,42,5");
        } finally {
            pw.close();
        }
        return file;
    }

    @Test
    public void testBasicCSV() throws IOException {
        File file = createBasicCSV();
        TextEventDAO dao = TextEventDAO.create(file, Formats.csvRatings());
        List<Rating> ratings = Cursors.makeList(dao.streamEvents(Rating.class));
        assertThat(ratings, containsInAnyOrder(Rating.create(1, 10, 3.5),
                                               Rating.create(1, 4, 2.5),
                                               Rating.create(2, 42, 5)));
    }

    private File createHeaderCSV() throws IOException {
        File file = folder.newFile("ratings.csv");
        PrintWriter pw = new PrintWriter(file);
        try {
            pw.println("user,item,rating");
            pw.println("1,10,3.5");
            pw.println("1,4,2.5");
            pw.println("2,42,5");
        } finally {
            pw.close();
        }
        return file;
    }

    @Test
    public void testHeaderCSV() throws IOException {
        File file = createHeaderCSV();
        TextEventDAO dao = TextEventDAO.create(file, Formats.csvRatings().setHeaderLines(1));
        List<Rating> ratings = Cursors.makeList(dao.streamEvents(Rating.class));
        assertThat(ratings, containsInAnyOrder(Rating.create(1, 10, 3.5),
                                               Rating.create(1, 4, 2.5),
                                               Rating.create(2, 42, 5)));
    }
}
