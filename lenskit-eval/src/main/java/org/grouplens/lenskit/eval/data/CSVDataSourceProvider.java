/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import com.google.common.base.Supplier;
import org.codehaus.plexus.util.DirectoryScanner;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.util.dtree.DataNode;
import org.grouplens.lenskit.util.dtree.Trees;
import org.grouplens.lenskit.eval.ConfigUtils;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.util.spi.ConfigAlias;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data source provider for CSV files.
 * 
 * <p>
 * This data source reads data from CSV files. Each entry is expected to be
 * User, Item, Rating [, Timestamp]. The following child elements can be used to
 * configure a CSV file provider:
 * 
 * <dl>
 * <dt>delimiter
 * <dd>The delimiter in the input file (defaults to tab)
 * <dt>url
 * <dd>The URL of a data source. The <var>name</var> attribute is supported to
 * provide a name for the data source.
 * <dt>file
 * <dd>The file name of a data source. A <var>dir</var> attribute is supported
 * to provide a parent directory.
 * <dt>fileset
 * <dd>A fileset specifying some data sources. A <var>dir</var> attribute is
 * supported to provide a base directory.
 * <dt>cache
 * <dd>If <tt>true</tt> (the default), a soft reference is used to cache rating
 * data in-memory to avoid unnecessary reloads.
 * </dl>
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@ConfigAlias("csvfile")
@MetaInfServices
public class CSVDataSourceProvider implements DataSourceProvider {
    private final Logger logger = LoggerFactory.getLogger(CSVDataSourceProvider.class);

    @Override
    public Collection<DataSource> configure(DataNode config)
            throws EvaluatorConfigurationException {
        String delimiter = Trees.childValue(config, "delimiter", "\t", false);
        boolean cache = Trees.childValueBool(config, "cache", true);
        
        String domSpec = Trees.childValue(config, "domain");
        PreferenceDomain domain = null;
        if (domSpec != null) {
            domain = PreferenceDomain.fromString(domSpec);
        }
        List<DataSource> sources = new ArrayList<DataSource>();
        for (DataNode child: config.getChildren()) {
            String n = child.getName();
            if (n.equals("url")) {
                sources.add(configureURLSource(delimiter, child));
            } else if (n.equals("file")) {
                sources.add(configureFileSource(delimiter, child, cache, domain));
            } else if (n.equals("fileset")) {
                sources.addAll(configureGlobSource(delimiter, child, cache, domain));
            }
        }
        
        return sources;
    }
    
    CSVSource configureURLSource(String delim, DataNode config) throws EvaluatorConfigurationException {
        URL url;
        try {
            url = new URL(config.getValue());
        } catch (MalformedURLException e) {
            throw new EvaluatorConfigurationException(e);
        }
        String name = config.getAttribute("name");
        if (name == null) {
            name = url.toString();
        }
        // TODO Implement URL sources
        throw new UnsupportedOperationException();
        //return new CSVSource(name, new SimpleFileRatingDAO.Factory(url, delim));
    }
    
    CSVSource configureFileSource(String delim, DataNode config, boolean cache, PreferenceDomain domain) throws EvaluatorConfigurationException {
        String fn = config.getValue();
        String bdir = config.getAttribute("dir");
        File file;
        if (bdir == null) {
            file = new File(fn);
        } else {
            file = new File(new File(bdir), fn);
        }
        String name = config.getAttribute("displayName");
        if (name == null) {
            name = file.getName();
        }
        return new CSVSource(name, file, delim, cache, domain);
    }
    
    List<CSVSource> configureGlobSource(String delim, DataNode config, boolean cache, PreferenceDomain domain) throws EvaluatorConfigurationException {
        DirectoryScanner ds = ConfigUtils.configureScanner(config);
        File base = ds.getBasedir();
        ds.scan();
        
        List<CSVSource> sources = new ArrayList<CSVSource>();
        for (String fn: ds.getIncludedFiles()) {
            File file = new File(base, fn);
            sources.add(new CSVSource(fn, file, delim, cache, domain));
        }
        
        return sources;
    }
    
    static class CSVSource implements DataSource {
        private final String name;
        private final DAOFactory factory;
        private final File sourceFile;
        private final PreferenceDomain domain;
        
        public CSVSource(String name, File file, String delim, boolean cache, PreferenceDomain pdom) {
            this.name = name;
            sourceFile = file;
            domain = pdom;
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

}
