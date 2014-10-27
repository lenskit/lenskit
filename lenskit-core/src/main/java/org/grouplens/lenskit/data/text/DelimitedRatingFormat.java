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

import javax.annotation.Nonnull;
import java.util.List;

public class DelimitedRatingFormat implements EventFormat {
    @Nonnull
    private String delimiter = "\t";
    private FieldList<RatingBuilder> fieldList =
            FieldList.create(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp(false));

    @Nonnull
    public String getDelimiter() {
        return delimiter;
    }

    public DelimitedRatingFormat setDelimiter(@Nonnull String delim) {
        delimiter = delim;
        return this;
    }

    /**
     * Set the fields to be parsed.  The default fields are:
     *
     * <ol>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#user() User ID}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#item() Item ID}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#rating() Rating}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#timestamp(boolean) Optional timestamp}</li>
     * </ol>
     *
     * @param fields The fields to be parsed by this format.
     * @return The format (for chaining).
     */
    public DelimitedRatingFormat setFields(Field<? super RatingBuilder>... fields) {
        fieldList = FieldList.create(fields);
        return this;
    }

    /**
     * Set the fields to be parsed.  The default fields are:
     *
     * <ol>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#user() User ID}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#item() Item ID}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#rating() Rating}</li>
     *     <li>{@linkplain org.grouplens.lenskit.data.text.Fields#timestamp(boolean) Optional timestamp}</li>
     * </ol>
     *
     * @param fields The fields to be parsed by this format.
     * @return The format (for chaining).
     */
    public DelimitedRatingFormat setFields(List<Field<? super RatingBuilder>> fields) {
        fieldList = FieldList.create(fields);
        return this;
    }

    /**
     * Get the field list.
     * @return
     */
    public FieldList<RatingBuilder> getFields() {
        return fieldList;
    }

    @Override
    public Class<? extends Event> getEventType() {
        return Rating.class;
    }

    private Rating parse(StrTokenizer tok, RatingBuilder rb) throws InvalidRowException {
        fieldList.parse(tok, rb);
        return rb.build();
    }

    @Override
    public Rating parse(String line) throws InvalidRowException {
        StrTokenizer tok = new StrTokenizer(line, delimiter);
        RatingBuilder rb = Ratings.newBuilder();
        return parse(tok, rb);
    }

    @Override
    public Object newContext() {
        return new Context();
    }

    @Override
    public Rating parse(String line, Object context) throws InvalidRowException {
        Context ctx = (Context) context;
        ctx.tokenizer.reset(line);
        return parse(ctx.tokenizer, ctx.builder);
    }

    @Override
    public Event copy(Event evt) {
        return evt;
    }

    private static class Context {
        public final StrTokenizer tokenizer = new StrTokenizer();
        public final RatingBuilder builder = new RatingBuilder();
    }
}
