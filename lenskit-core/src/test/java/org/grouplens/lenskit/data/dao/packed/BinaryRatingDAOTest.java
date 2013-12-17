package org.grouplens.lenskit.data.dao.packed;

import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryRatingDAOTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testEmptyDAO() throws IOException {
        File file = folder.newFile("ratings.bin");
        BinaryRatingPacker packer = BinaryRatingPacker.open(file);
        packer.close();

        BinaryRatingDAO dao = new BinaryRatingDAO(file);
        assertThat(Cursors.makeList(dao.streamEvents()),
                   hasSize(0));
        assertThat(dao.getUserIds(), hasSize(0));
        assertThat(dao.getItemIds(), hasSize(0));
    }

    @Test
    public void testSimpleDAO() throws IOException {
        File file = folder.newFile("ratings.bin");
        BinaryRatingPacker packer = BinaryRatingPacker.open(file);
        try {
            packer.writeRating(Ratings.make(42, 105, 3.5));
            packer.writeRating(Ratings.make(42, 120, 2.5));
            packer.writeRating(Ratings.make(39, 120, 4.5));
        } finally {
            packer.close();
        }

        BinaryRatingDAO dao = new BinaryRatingDAO(file);
        assertThat(Cursors.makeList(dao.streamEvents()),
                   hasSize(3));
        assertThat(dao.getUserIds(), containsInAnyOrder(42L, 39L));
        assertThat(dao.getItemIds(), containsInAnyOrder(105L, 120L));
        assertThat(dao.getUsersForItem(105), containsInAnyOrder(42L));
        assertThat(dao.getUsersForItem(120), containsInAnyOrder(42L, 39L));
        assertThat(dao.getEventsForUser(39, Rating.class),
                   contains(Ratings.make(39, 120, 4.5)));
        assertThat(dao.getEventsForUser(42, Rating.class),
                   containsInAnyOrder(Ratings.make(42, 120, 2.5),
                                      Ratings.make(42, 105, 3.5)));
        assertThat(dao.getEventsForItem(105, Rating.class),
                   contains(Ratings.make(42, 105, 3.5)));
        assertThat(dao.getEventsForItem(120, Rating.class),
                   containsInAnyOrder(Ratings.make(39, 120, 4.5),
                                      Ratings.make(42, 120, 2.5)));
        assertThat(dao.getEventsForItem(42), nullValue());
        assertThat(dao.getEventsForUser(105), nullValue());
    }
}
