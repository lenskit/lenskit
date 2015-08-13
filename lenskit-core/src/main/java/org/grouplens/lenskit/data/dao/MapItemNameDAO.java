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
package org.grouplens.lenskit.data.dao;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.util.LineCursor;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An item name DAO backed by a map of item IDs to names.
 *
 * @since 2.2
 * @see org.grouplens.lenskit.data.dao.ItemNameDAO
 */
@Shareable
public class MapItemNameDAO implements ItemNameDAO, ItemDAO, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(MapItemNameDAO.class);
    private static final long serialVersionUID = 1L;
    private final Map<Long,String> itemNameMap;
    private final LongSortedSet itemIds;

    public MapItemNameDAO(Map<Long,String> items) {
        itemNameMap = ImmutableMap.copyOf(items);
        itemIds = LongUtils.packedSet(itemNameMap.keySet());
    }

    @Nullable
    @Override
    public LongSet getItemIds() {
        return itemIds;
    }

    @Nullable
    @Override
    public String getItemName(long item) {
        return itemNameMap.get(item);
    }

    /**
     * Read an item list DAO from a file.
     * @param file A file of item IDs, one per line.
     * @return The item list DAO.
     * @throws java.io.IOException if there is an error reading the list of items.
     */
    public static MapItemNameDAO fromCSVFile(File file) throws IOException {
        LineCursor cursor = LineCursor.openFile(file, CompressionMode.AUTO);
        try {
            ImmutableMap.Builder<Long, String> names = ImmutableMap.builder();
            StrTokenizer tok = StrTokenizer.getCSVInstance();
            for (String line : cursor) {
                tok.reset(line);
                long item = Long.parseLong(tok.next());
                String title = tok.nextToken();
                if (title != null) {
                    names.put(item, title);
                }
            }
            return new MapItemNameDAO(names.build());
        } catch (NoSuchElementException ex) {
            throw new IOException(String.format("%s:%s: not enough columns",
                                                file, cursor.getLineNumber()),
                                  ex);
        } catch (NumberFormatException ex) {
            throw new IOException(String.format("%s:%s: id not an integer",
                                                file, cursor.getLineNumber()),
                                  ex);
        } finally {
            cursor.close();
        }
    }
}
