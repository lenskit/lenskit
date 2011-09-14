package org.grouplens.lenskit.util.spi;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.junit.Test;

import com.google.common.collect.Iterators;

/**
 * Make sure that we can use {@link ServiceLoader} on the classpath.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestServiceLoader {

    @Test
    public void test() {
        ServiceLoader<DummyInterface> loader = ServiceLoader.load(DummyInterface.class);
        DummyInterface[] dummies = Iterators.toArray(loader.iterator(), DummyInterface.class);
        assertThat(dummies.length, greaterThanOrEqualTo(1));
        boolean found = false;
        for (DummyInterface impl: dummies) {
            if (impl instanceof DummyImpl) {
                found = true;
                assertEquals("FOOBIE BLETCH", impl.getMessage());
            }
        }
        assertTrue("could not find implementation", found);
    }

}
