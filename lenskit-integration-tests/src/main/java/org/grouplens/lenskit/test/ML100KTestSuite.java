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
package org.grouplens.lenskit.test;

import org.junit.AssumptionViolatedException;
import org.junit.Before;
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
