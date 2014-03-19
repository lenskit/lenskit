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

package org.grouplens.lenskit.eval.data.subsample;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.table.writer.TableWriter;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * The mode of the subsample file. It can be chosen as Ratings, Items or Users so that
 * the data source file can be subsampled according to Rating, Item or User. 
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public enum SubsampleMode {
    RATING {
        @Override
        public void doSample(DataSource source, TableWriter output, double fraction, Random rng) throws IOException {
            List<Rating> ratings = Cursors.makeList(source.getEventDAO().streamEvents(Rating.class));
            final int n = ratings.size();
            final int m = (int)(fraction * n);
            for (int i = 0; i < m; i++) {
                int j = rng.nextInt(n-1-i) + i;
                final Rating rating = ratings.get(j);
                ratings.set(j, ratings.get(i));        
                writeRating(output, rating);     
            }
        }
    },
    ITEM {
        @Override
        public void doSample(DataSource source, TableWriter output, double fraction, Random rng) throws IOException {
            ItemDAO idao = source.getItemDAO();
            ItemEventDAO edao = source.getItemEventDAO();
            LongArrayList itemList = new LongArrayList(idao.getItemIds());
            LongLists.shuffle(itemList, rng);
            final int n = itemList.size();
            final int m = (int)(fraction * n);
            LongIterator iter = itemList.subList(0, m).iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                List<Rating> events = edao.getEventsForItem(item, Rating.class);
                for (Rating rating: CollectionUtils.fast(events)) {
                    writeRating(output, rating);
                }
            }
        }
    },
    USER {
        @Override
        public void doSample(DataSource source, TableWriter output, double fraction, Random rng) throws IOException {
            UserDAO udao = source.getUserDAO();
            UserEventDAO edao = source.getUserEventDAO();
            LongArrayList userList = new LongArrayList(udao.getUserIds());
            LongLists.shuffle(userList, rng);
            final int n = userList.size();
            final int m = (int)(fraction * n);
            LongIterator iter = userList.subList(0, m).iterator();
            while (iter.hasNext()) {
                final long user = iter.nextLong();
                List<Rating> events = edao.getEventsForUser(user, Rating.class);
                for (Rating rating: CollectionUtils.fast(events)) {
                    writeRating(output, rating);
                }
            }
        }
    };
    
    /**
     * Write a random subset of all objects chosen by mode to the output file.
     *
     *
     * @param source The DAO of the data source file
     * @param output The table output to output the rating
     * @param fraction The fraction of data to keep.
     * @throws IOException if there is an error sampling the data set.
     */
    public abstract void doSample(DataSource source, TableWriter output, double fraction, Random rng) throws IOException;

    /**
     * Writing a rating event to the file using table output
     *
     * @param output The table output to output the rating
     * @param rating The rating event to output
     * @throws IOException The output IO error
     */
    private static void writeRating(TableWriter output, Rating rating) throws IOException {
        Preference pref = rating.getPreference();
        Object[] row = {
                Long.toString(rating.getUserId()),
                Long.toString(rating.getItemId()),
                pref != null ? Double.toString(pref.getValue()) : "NaN",
                Long.toString(rating.getTimestamp())
        };
        output.writeRow(row);
    }
}
