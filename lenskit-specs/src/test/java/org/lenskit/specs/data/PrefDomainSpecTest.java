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
package org.lenskit.specs.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.Distribution;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PrefDomainSpecTest {
    /**
     * Generate, serialize, and deserialize a bunch of preference domains.
     */
    @Test
    public void testBunchOfPrecisions() throws IOException {
        Generator<Double> mins = PrimitiveGenerators.doubles(0, 2, Distribution.INVERTED_NORMAL);
        Generator<Double> maxes = PrimitiveGenerators.doubles(4, 6, Distribution.INVERTED_NORMAL);
        Generator<Double> precs = PrimitiveGenerators.doubles(4, 6, Distribution.INVERTED_NORMAL);
        Generator<Boolean> hasPrecision = PrimitiveGenerators.booleans();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter w = mapper.writer();
        ObjectReader r = mapper.reader(PrefDomainSpec.class);
        for (int i = 0; i < 50; i++) {
            PrefDomainSpec dom = new PrefDomainSpec();
            dom.setMaximum(maxes.next());
            dom.setMinimum(mins.next());
            if (hasPrecision.next()) {
                dom.setPrecision(precs.next());
            } else {
                dom.setPrecision(Double.NaN);
            }

            String json = w.writeValueAsString(dom);

            PrefDomainSpec d2 = r.readValue(json);

            assertThat(d2.getMinimum(), equalTo(dom.getMinimum()));
            assertThat(d2.getMaximum(), equalTo(dom.getMaximum()));
            assertThat(d2.getPrecision(), equalTo(dom.getPrecision()));
        }
    }
}
