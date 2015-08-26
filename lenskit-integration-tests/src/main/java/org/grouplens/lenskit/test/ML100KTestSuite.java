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
package org.grouplens.lenskit.test;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.ItemListItemDAO;
import org.grouplens.lenskit.data.dao.PrefetchingItemDAO;
import org.grouplens.lenskit.data.text.DelimitedColumnEventFormat;
import org.grouplens.lenskit.data.text.Fields;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import static org.junit.Assume.assumeTrue;

/**
 * Base class for integration tests using the ML-100K data set.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ML100KTestSuite {
    protected final String ML100K_PROPERTY = "lenskit.movielens.100k";
    protected final String INPUT_FILE_NAME = "u.data";
    protected static final int SUBSET_DROP_SIZE = 20;

    protected File inputFile;
    protected EventDAO ratingDAO;
    protected EventDAO implicitDAO;

    protected LenskitConfiguration getDaoConfig() {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(ratingDAO);
        return config;
    }

    protected LenskitConfiguration getItemSubsetConfig() {
        LenskitConfiguration config = getDaoConfig();
        config.bind(ItemDAO.class).toProvider(SubsetProvider.class);
        return config;
    }

    @Before
    public void createDAO() throws FileNotFoundException {
        String skip = System.getProperty("lenskit.integration.skip");
        if (skip != null && !skip.equalsIgnoreCase("true")) {
            throw new AssumptionViolatedException("Integration test skip requested");
        }
        final String dataProp = System.getProperty(ML100K_PROPERTY);
        final File dataDir = dataProp != null ? new File(dataProp) : new File("data/ml-100k");
        inputFile = new File(dataDir, INPUT_FILE_NAME);
        if (dataProp == null) {
            assumeTrue("MovieLens should be available. To correct this, unpack the" +
                       " MovieLens 100K data set into data/ml-100k, or set the" +
                       " lenskit.movielens.100k property to point to the location" +
                       " of an unpacked copy of the data set.  For more details, see" +
                       " http://lenskit.org/ML100K", inputFile.exists());
        } else if (!inputFile.exists()) {
            // if the property is set, fail fatally if it doesn't work
            throw new FileNotFoundException("ML data set at " + inputFile + ". " +
                                            "See <http://lenskit.org/ML100K>.");
        }
        ratingDAO = TextEventDAO.create(inputFile, Formats.ml100kFormat());
        DelimitedColumnEventFormat format = DelimitedColumnEventFormat.create("like");
        format.setDelimiter("\t")
              .setFields(Fields.user(), Fields.item(), Fields.ignored(), Fields.timestamp());
        implicitDAO = TextEventDAO.create(inputFile, format);
    }

    public static class SubsetProvider implements Provider<ItemDAO> {
        private final Random rng;
        private final ItemDAO baseDAO;

        @Inject
        public SubsetProvider(Random rng, PrefetchingItemDAO dao) {
            this.rng = rng;
            baseDAO = dao;
        }

        @Override
        public ItemDAO get() {
            LongList items = new LongArrayList(baseDAO.getItemIds());
            LongLists.shuffle(items, rng);
            items = items.subList(0, items.size() - SUBSET_DROP_SIZE);
            return new ItemListItemDAO(items);
        }
    }
}
