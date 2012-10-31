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
    protected final File dataDir = new File(System.getProperty("lenskit.ml100k.directory"));
    protected final File inputFile = new File(dataDir, "u.data");

    protected DAOFactory daoFactory;

    @Before
    public void createDAOFactory() {
        daoFactory = SimpleFileRatingDAO.Factory.caching(inputFile, "\t");
    }
}
