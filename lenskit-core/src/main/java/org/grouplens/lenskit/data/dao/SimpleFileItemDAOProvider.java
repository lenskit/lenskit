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
package org.grouplens.lenskit.data.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;

/**
 * Provider for {@link org.grouplens.lenskit.data.dao.ItemListItemDAO} that reads a list of item IDs from a file, one per line.
 *
 * @since 2.1
 */
public class SimpleFileItemDAOProvider implements Provider<ItemListItemDAO> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileItemDAOProvider.class);
    private final File itemFile;

    @Inject
    public SimpleFileItemDAOProvider(@ItemFile File file) {
        itemFile = file;
    }

    @Override
    public ItemListItemDAO get() {
        try {
            return ItemListItemDAO.fromFile(itemFile);
        } catch (IOException e) {
            throw new DataAccessException("error reading " + itemFile, e);
        }
    }
}
