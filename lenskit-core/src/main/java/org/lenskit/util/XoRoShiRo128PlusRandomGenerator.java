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
package org.lenskit.util;

import org.apache.commons.math3.random.AbstractRandomGenerator;

import javax.inject.Inject;

/**
 * XoRoShiRo128+ random number generator. This is implemented from the public-domain sources by David Blackman
 * and Sebastiano Vigna at <http://xoroshiro.di.unimi.it/xoroshiro128plus.c> and the notes at
 * <http://xoroshiro.di.unimi.it/>.
 */
public class XoRoShiRo128PlusRandomGenerator extends AbstractRandomGenerator {
    private long s0, s1;

    /**
     * Create a new generator seeded with {@link System#currentTimeMillis()}.
     */
    @Inject
    public XoRoShiRo128PlusRandomGenerator() {
        setSeed(System.currentTimeMillis());
    }

    /**
     * Create a new RNG with a specified seed.
     * @param seed The seed.
     */
    public XoRoShiRo128PlusRandomGenerator(long seed) {
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        // seed with a SplitMix64 generator
        // see http://xoroshiro.di.unimi.it/splitmix64.c
        long x = seed;
        x += 0x9e3779b97f4a7c15L;

        long z = x;
        z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >> 27)) * 0x94d049bb133111ebL;
        s0 = z ^ (z >> 31);

        x += 0x9e3779b97f4a7c15L;
        z = x;
        z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >> 27)) * 0x94d049bb133111ebL;
        s1 = z ^ (z >> 31);
    }

    @Override
    public double nextDouble() {
        long x = nextLong();
        return (x >>> 11) * 0x1.0p-53;
    }

    @Override
    public long nextLong() {
        long result = s0 + s1;

        s1 ^= s0;
        s0 = Long.rotateLeft(s0, 55) ^ s1 ^ (s1 << 14); // a, b
        s1 = Long.rotateLeft(s1, 36); // c

        return result;
    }

    @Override
    public int nextInt() {
        // use the high-order 32 bits
        return (int) (nextLong() >>> 32);
    }

    @Override
    public boolean nextBoolean() {
        // as recommended: return the sign bit
        return nextLong() < 0;
    }
}
