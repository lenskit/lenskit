/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.external;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.util.io.LoggingStreamSlurper;
import org.lenskit.api.ItemScorer;
import org.lenskit.basic.PrecomputedItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Build a {@link PrecomputedItemScorer} using an external process.
 * This class implements {@link javax.inject.Provider}, but is not itself instantiable with
 * dependency injection.
 *
 * <p>
 * The external process can receive string arguments, as well as arguments derived from rating, user,
 * and item DAOs.  For DAOs, the contents of the DAO will be written to a file and that file will
 * be provided on the command line.
 * <p>
 * The external process is expected to produce its scores on standard output in comma-separated
 * user, item, score format.
 * <p>
 * <strong>Warning:</strong> if you use this code to build item scorers in the evaluator, be careful
 * with the file-based caching (<tt>componentCacheDirectory</tt>).  The cache will likely not rerun
 * the external process.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExternalProcessItemScorerBuilder implements Provider<ItemScorer> {
    private static final Logger logger = LoggerFactory.getLogger(ExternalProcessItemScorerBuilder.class);
    private static final String CHARSET_UTF_8 = "UTF-8";
    private File workingDir = new File(".");
    private String executable;
    private List<Supplier<String>> arguments = Lists.newArrayList();

    /**
     * Set the working directory to use.
     * @param dir The working directory.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder setWorkingDir(File dir) {
        workingDir = dir;
        return this;
    }

    /**
     * Set the working directory to use.
     * @param dir The working directory.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder setWorkingDir(String dir) {
        return setWorkingDir(new File(dir));
    }

    /**
     * Set the executable to use.
     * @param exe The name or path of the executable to run.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder setExecutable(String exe) {
        executable = exe;
        return this;
    }

    /**
     * Add a command line argument.
     * @param arg The argument to add.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addArgument(String arg) {
        arguments.add(Suppliers.ofInstance(arg));
        return this;
    }

    /**
     * Add some command line arguments.
     * @param args The arguments to add.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addArguments(Collection<String> args) {
        for (String arg: args) {
            addArgument(arg);
        }
        return this;
    }

    /**
     * Add some command line arguments.
     * @param args The arguments to add.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addArguments(String... args) {
        return addArguments(Arrays.asList(args));
    }

    /**
     * Add a list of ratings as a command-line argument.  The ratings will be provided as a CSV file
     * in the format (user, item, rating, timestamp).
     *
     * @param dao A DAO of ratings to provide to the process.  Only ratings will be considered.
     * @param name A file name (optional).  If provided, the ratings will be stored in this file
     *             name in the working directory; otherwise, a fresh name will be generated.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addRatingFileArgument(DataAccessObject dao, @Nullable String name) {
        arguments.add(new RatingFileSupplier(dao, name));
        return this;
    }

    /**
     * Add a list of ratings as a command-line argument.  The ratings will be provided as a CSV file
     * in the format (user, item, rating, timestamp).
     *
     * @param dao A DAO of ratings to provide to the process.  Only ratings will be considered.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addRatingFileArgument(DataAccessObject dao) {
        return addRatingFileArgument(dao, null);
    }

    /**
     * Add a list of items as a command-line argument.  The items will be provided as a text file
     * with one item per line.
     *
     * @param items A DAO of items to provide to the process.
     * @param name A file name (optional).  If provided, the items will be stored in this file
     *             name in the working directory; otherwise, a fresh name will be generated.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addItemFileArgument(LongSet items, @Nullable String name) {
        arguments.add(new IDFileSupplier(name, items));
        return this;
    }

    /**
     * Add a list of items as a command-line argument.  The items will be provided as a CSV file
     * in the format (user, item, item, timestamp).
     *
     * @param items A DAO of items to provide to the process.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addItemFileArgument(LongSet items) {
        return addItemFileArgument(items, String.format("items-%s.csv", UUID.randomUUID()));
    }

    /**
     * Add a list of users as a command-line argument.  The users will be provided as a text file
     * with one user per line.
     *
     * @param users A DAO of users to provide to the process.
     * @param name A file name (optional).  If provided, the users will be stored in this file
     *             name in the working directory; otherwise, a fresh name will be generated.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addUserFileArgument(LongSet users, @Nullable String name) {
        arguments.add(new IDFileSupplier(name, users));
        return this;
    }

    /**
     * Add a list of users as a command-line argument.  The users will be provided as a text file
     * with one user per line.
     *
     * @param users A DAO of users to provide to the process.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addUserFileArgument(LongSet users) {
        return addUserFileArgument(users, String.format("users-%s.csv", UUID.randomUUID()));
    }

    /**
     * Build the item scorer.
     * @return An item scorer that will return the scores provided by the external algorithm.
     */
    public PrecomputedItemScorer build() {
        Preconditions.checkState(executable != null, "no executable specified");
        List<String> command = Lists.newArrayList();
        command.add(executable);
        for (Supplier<String> arg: arguments) {
            command.add(arg.get());
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command).directory(workingDir);

        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            logger.error("could not start {}: {}", executable, e);
            throw new ExternalProcessException("could not start external process", e);
        }
        Thread slurp = new LoggingStreamSlurper("build-" + executable, proc.getErrorStream(),
                                                logger, "");
        slurp.start();

        PrecomputedItemScorer scorer;
        try (InputStreamReader rdr = new InputStreamReader(proc.getInputStream(), Charsets.UTF_8);
             BufferedReader buf = new BufferedReader(rdr)) {
            scorer = PrecomputedItemScorer.fromCSV(buf);
        } catch (IOException e) {
            throw new ExternalProcessException("cannot open output", e);
        }

        int ec;
        try {
            ec = proc.waitFor();
        } catch (InterruptedException e) {
            proc.destroy();
            throw new ExternalProcessException("external process interrupted", e);
        }
        if (ec == 0) {
            return scorer;
        } else {
            logger.error("{} exited with code {}", executable, ec);
            throw new ExternalProcessException("external process failed with code " + ec);
        }
    }

    @Override
    public ItemScorer get() {
        return build();
    }

    private class RatingFileSupplier implements Supplier<String> {
    private final DataAccessObject dao;
        private final String fileName;

        public RatingFileSupplier(DataAccessObject dao, @Nullable String name) {
            this.dao = dao;
            fileName = name;
        }

        @Override
        public String get() {
            String name = fileName;
            if (name == null) {
                name = "ratings-" + UUID.randomUUID().toString() + ".csv";
            }
            logger.info("writing ratings to {}", name);
            File file = new File(workingDir, name);
            try (PrintWriter writer = new PrintWriter(file, CHARSET_UTF_8);
                 ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
                for (Rating r: ratings) {
                    writer.printf("%d,%d,", r.getUserId(), r.getItemId());
                    writer.print(r.getValue());
                    writer.print(",");
                    long ts = r.getTimestamp();
                    if (ts >= 0) {
                        writer.print(ts);
                    }
                    writer.println();
                }
            } catch (IOException e) {
                throw new ExternalProcessException("Error creating ratings file", e);
            }
            return name;
        }
    }

    private class IDFileSupplier implements Supplier<String> {
        private final String fileName;
        private final LongSet identifiers;

        IDFileSupplier(String name, LongSet ids) {
            fileName = name;
            identifiers = ids;
        }

        @Override
        public String get() {
            String name = fileName;
            logger.info("writing {}", name);
            File file = new File(workingDir, name);
            try (PrintWriter writer = new PrintWriter(file, CHARSET_UTF_8)) {
                LongIterator iter = identifiers.iterator();
                while (iter.hasNext()) {
                    writer.println(iter.nextLong());
                }
            } catch (IOException e) {
                throw new ExternalProcessException("Error creating ratings file", e);
            }
            return name;
        }
    }
}
