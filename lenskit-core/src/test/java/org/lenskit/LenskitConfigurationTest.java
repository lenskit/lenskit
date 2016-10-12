/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.junit.Ignore;
import org.junit.Test;
import org.lenskit.inject.RecommenderGraphBuilder;
import org.lenskit.inject.StaticInjector;

public class LenskitConfigurationTest {
    /**
     * Test that we can bind a file parameter to a string and have it work.
     *
     * @throws InjectionException if there is an (unexpected) injection failure.
     */
    @Test
    @Ignore("this test is no longer effective")
    @SuppressWarnings("unchecked")
    public void testSetFile() throws InjectionException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.clearRoots();
        // config.bind(EventFormat.class).to(Formats.ml100kFormat());
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addConfiguration(config);
        try (Injector inj = new StaticInjector(rgb.buildGraph())) {
            // File f = inj.getInstance(AnnotationBuilder.of(EventFile.class).build(), File.class);
            // assertThat(f.getName(), equalTo("ratings.foodat"));
        }
    }
}
