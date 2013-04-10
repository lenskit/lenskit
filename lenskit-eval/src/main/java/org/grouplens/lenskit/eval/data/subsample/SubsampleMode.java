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

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.table.writer.TableWriter;

/**
 * The mode of the subsample file. It can be chosen as Ratings, Items or Users so that
 * the data source file can be subsampled according to Rating, Item or User. 
 *
 * @author Lingfei He<Lingfei@cs.umn.edu>
 */

public enum SubsampleMode {
    RATING {
        @Override
        public void doSample(DataAccessObject dao, TableWriter output, double fraction) throws IOException {
            List<Rating> ratings = Cursors.makeList(dao.getEvents(Rating.class));
            final int n = ratings.size();
            final int m = (int)(fraction * n);
            for (int i = 0; i < m; i++) {
                int j = random.nextInt(n-1-i) + i;
                final Rating rating = ratings.get(j);
                ratings.set(j, ratings.get(i));        
                writeRating(output, rating);     
            }
        }
    },
    ITEM {
        @Override
        public void doSample(DataAccessObject dao, TableWriter output, double fraction) throws IOException {
            LongArrayList itemList = Cursors.makeList(dao.getItems());
            LongLists.shuffle(itemList, random);
            final int n = itemList.size();
            final int m = (int)(fraction * n);
            LongIterator iter = itemList.subList(0, m).iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                Cursor<Rating> cursor = dao.getItemEvents(item, Rating.class);
                try {
                    for (Rating rating: cursor) {
                        writeRating(output, rating);     
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    },
    USER {
        @Override
        public void doSample(DataAccessObject dao, TableWriter output, double fraction) throws IOException {
            LongArrayList userList = Cursors.makeList(dao.getUsers());
            LongLists.shuffle(userList, random);
            final int n = userList.size();
            final int m = (int)(fraction * n);
            LongIterator iter = userList.subList(0, m).iterator();
            while (iter.hasNext()) {
                final long user = iter.nextLong();
                Cursor<Rating> cursor = dao.getUserEvents(user, Rating.class);
                try {
                    for (Rating rating: cursor) {
                        writeRating(output, rating);     
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    };
    private static final Random random = new Random();
    
    /**
     * Write a random subset of all objects chosen by mode to the output file.
     *
     * @param dao The DAO of the data source file
     * @param output The table output to output the rating
     * @param fraction The fraction of data to keep.
     * @throws org.grouplens.lenskit.eval.CommandException
     *          Any error
     */
    public abstract void doSample(DataAccessObject dao, TableWriter output, double fraction) throws IOException;

    /**
     * Writing a rating event to the file using table output
     *
     * @param output The table output to output the rating
     * @param rating The rating event to output
     * @throws IOException The output IO error
     */
    private static void writeRating(TableWriter output, Rating rating) throws IOException {
        String[] row = new String[4];
        row[0] = Long.toString(rating.getUserId());
        row[1] = Long.toString(rating.getItemId());
        Preference pref = rating.getPreference();
        row[2] = pref != null ? Double.toString(pref.getValue()) : "NaN";
        row[3] = Long.toString(rating.getTimestamp());
        output.writeRow(row);
    }
}
