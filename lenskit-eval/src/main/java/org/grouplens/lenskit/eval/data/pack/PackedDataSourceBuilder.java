package org.grouplens.lenskit.eval.data.pack;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackedDataSourceBuilder implements Builder<PackedDataSource> {
    private File file;
    private PreferenceDomain domain;
    private String name;

    public PackedDataSourceBuilder() {}

    public PackedDataSourceBuilder(String name) {
        this.name = name;
    }

    public PackedDataSourceBuilder(File f) {
        file = f;
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     *
     * @param n The name of the data source.
     * @see #setFile(File)
     */
    public PackedDataSourceBuilder setName(String n) {
        name = n;
        return this;
    }

    @Nonnull
    public String getName() {
        if (name != null) {
            return name;
        } else {
            return file.getName();
        }
    }

    /**
     * Get the input file.
     * @return The input file for the builder.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the input file. If unspecified, the name (see {@link #setName(String)}) is used
     * as the file name.
     *
     * @param file The file to read ratings from.
     */
    public PackedDataSourceBuilder setFile(File f) {
        file = f;
        return this;
    }

    /**
     * Set the input file by name.
     *
     * @param fn The input file name.
     * @return The builder (for chaining).
     */
    public PackedDataSourceBuilder setFile(String fn) {
        return setFile(new File(fn));
    }

    public PreferenceDomain getDomain() {
        return domain;
    }

    /**
     * Set the preference domain for the data source.
     *
     *
     * @param dom The preference domain.
     * @return The command (for chaining).
     */
    public PackedDataSourceBuilder setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    @Override
    public PackedDataSource build() {
        return new PackedDataSource(getName(), file, getDomain());
    }
}
