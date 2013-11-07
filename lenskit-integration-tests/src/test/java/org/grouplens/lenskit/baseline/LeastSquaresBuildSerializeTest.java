/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.test.ML100KTestSuite;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Do major tests on the item-item recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LeastSquaresBuildSerializeTest extends ML100KTestSuite {
    @Ignore("broken until 2.1 brings back serialization")
    @Test
    public void testBuildAndSerializeModel() throws RecommenderBuildException, IOException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemRecommender.class)
              .to(TopNItemRecommender.class);
        config.bind(ItemScorer.class)
              .to(LeastSquaresItemScorer.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);
        assertThat(engine, notNullValue());
        // engine.setSymbolMapping(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.write(out);
        byte[] bytes = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        LenskitRecommenderEngine loaded = LenskitRecommenderEngine.load(in);
        assertThat(loaded, notNullValue());
        // loaded.setSymbolMapping(mapping);

        LenskitRecommender rec = loaded.createRecommender();
        assertThat(rec.getItemScorer(),
                   instanceOf(LeastSquaresItemScorer.class));
    }
}
