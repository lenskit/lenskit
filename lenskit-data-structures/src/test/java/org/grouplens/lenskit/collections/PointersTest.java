package org.grouplens.lenskit.collections;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PointersTest {
    @Test
    public void testFromToEmpty() {
        IntPointer ptr = Pointers.fromTo(0, 0);
        assertThat(ptr.isAtEnd(), equalTo(true));
        assertThat(ptr.advance(), equalTo(false));
        try {
            ptr.getInt();
            fail("getting from empty pointer should throw exception");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testFromToSingleton() {
        IntPointer ptr = Pointers.fromTo(0, 1);
        assertThat(ptr.isAtEnd(), equalTo(false));
        assertThat(ptr.getInt(), equalTo(0));
        assertThat(ptr.advance(), equalTo(false));
        assertThat(ptr.isAtEnd(), equalTo(true));
        try {
            ptr.getInt();
            fail("getting from consumed pointer should throw exception");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }
}
