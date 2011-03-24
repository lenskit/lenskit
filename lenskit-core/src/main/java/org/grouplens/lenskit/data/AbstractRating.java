package org.grouplens.lenskit.data;

/**
 * Abstract rating implementation.  This just provides the {@link #equals(Object)}
 * and {@link #hashCode()} methods so classes don't have to duplicate the code.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRating implements Rating {

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rating) {
            Rating or = (Rating) o;
            return getUserId() == or.getUserId()
                && getItemId() == or.getItemId()
                && getRating() == or.getRating()
                && getTimestamp() == or.getTimestamp();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getUserId()).hashCode()
            ^ Long.valueOf(getItemId()).hashCode()
            ^ Double.valueOf(getRating()).hashCode()
            ^ Long.valueOf(getTimestamp()).hashCode();
    }

}