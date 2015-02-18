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

import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.text.EventFormat;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.grouplens.lenskit.util.io.CompressionMode;

import java.io.File;

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

    TextDataSource(String name, File file, EventFormat fmt, PreferenceDomain pdom) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        format = fmt;

        dao = TextEventDAO.create(file, format, CompressionMode.AUTO);
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
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("TextData(")
           .append(getName())
           .append(")");
        return str.toString();
    }
}
