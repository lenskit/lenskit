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

import it.unimi.dsi.fastutil.longs.LongLists;
import org.grouplens.lenskit.collections.LongUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class UserListUserDAOTest {
    public void testEmptyDAO() {
        UserDAO dao = new UserListUserDAO(LongLists.EMPTY_LIST);
        assertThat(dao.getUserIds(), hasSize(0));
    }

    public void testUsersDAO() {
        UserDAO dao = new UserListUserDAO(LongUtils.packedSet(1, 2, 3, 42));
        assertThat(dao.getUserIds(), hasSize(4));
        assertThat(dao.getUserIds(), contains(1L, 2L, 3L, 42L));
    }

    public void testReadUserFile() throws IOException {
        File file = File.createTempFile("users", ".lst");
        try {
            PrintWriter writer = new PrintWriter(file);
            try {
                writer.println(1);
                writer.println(3);
                writer.println();
                writer.println(4);
            } finally {
                writer.close();
            }
            UserDAO dao = UserListUserDAO.fromFile(file);
            assertThat(dao.getUserIds(), hasSize(3));
            assertThat(dao.getUserIds(), contains(1L, 2L, 3L));
        } finally {
            file.delete();
        }
    }
}
