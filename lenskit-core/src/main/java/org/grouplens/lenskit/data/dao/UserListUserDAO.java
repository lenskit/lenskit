/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.Shareable;

import java.io.*;
import java.util.Collection;

/**
 * A user DAO that stores a precomputed list of users.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class UserListUserDAO implements UserDAO, Serializable {
    private static final long serialVersionUID = 1L;
    private final LongSortedSet userSet;

    public UserListUserDAO(Collection<Long> users) {
        userSet = LongUtils.packedSet(users);
    }

    @Override
    public LongSet getUserIds() {
        return userSet;
    }

    /**
     * Read a user list DAO from a file.
     * @param file A file of user IDs, one per line.
     * @return The user list DAO.
     * @throws java.io.IOException if there is an error reading the list of users.
     */
    public static UserListUserDAO fromFile(File file) throws IOException {
        LongList users = new LongArrayList();
        Closer closer = Closer.create();
        try {
            FileReader fread = closer.register(new FileReader(file));
            BufferedReader buf = closer.register(new BufferedReader(fread));
            String line;
            int lno = 0;
            while ((line = buf.readLine()) != null) {
                lno += 1;
                if (line.trim().isEmpty()) {
                    continue; // skip blank lines
                }
                long item;
                try {
                    item = Long.parseLong(line.trim());
                } catch (IllegalArgumentException ex) {
                    throw new IOException("invalid user ID on " + file + " line " + lno + ": " + line);
                }
                users.add(item);
            }
        } catch (Throwable th) {
            throw closer.rethrow(th);
        } finally {
            closer.close();
        }

        return new UserListUserDAO(users);
    }
}
