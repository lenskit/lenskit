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