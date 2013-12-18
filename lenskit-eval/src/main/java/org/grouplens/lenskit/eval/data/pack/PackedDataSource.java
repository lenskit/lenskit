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
package org.grouplens.lenskit.eval.data.pack;

import com.google.common.base.Supplier;
import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.MoreSuppliers;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackedDataSource implements DataSource {
    private final String name;
    private final File file;
    private final PreferenceDomain domain;
    private final Supplier<BinaryRatingDAO> packedDao;

    public PackedDataSource(String name, File file, PreferenceDomain dom) {
        this.name = name;
        this.file = file;
        domain = dom;
        packedDao = MoreSuppliers.weakMemoize(new DAOSupplier(file));
    }

    @Override
    public String getName() {
        return name;
    }

    public File getPackedFile() {
        return file;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public EventDAO getEventDAO() {
        return packedDao.get();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public UserEventDAO getUserEventDAO() {
        return packedDao.get();
    }

    @Override
    public ItemEventDAO getItemEventDAO() {
        return packedDao.get();
    }

    @Override
    public ItemDAO getItemDAO() {
        return packedDao.get();
    }

    @Override
    public UserDAO getUserDAO() {
        return packedDao.get();
    }

    @Override
    public LenskitConfiguration getConfiguration() {
        LenskitConfiguration config = new LenskitConfiguration();
        Provider<BinaryRatingDAO> provider = Providers.fromSupplier(packedDao, BinaryRatingDAO.class);
        config.bind(BinaryRatingDAO.class).toProvider(provider);
        return config;
    }

    private static class DAOSupplier implements Supplier<BinaryRatingDAO> {
        private final File packedFile;

        public DAOSupplier(File file) {
            packedFile = file;
        }

        @Override
        public BinaryRatingDAO get() {
            try {
                return new BinaryRatingDAO(packedFile);
            } catch (IOException ex) {
                throw new RuntimeException("error opening " + packedFile, ex);
            }
        }
    }
}
