/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.external;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.PrecomputedItemScorer;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.DelimitedTextCursor;
import org.grouplens.lenskit.util.io.LoggingStreamSlurper;
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
 * Build a {@link org.grouplens.lenskit.basic.PrecomputedItemScorer} using an external process.
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
     * @param ratings A DAO of ratings to provide to the process.  Only ratings will be considered.
     * @param name A file name (optional).  If provided, the ratings will be stored in this file
     *             name in the working directory; otherwise, a fresh name will be generated.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addRatingFileArgument(EventDAO ratings, @Nullable String name) {
        arguments.add(new RatingFileSupplier(ratings, name));
        return this;
    }

    /**
     * Add a list of ratings as a command-line argument.  The ratings will be provided as a CSV file
     * in the format (user, item, rating, timestamp).
     *
     * @param ratings A DAO of ratings to provide to the process.  Only ratings will be considered.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addRatingFileArgument(EventDAO ratings) {
        return addRatingFileArgument(ratings, null);
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
    public ExternalProcessItemScorerBuilder addItemFileArgument(ItemDAO items, @Nullable String name) {
        arguments.add(new ItemFileSupplier(items, name));
        return this;
    }

    /**
     * Add a list of items as a command-line argument.  The items will be provided as a CSV file
     * in the format (user, item, item, timestamp).
     *
     * @param items A DAO of items to provide to the process.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addItemFileArgument(ItemDAO items) {
        return addItemFileArgument(items, null);
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
    public ExternalProcessItemScorerBuilder addUserFileArgument(UserDAO users, @Nullable String name) {
        arguments.add(new UserFileSupplier(users, name));
        return this;
    }

    /**
     * Add a list of users as a command-line argument.  The users will be provided as a CSV file
     * in the format (user, user, user, timestamp).
     *
     * @param users A DAO of users to provide to the process.
     * @return The builder (for chaining).
     */
    public ExternalProcessItemScorerBuilder addUserFileArgument(UserDAO users) {
        return addUserFileArgument(users, null);
    }

    /**
     * Build the item scorer.
     * @return An item scorer that will return the scores provided by the external algorithm.
     */
    public ItemScorer build() {
        Preconditions.checkState(executable != null, "no executable specified");
        List<String> command = Lists.newArrayList();
        command.add(executable);
        for (Supplier<String> arg: arguments) {
            command.add(arg.get());
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command).directory(workingDir);

        PrecomputedItemScorer.Builder builder = PrecomputedItemScorer.newBuilder();

        Process proc = null;
        try {
            proc = pb.start();
        } catch (IOException e) {
            logger.error("could not start {}: {}", executable, e);
            throw new RuntimeException("could not start external process", e);
        }
        Thread slurp = new LoggingStreamSlurper("build-" + executable, proc.getErrorStream(),
                                                logger, "");
        slurp.start();

        Cursor<String[]> rows = new DelimitedTextCursor(new BufferedReader(new InputStreamReader(proc.getInputStream())), ",");
        try {
            for (String[] row: rows) {
                // FIXME Add error checking
                long user = Long.parseLong(row[0]);
                long item = Long.parseLong(row[1]);
                double score = Double.parseDouble(row[2]);
                builder.addScore(user, item, score);
            }
        } finally {
            rows.close();
        }

        int ec;
        try {
            ec = proc.waitFor();
        } catch (InterruptedException e) {
            proc.destroy();
            throw new RuntimeException("external process interrupted", e);
        }
        if (ec == 0) {
            return builder.build();
        } else {
            logger.error("{} exited with code {}", executable, ec);
            throw new RuntimeException("external process failed with code " + ec);
        }
    }

    @Override
    public ItemScorer get() {
        return build();
    }

    private class RatingFileSupplier implements Supplier<String> {
        private final EventDAO eventDAO;
        private final String fileName;

        public RatingFileSupplier(EventDAO dao, @Nullable String name) {
            eventDAO = dao;
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
            try {
                Closer closer = Closer.create();
                try {
                    PrintWriter writer = closer.register(new PrintWriter(file));
                    Cursor<Rating> ratings = closer.register(eventDAO.streamEvents(Rating.class));
                    for (Rating r: ratings) {
                        writer.printf("%d,%d,", r.getUserId(), r.getItemId());
                        Preference p = r.getPreference();
                        if (p != null) {
                            writer.print(p.getValue());
                        }
                        writer.print(",");
                        long ts = r.getTimestamp();
                        if (ts >= 0) {
                            writer.print(ts);
                        }
                        writer.println();
                    }
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error creating ratings file", e);
            }
            return name;
        }
    }

    private abstract class IDFileSupplier implements Supplier<String> {
        private final String fileName;
        private final String prefix;

        IDFileSupplier(String name, String pfx) {
            fileName = name;
            prefix = pfx;
        }

        protected abstract LongSet getIds();

        @Override
        public String get() {
            String name = fileName;
            if (name == null) {
                name = prefix + "-" + UUID.randomUUID().toString() + ".csv";
            }
            logger.info("writing {} to {}", prefix, name);
            File file = new File(workingDir, name);
            try {
                Closer closer = Closer.create();
                try {
                    PrintWriter writer = closer.register(new PrintWriter(file));
                    LongSet ids = getIds();
                    LongIterator iter = ids.iterator();
                    while (iter.hasNext()) {
                        writer.println(iter.nextLong());
                    }
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error creating ratings file", e);
            }
            return name;
        }
    }

    private class ItemFileSupplier extends IDFileSupplier {
        private final ItemDAO itemDAO;

        public ItemFileSupplier(ItemDAO dao, @Nullable String name) {
            super(name, "items");
            itemDAO = dao;
        }

        @Override
        protected LongSet getIds() {
            return itemDAO.getItemIds();
        }
    }

    private class UserFileSupplier extends IDFileSupplier {
        private final UserDAO userDAO;

        public UserFileSupplier(UserDAO dao, @Nullable String name) {
            super(name, "users");
            userDAO = dao;
        }

        @Override
        protected LongSet getIds() {
            return userDAO.getUserIds();
        }
    }
}
