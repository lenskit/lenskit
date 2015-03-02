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
package org.grouplens.lenskit.data.source;

import com.google.common.collect.ImmutableMap;
import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.text.*;
import org.grouplens.lenskit.data.text.SimpleFileItemDAOProvider;
import org.grouplens.lenskit.util.io.CompressionMode;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.io.File;
import java.util.Map;

/**
 * Data source backed by a CSV file.  Use {@link CSVDataSourceBuilder} to configure and build one
 * of these, or the <code>csvfile</code> command in an eval script.
 *
 * @since 2.2
 * @see CSVDataSourceBuilder
 */
public class TextDataSource extends AbstractDataSource {
    private final String name;
    private final EventDAO dao;
    private final File sourceFile;
    private final PreferenceDomain domain;
    private final EventFormat format;

    private final Provider<ItemListItemDAO> items;
    private final Provider<MapItemNameDAO> itemNames;

    TextDataSource(String name, File file, EventFormat fmt, PreferenceDomain pdom,
                   File itemFile, File itemNameFile) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        format = fmt;

        dao = TextEventDAO.create(file, format, CompressionMode.AUTO);

        if (itemFile != null) {
            items = Providers.memoize(new SimpleFileItemDAOProvider(itemFile));
        } else {
            items = null;
        }
        if (itemNameFile != null) {
            itemNames = Providers.memoize(new CSVFileItemNameDAOProvider(itemNameFile));
        } else {
            itemNames = null;
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            return sourceFile.getName();
        } else {
            return name;
        }
    }

    public File getFile() {
        return sourceFile;
    }

    public EventFormat getFormat() {
        return format;
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
    public EventDAO getEventDAO() {
        return dao;
    }

    @Override
    public ItemDAO getItemDAO() {
        if (items != null) {
            return items.get();
        } else if (itemNames != null) {
            return itemNames.get();
        } else {
            return super.getItemDAO();
        }
    }

    @Override
    public ItemNameDAO getItemNameDAO() {
        if (itemNames != null) {
            return itemNames.get();
        } else {
            return super.getItemNameDAO();
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("TextData(")
           .append(getName())
           .append(")");
        return str.toString();
    }

    @Nonnull
    @Override
    public Map<String, Object> toSpecification() {
        ImmutableMap.Builder<String,Object> bld = ImmutableMap.builder();
        bld.put("type", "text")
           .put("name", name)
           .put("file", sourceFile.getPath());
        // FIXME Handle item name DAO
        if (format instanceof DelimitedColumnEventFormat) {
            DelimitedColumnEventFormat cf = (DelimitedColumnEventFormat) format;
            bld.put("delimiter", cf.getDelimiter());
            // FIXME Serialize columns
            logger.warn("cannot serialize columns, assuming default");
        }
        if (domain != null) {
            bld.put("domain", domain.toSpecification());
        }
        return bld.build();
    }
}
