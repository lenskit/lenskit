package org.grouplens.lenskit.util;

import javax.inject.Provider;
import java.util.Random;

/**
 * Provide a {@link Random} instance. Used to allow {@link Random} to be dependency-injected.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RandomProvider implements Provider<Random> {
    @Override
    public Random get() {
        return new Random();
    }
}
