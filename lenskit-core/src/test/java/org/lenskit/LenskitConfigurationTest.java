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
