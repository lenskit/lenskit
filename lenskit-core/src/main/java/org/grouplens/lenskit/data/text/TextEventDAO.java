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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
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
public class TextEventDAO implements EventDAO {
    private final File inputFile;
    private final EventFormat eventFormat;

    @Inject
    public TextEventDAO(@EventFile File file, EventFormat format) {
        inputFile = file;
        eventFormat = format;
    }

    /**
     * Open a rating DAO.
     * @param file The file.
     * @param delim The delimiter.
     * @return A DAO that parses ratings from {@code file} using {@link DelimitedRatingFormat}.
     */
    public static TextEventDAO ratings(File file, String delim) {
        EventFormat fmt = new DelimitedRatingFormat().setDelimiter(delim);
        return new TextEventDAO(file, fmt);
    }

    @Override
    public Cursor<Event> streamEvents() {
        try {
            return new EventCursor(new LineCursor(inputFile));
        } catch (FileNotFoundException e) {
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

    private final class EventCursor extends AbstractCursor<Event> {
        private final Cursor<String> lines;
        private Object context;

        EventCursor(Cursor<String> lc) {
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
            return eventFormat.parse(lines.next());
        }

        @Nonnull
        @Override
        public Event fastNext() {
            String line = lines.next();
            return eventFormat.parse(line, context);
        }

        @Override
        public void close() {
            lines.close();
        }
    }
}
