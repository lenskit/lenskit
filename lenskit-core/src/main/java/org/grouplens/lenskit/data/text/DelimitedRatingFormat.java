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

import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.util.OptionFlag;

import javax.annotation.Nonnull;

public class DelimitedRatingFormat implements EventFormat {
    @Nonnull
    private String delimiter = "\t";
    @Nonnull
    private OptionFlag hasTimestamp = OptionFlag.MAYBE;

    @Nonnull
    public String getDelimiter() {
        return delimiter;
    }

    public DelimitedRatingFormat setDelimiter(@Nonnull String delim) {
        delimiter = delim;
        return this;
    }

    public OptionFlag getHasTimestamp() {
        return hasTimestamp;
    }

    public DelimitedRatingFormat setHasTimestamp(OptionFlag ts) {
        hasTimestamp = ts;
        return this;
    }

    public DelimitedRatingFormat setHasTimestamp(boolean hasTS) {
        return setHasTimestamp(OptionFlag.fromBoolean(hasTS));
    }

    @Override
    public Class<? extends Event> getEventType() {
        return Rating.class;
    }

    @Override
    public Rating parse(String line) {
        StrTokenizer tok = new StrTokenizer(line, delimiter);
        RatingBuilder rb = Ratings.newBuilder();
        rb.setUserId(Long.parseLong(tok.next()));
        rb.setItemId(Long.parseLong(tok.next()));
        rb.setRating(Double.parseDouble(tok.next()));

        String ts = null;
        switch (hasTimestamp) {
        case YES:
            ts = tok.next();
            break;
        case MAYBE:
            ts = tok.nextToken();
            break;
        }
        if (ts != null) {
            rb.setTimestamp(Long.parseLong(ts));
        }

        return rb.build();
    }

    @Override
    public Object newContext() {
        return null;
    }

    @Override
    public Rating parse(String line, Object context) {
        return parse(line);
    }

    @Override
    public Event copy(Event evt) {
        return evt;
    }
}
