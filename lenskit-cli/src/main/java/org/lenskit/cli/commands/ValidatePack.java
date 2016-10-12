/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.lenskit.data.history.ItemEventCollection;
import org.lenskit.data.history.UserHistory;
import org.lenskit.data.packed.BinaryRatingDAO;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AutoService(Command.class)
public class ValidatePack implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ValidatePack.class);

    @Override
    public String getName() {
        return "validate-pack";
    }

    @Override
    public String getHelp() {
        return "Validates a rating pack file.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.addArgument("pack_file")
              .type(File.class)
              .metavar("FILE")
              .nargs("+")
              .required(true)
              .help("check ratings in FILE");
    }

    @Override
    public void execute(Namespace options) throws Exception {
        List<File> files = options.get("pack_file");
        List<File> bad = new ArrayList<>();
        for (File file: files) {
            if (!checkFile(file)) {
                bad.add(file);
            }
        }
        if (!bad.isEmpty()) {
            logger.error("Found {} bad files", bad.size());
            for (File file: bad) {
                logger.info("corrupt file: {}", file);
            }
            throw new IOException("Found errors in " + bad.size() + " files");
        }
    }

    private boolean checkFile(File file) throws IOException {
        logger.info("Checking ratings in pack file {}", file);
        BinaryRatingDAO dao = BinaryRatingDAO.open(file);
        int nerrors = 0;

        /* Count the ratings */
        int count = 0;
        try (ObjectStream<Rating> ratings = dao.streamEvents(Rating.class)) {
            for (Rating r: ratings) {
                count += 1;
            }
        }
        logger.info("Read {} ratings", count);

        LongSet userIds = dao.getUserIds();
        LongSet itemIds = dao.getItemIds();

        /* Check ratings by user */
        int countByUser = 0;
        int nusers = 0;
        try (ObjectStream<UserHistory<Rating>> users = dao.streamEventsByUser(Rating.class)) {
            for (UserHistory<Rating> user: users) {
                if (!userIds.contains(user.getUserId())) {
                    nerrors++;
                    logger.error("User {} not in set of users", user.getUserId());
                }
                for (Rating r: user) {
                    long uid = r.getUserId();
                    if (uid != user.getUserId()) {
                        nerrors++;
                        logger.error("Rating {} has incorrect user ID (expected {})", r, user.getUserId());
                    }
                    if (!itemIds.contains(r.getItemId())) {
                        nerrors++;
                        logger.error("Item {} not in set of items", r.getItemId());
                    }
                    countByUser += 1;
                }
                nusers += 1;
            }
        }
        logger.info("Counted {} ratings by {} users", countByUser, nusers);
        if (countByUser != count) {
            nerrors++;
            logger.error("Iteration by user found {} ratings, expected {}",
                         countByUser, count);
        }
        if (userIds.size() != nusers) {
            nerrors++;
            logger.error("DAO has {} users, found ratings for {}",
                         dao.getUserIds().size(), nusers);
        }

        /* Check ratings by item */
        int countByItem = 0;
        int nitems = 0;
        try (ObjectStream<ItemEventCollection<Rating>> items = dao.streamEventsByItem(Rating.class)) {
            for (ItemEventCollection<Rating> item: items) {
                if (!itemIds.contains(item.getItemId())) {
                    nerrors++;
                    logger.error("item {} not in set of item IDs", item.getItemId());
                }
                for (Rating r: item) {
                    long iid = r.getItemId();
                    if (iid != item.getItemId()) {
                        nerrors++;
                        logger.error("Rating {} has incorrect item ID (expected {})", r, item.getItemId());
                    }
                    if (!userIds.contains(r.getUserId())) {
                        nerrors++;
                        logger.error("user {} not in set of user IDs", r.getUserId());
                    }
                    countByItem += 1;
                }
                nitems += 1;
            }
        }
        logger.info("Counted {} ratings by {} items", countByItem, nitems);
        if (countByItem != count) {
            nerrors++;
            logger.error("Iteration by item found {} ratings, expected {}",
                         countByItem, count);
        }
        if (itemIds.size() != nitems) {
            nerrors++;
            logger.error("DAO has {} items, found ratings for {}",
                         dao.getItemIds().size(), nitems);
        }


        if (nerrors > 0) {
            logger.error("Found {} errors in file {}", nerrors, file);
            return false;
        }
        return true;
    }
}
