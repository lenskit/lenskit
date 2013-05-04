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
package org.grouplens.lenskit.test;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.junit.Before;

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

    protected DAOFactory daoFactory;

    @Before
    public void createDAOFactory() throws FileNotFoundException {
        final String dataProp = System.getProperty(ML100K_PROPERTY);
        final File dataDir = dataProp != null ? new File(dataProp) : new File("data/ml-100k");
        final File inputFile = new File(dataDir, INPUT_FILE_NAME);
        if (dataProp == null) {
            /* If this assumption fails, put an unzipped copy of the MovieLens data set
             * in the data directory (specified by the lenskit.movielens.100k property, or
             * in the data/ml-100k directory. */
            assumeTrue("ML data set should be available", inputFile.exists());
        } else if (!inputFile.exists()) {
            // if the property is set, fail fatally if it doesn't work
            throw new FileNotFoundException("ML data set at " + inputFile);
        }
        daoFactory = SimpleFileRatingDAO.Factory.caching(inputFile, "\t");
    }
}
