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
package org.lenskit.eval.traintest.metrics;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class DiscountsTest {
    @Test
    public void testParseLog() {
        Discount discount = Discounts.parse("log");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(2.0));
    }

    @Test
    public void testParseLog2() {
        Discount discount = Discounts.parse("log2");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(2.0));
    }

    @Test
    public void testParseLogBase() {
        Discount discount = Discounts.parse("log(10)");
        assertThat(discount, instanceOf(LogDiscount.class));
        assertThat(((LogDiscount) discount).getLogBase(),
                   equalTo(10.0));
    }

    @Test
    public void testParseExp() {
        Discount discount = Discounts.parse("exp(5)");
        assertThat(discount, instanceOf(ExponentialDiscount.class));
        assertThat(((ExponentialDiscount) discount).getHalfLife(),
                   equalTo(5.0));
    }
}