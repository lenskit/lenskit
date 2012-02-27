package org.grouplens.lenskit.eval.data;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.config.BuilderFactory;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Build a CSV data source.
 * @author Michael Ekstrand
 */
public class CSVDataSourceBuilder implements Builder<CSVDataSource> {
    String delimiter = "\t";
    String sourceName;
    File inputFile;
    boolean cache = true;
    PreferenceDomain domain;

    public CSVDataSourceBuilder() {}

    public CSVDataSourceBuilder(String name) {
        if (name != null) {
            setName(name);
        }
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     * @param name The name of the data source.
     * @see #setFile(File)
     */
    public CSVDataSourceBuilder setName(String name) {
        sourceName = name;
        return this;
    }

    /**
     * Set the input file. If unspecified, the name (see {@link #setName(String)}) is used
     * as the file name.
     * @param file The file to read ratings from.
     */
    public CSVDataSourceBuilder setFile(File file) {
        inputFile = file;
        return this;
    }

    /**
     * Set the input field delimiter. The default is the tab character.
     * @param delim The input delimiter.
     */
    public CSVDataSourceBuilder setDelimiter(String delim) {
        delimiter = delim;
        return this;
    }

    /**
     * Specify whether to cache ratings in memory. Caching is enabled by default.
     * @param on {@code false} to disable caching.
     */
    public CSVDataSourceBuilder setCache(boolean on) {
        cache = on;
        return this;
    }

    public CSVDataSourceBuilder setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    /**
     * Build the data source. At least one of {@link #setName(String)} or
     * {@link #setFile(File)} must be called prior to building.
     * @return The configured data source.
     */
    @Override
    public CSVDataSource build() {
        // if no name, use the file name
        if (sourceName == null && inputFile != null) {
            sourceName = inputFile.toString();
        }
        // if no file, use the name
        if (inputFile == null && sourceName != null) {
            inputFile = new File(sourceName);
        }
        // by now we should have a file
        Preconditions.checkState(inputFile != null, "no input file specified");
        return new CSVDataSource(sourceName, inputFile, delimiter, cache, domain);
    }

    /**
     * Factory for building CSV data sources. It is registered under the name “csvfile”.
     */
    @ServiceProvider
    public static class Factory implements BuilderFactory<CSVDataSource> {
        @Override public String getName() {
            return "csvfile";
        }
        @Nonnull
        @Override public CSVDataSourceBuilder newBuilder(String arg) {
            CSVDataSourceBuilder bld = new CSVDataSourceBuilder();
            if (arg != null) {
                bld.setName(arg);
            }
            return bld;
        }
    }
}
