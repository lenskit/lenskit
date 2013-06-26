package org.grouplens.lenskit.collections;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * A pointer over the bits in a bit set.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class BitSetPointer extends AbstractIntPointer {
    private final BitSet bitSet;
    // position is the current bit, or < 0 if past the end
    private int position;
    private final int limit;

    /**
     * Create a bit set pointer starting at the first set bit.
     * @param bits The bit set.
     */
    public BitSetPointer(BitSet bits) {
        this(bits, 0);
    }

    public BitSetPointer(BitSet bits, int start) {
        this(bits, start, bits.size());
    }

    /**
     * Create a new bit set pointer with a specified starting position.  The pointer is at the
     * first set bit at or after the specified starting point, or at the end if there is no such bit.
     * @param bits The bit set to iterate over.
     * @param start The starting index.
     * @param limit The upper limit (exclusive) of the bits to return.
     */
    public BitSetPointer(BitSet bits, int start, int limit) {
        bitSet = bits;
        position = bitSet.nextSetBit(start);
        this.limit = limit;
    }

    @Override
    public int getInt() {
        if (position >= 0 && position < limit) {
            return position;
        } else {
            throw new NoSuchElementException("bit set pointer out of bounds");
        }
    }

    @Override
    public boolean advance() {
        if (position >= 0 && position < limit) {
            position = bitSet.nextSetBit(position + 1);
        }
        return position >= 0 && position < limit;
    }

    @Override
    public boolean isAtEnd() {
        return position < 0 || position >= limit;
    }
}
