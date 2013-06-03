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
package org.grouplens.lenskit.eval.data;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.PreferenceDomain;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CSVDataSource implements DataSource {
    final String name;
    final DAOFactory factory;
    final File sourceFile;
    final PreferenceDomain domain;
    final String delimiter;

    CSVDataSource(String name, File file, String delim, boolean cache, PreferenceDomain pdom,
                  @Nullable Function<DAOFactory, DAOFactory> wrap) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        delimiter = delim;
        final DAOFactory csvFactory = new SimpleFileRatingDAO.Factory(file, delim);
        DAOFactory daof = csvFactory;
        if (cache) {
            daof = new EventCollectionDAO.SoftFactory(new Supplier<List<Rating>>() {
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
        }
        if (wrap != null) {
            daof = wrap.apply(daof);
        }
        factory = daof;
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
    public long lastModified() {
        return sourceFile.exists() ? sourceFile.lastModified() : -1L;
    }

    @Override
    public DAOFactory getDAOFactory() {
        return factory;
    }
}
