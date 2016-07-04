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

import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.data.text.CSVFileItemNameDAOProvider;
import org.grouplens.lenskit.data.text.EventFormat;
import org.grouplens.lenskit.data.text.SimpleFileItemDAOProvider;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.data.dao.*;
import org.lenskit.data.dao.file.DelimitedColumnEntityFormat;
import org.lenskit.data.dao.file.EntityFormat;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.dao.file.TextEntitySource;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.TypedName;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.specs.data.DataSourceSpec;
import org.lenskit.specs.data.TextDataSourceSpec;

import javax.inject.Provider;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Data source backed by a CSV file.  Use {@link CSVDataSourceBuilder} to configure and build one
 * of these, or the <code>csvfile</code> command in an eval script.
 *
 * @since 2.2
 * @see CSVDataSourceBuilder
 */
public class TextDataSource extends AbstractDataSource {
    private final String name;
    private final StaticDataSource source;
    private final EventDAO legacyDAO;
    private final File sourceFile;
    private final PreferenceDomain domain;
    private final EntityFormat format;

    private final Provider<ItemListItemDAO> items;
    private final Provider<MapItemNameDAO> itemNames;
    private final Path itemFile;
    private final Path itemNameFile;

    public TextDataSource(String name, StaticDataSource provider) {
        this.name = name;
        source = provider;
        legacyDAO = null;

        sourceFile = null;
        // FIXME Support preference domains
        domain = null;

        format = null;
        items = null;
        itemNames= null;
        itemFile = null;
        itemNameFile = null;
    }

    TextDataSource(String name, File file, EntityFormat fmt, EventFormat efmt, PreferenceDomain pdom,
                   Path itemFile, Path itemNameFile) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        format = fmt;
        source = new StaticDataSource();

        TextEntitySource source = new TextEntitySource(name);
        source.setFile(file.toPath());
        source.setFormat(format);
        this.source.addSource(source);

        legacyDAO = TextEventDAO.create(file, efmt, CompressionMode.AUTO);

        if (itemFile != null) {
            TextEntitySource itemSource = new TextEntitySource();
            DelimitedColumnEntityFormat itemFmt = new DelimitedColumnEntityFormat();
            itemFmt.setEntityType(CommonTypes.ITEM);
            itemFmt.addColumn(CommonAttributes.ENTITY_ID);
            itemSource.setFormat(itemFmt);
            this.source.addSource(itemSource);

            items = Providers.memoize(new SimpleFileItemDAOProvider(itemFile.toFile()));
            this.itemFile = itemFile;
        } else {
            items = null;
            this.itemFile = null;
        }
        if (itemNameFile != null) {
            TextEntitySource itemSource = new TextEntitySource();
            DelimitedColumnEntityFormat itemFmt = new DelimitedColumnEntityFormat();
            itemFmt.setEntityType(CommonTypes.ITEM);
            itemFmt.addColumns(CommonAttributes.ENTITY_ID, CommonAttributes.NAME);
            itemSource.setFormat(itemFmt);
            this.source.addSource(itemSource);

            itemNames = Providers.memoize(new CSVFileItemNameDAOProvider(itemNameFile.toFile()));
            this.itemNameFile = itemNameFile;
        } else {
            itemNames = null;
            this.itemNameFile = null;
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

    public StaticDataSource getDataSource() {
        return source;
    }

    public DataAccessObject getDataAccessObject() {
        return source.get();
    }

    public File getFile() {
        return sourceFile;
    }

    public EntityFormat getFormat() {
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
        return new BridgeEventDAO(source.get());
    }

    @Override
    public ItemDAO getItemDAO() {
        if (items != null) {
            return items.get();
        } else if (itemNames != null) {
            return itemNames.get();
        } else {
            return new BridgeItemDAO(source.get());
        }
    }

    @Override
    public ItemNameDAO getItemNameDAO() {
        if (itemNames != null) {
            return itemNames.get();
        } else {
            return new BridgeItemNameDAO(source.get());
        }
    }

    @Override
    public UserEventDAO getUserEventDAO() {
        return new BridgeUserEventDAO(source.get());
    }

    @Override
    public ItemEventDAO getItemEventDAO() {
        return new BridgeItemEventDAO(source.get());
    }

    @Override
    public UserDAO getUserDAO() {
        return new BridgeUserDAO(source.get());
    }

    @Override
    public void configure(LenskitConfiguration config) {
        // we just use our static file DAO
        config.bind(DataAccessObject.class).toProvider(source);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("TextData(")
           .append(getName())
           .append(")");
        return str.toString();
    }

    @Override
    public DataSourceSpec toSpec() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName(getName());
        spec.setFile(getFile().toPath());
        if (format instanceof DelimitedColumnEntityFormat) {
            DelimitedColumnEntityFormat cf = (DelimitedColumnEntityFormat) format;
            spec.setDelimiter(cf.getDelimiter());
            List<String> fieldNames = new ArrayList<>();
            for (TypedName<?> name: cf.getColumnList()) {
                fieldNames.add(name.getName());
            }
            spec.setFields(fieldNames);
            spec.setBuilderType(cf.getEntityBuilder().getName());
            spec.setItemFile(itemFile);
            spec.setItemNameFile(itemNameFile);
            spec.setHeaderLines(cf.getHeaderLines());
        }
        if (domain != null) {
            spec.setDomain(domain.toSpec());
        }
        return spec;
    }

    /**
     * Build a text data source from a spec.
     * @param spec The spec.
     * @return The data source.
     */
    public static TextDataSource fromSpec(TextDataSourceSpec spec) {
        TextDataSourceBuilder bld = new TextDataSourceBuilder();
        bld.setName(spec.getName())
           .setFile(spec.getFile().toFile())
           .setDomain(PreferenceDomain.fromSpec(spec.getDomain()));
        bld.setDelimiter(spec.getDelimiter());
        // FIXME Support fields
        bld.setHeaderLines(spec.getHeaderLines());
        bld.setItemFile(spec.getItemFile());
        bld.setItemNameFile(spec.getItemNameFile());
        return bld.build();
    }
}
