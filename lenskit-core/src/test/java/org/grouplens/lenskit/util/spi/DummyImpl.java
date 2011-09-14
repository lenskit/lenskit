package org.grouplens.lenskit.util.spi;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class DummyImpl implements DummyInterface {

    @Override
    public String getMessage() {
        return "FOOBIE BLETCH";
    }

}
