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
package org.grouplens.lenskit.data.text;

import com.google.common.collect.Lists;
import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessException;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.util.LineCursor;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DAO that reads events from a text file, one per line.  Events are formatted according to an
 * {@link EventFormat}.
 *
 * @since 2.2
 */
@ThreadSafe
public class TextEventDAO implements EventDAO, Describable {
    private final File inputFile;
    private final CompressionMode compression;
    private final EventFormat eventFormat;

    @Inject
    public TextEventDAO(@EventFile File file, EventFormat format) {
        this(file, format, CompressionMode.AUTO);
    }

    private TextEventDAO(@EventFile File file, EventFormat format, CompressionMode comp) {
        inputFile = file;
        compression = comp;
        eventFormat = format;
    }

    /**
     * Open a rating DAO with the default layout and automatic compression.
     * @param file The file.
     * @param delim The delimiter.
     * @return A DAO that parses ratings from {@code file} using {@link DelimitedColumnEventFormat}.
     * @see #ratings(File, String, org.grouplens.lenskit.util.io.CompressionMode)
     */
    public static TextEventDAO ratings(File file, String delim) {
        return ratings(file, delim, CompressionMode.AUTO);
    }

    /**
     * Open a potentially-compressed file of ratings.  It uses the default layout:
     *
     * <ol>
     *     <li>User</li>
     *     <li>Item</li>
     *     <li>Rating</li>
     *     <li>Optional timestamp</li>
     * </ol>
     *
     * @param file The file to open.
     * @param delim The delimiter.
     * @param mode The compression mode.
     * @return A text event DAO.
     */
    public static TextEventDAO ratings(File file, String delim, CompressionMode mode) {
        EventFormat fmt = DelimitedColumnEventFormat.create(new RatingEventType())
                                                    .setDelimiter(delim);
        return new TextEventDAO(file, fmt, mode);
    }

    public static TextEventDAO create(File inputFile, EventFormat format) {
        return create(inputFile, format, CompressionMode.AUTO);
    }

    public static TextEventDAO create(File inputFile, EventFormat format, CompressionMode comp) {
        return new TextEventDAO(inputFile, format, comp);
    }

    @Override
    public Cursor<Event> streamEvents() {
        try {
            LineCursor lines = LineCursor.openFile(inputFile, compression);
            Cursors.consume(eventFormat.getHeaderLines(), lines);
            return new EventCursor(lines);
        } catch (IOException e) {
            throw new DataAccessException("cannot open " + inputFile, e);
        }
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return Cursors.filter(streamEvents(), type);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        Comparator<Event> evt = order.getEventComparator();
        if (evt == null) {
            return streamEvents(type);
        }

        Cursor<E> cursor = streamEvents(type);
        try {
            List<E> events = Lists.newArrayList(cursor);
            Collections.sort(events, evt);
            return Cursors.wrap(events);
        } finally {
            cursor.close();
        }
    }

    @Override
    public void describeTo(DescriptionWriter descr) {
        descr.putField("file", inputFile.getAbsolutePath())
             .putField("length", inputFile.length())
             .putField("mtime", inputFile.lastModified());
    }

    private final class EventCursor extends AbstractCursor<Event> {
        private final LineCursor lines;
        private Object context;

        EventCursor(LineCursor lc) {
            lines = lc;
            context = eventFormat.newContext();
        }

        @Override
        public boolean hasNext() {
            return lines.hasNext();
        }

        @Nonnull
        @Override
        public Event next() {
            try {
                return eventFormat.parse(lines.next(), context);
            } catch (InvalidRowException e) {
                throw new DataAccessException("malformed input on line " + lines.getLineNumber(), e);
            }
        }

        @Override
        public void close() {
            lines.close();
        }
    }
}
