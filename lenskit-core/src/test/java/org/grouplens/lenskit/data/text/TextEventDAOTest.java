package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
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
        assertThat(ratings, containsInAnyOrder(Ratings.make(1, 10, 3.5),
                                               Ratings.make(1, 4, 2.5),
                                               Ratings.make(2, 42, 5)));
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
        assertThat(ratings, containsInAnyOrder(Ratings.make(1, 10, 3.5),
                                               Ratings.make(1, 4, 2.5),
                                               Ratings.make(2, 42, 5)));
    }
}
