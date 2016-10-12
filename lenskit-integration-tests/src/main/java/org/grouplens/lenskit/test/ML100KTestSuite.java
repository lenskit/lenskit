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
package org.grouplens.lenskit.test;

import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;
import org.lenskit.LenskitConfiguration;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.DelimitedColumnEntityFormat;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.dao.file.TextEntitySource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assume.assumeTrue;

/**
 * Base class for integration tests using the ML-100K data set.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ML100KTestSuite {
    protected final String ML100K_PROPERTY = "lenskit.movielens.100k";
    protected final String INPUT_FILE_NAME = "u.data";
    protected final EntityType LIKE = EntityType.forName("like");
    protected static final int SUBSET_DROP_SIZE = 20;

    protected File inputFile;
    protected StaticDataSource source;
    protected StaticDataSource implicitSource;

    protected LenskitConfiguration getDaoConfig() {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(DataAccessObject.class)
              .toProvider(source);
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

        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter("\t");
        format.setEntityType(LIKE);
        format.addColumns(CommonAttributes.USER_ID,
                          CommonAttributes.ITEM_ID);
        TextEntitySource implicit = new TextEntitySource("likes");
        implicit.setFile(inputFile.toPath());
        implicit.setFormat(format);
        implicitSource = new StaticDataSource("implicit");
        implicitSource.addSource(implicit);
        implicitSource.addDerivedEntity(CommonTypes.USER, LIKE, CommonAttributes.USER_ID);
        implicitSource.addDerivedEntity(CommonTypes.ITEM, LIKE, CommonAttributes.ITEM_ID);

        source = new StaticDataSource("ml-100k");
        TextEntitySource tes = new TextEntitySource();
        tes.setFile(inputFile.toPath());
        tes.setFormat(org.lenskit.data.dao.file.Formats.tsvRatings());
        source.addSource(tes);
    }
}
