package org.grouplens.lenskit.eval.data;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.config.BuilderFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Provider;
import java.util.List;

/**
 * @author Michael Ekstrand
 */
public class CSVDataSource implements DataSource {
    final String name;
    final DAOFactory factory;
    final File sourceFile;
    final PreferenceDomain domain;
    final String delimiter;

    CSVDataSource(String name, File file, String delim, boolean cache, PreferenceDomain pdom) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        delimiter = delim;
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final DAOFactory csvFactory = new SimpleFileRatingDAO.Factory(url, delim);
        if (cache) {
            factory = new EventCollectionDAO.SoftFactory(new Supplier<List<Rating>>() {
                @Override
                public List<Rating> get() {
                    DataAccessObject dao = csvFactory.create();
                    try {
                        return Cursors.makeList(dao.getEvents(Rating.class));
                    } finally {
                        dao.close();
                    }
                }
            });
        } else {
            factory = csvFactory;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public File getFile() {
        return sourceFile;
    }

    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public long lastUpdated(PreparationContext context) {
        return sourceFile.exists() ? sourceFile.lastModified() : -1L;
    }

    @Override
    public void prepare(PreparationContext context) {
        /* no-op */
    }

    @Override
    public DAOFactory getDAOFactory() {
        return factory;
    }
}
