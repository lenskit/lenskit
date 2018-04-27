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
package org.lenskit.data.output;

import com.google.common.collect.Lists;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.table.writer.CSVWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class CSVRatingWriter implements RatingWriter {
    private final CSVWriter tableWriter;
    private boolean includeTimestamps = true;

    public CSVRatingWriter(CSVWriter tw) {
        tableWriter = tw;
    }

    /**
     * Query whether this writer includes timestamps.
     * @return {@code true} if timestamps are written.
     */
    boolean isIncludeTimestamps() {
        return includeTimestamps;
    }

    /**
     * Set whether this writer writes timestamps.
     * @param val Whether or not to write timestamps.
     */
    void setIncludeTimestamps(boolean val) {
        includeTimestamps = val;
    }

    @Override
    public void writeRating(Rating r) throws IOException {
        List<Object> row = Lists.newArrayListWithCapacity(4);
        row.add(r.getUserId());
        row.add(r.getItemId());
        row.add(r.getValue());
        if (includeTimestamps) {
            row.add(r.getTimestamp());
        }
        tableWriter.writeRow(row);
    }

    @Override
    public void close() throws IOException {
        tableWriter.close();
    }
}
