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

/**
 * Base class for integration tests using the ML-100K data set.
 * @author Michael Ekstrand
 */
public class ML100KTestSuite {
    protected final String ML100K_PROPERTY = "lenskit.movielens.100k";
    protected final String INPUT_FILE_NAME = "u.data";

    protected DAOFactory daoFactory;

    @Before
    public void createDAOFactory() {
        String dataDirName = System.getProperty(ML100K_PROPERTY);
        if (null == dataDirName) dataDirName = "data/ml-100k";
        File dataDir = new File(dataDirName);
        File inputFile = new File(dataDir, INPUT_FILE_NAME);
        if (! inputFile.exists()) {  
                throw new IllegalStateException("must either put an unzipped copy of the MovieLens dataset in the "
                        + dataDirName + " directory\n "
                        + " or configure " + ML100K_PROPERTY
                        + " to point to an unzipped version of the MovieLens dataset");
        }
        daoFactory = SimpleFileRatingDAO.Factory.caching(inputFile, "\t");
    }
}
