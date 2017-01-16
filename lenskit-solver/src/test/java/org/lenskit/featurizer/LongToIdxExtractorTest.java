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

package org.lenskit.featurizer;

import org.junit.Test;
import org.lenskit.data.entities.BasicEntityBuilder;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.space.IndexSpace;
import org.lenskit.space.SynchronizedIndexSpace;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LongToIdxExtractorTest {

    @Test
    public void testLongToIdxExtractor() {
        LongToIdxExtractor extractor = new LongToIdxExtractor("biases", "user", "userBiasIdx");
        IndexSpace indexSpace = new SynchronizedIndexSpace();
        indexSpace.requestKeyMap("biases");
        BasicEntityBuilder builder = new BasicEntityBuilder(CommonTypes.RATING);
        builder.setAttribute(CommonAttributes.ENTITY_ID, 1L);
        builder.setAttribute(CommonAttributes.USER_ID, 1L);
        Map<String, List<Feature>> features = extractor.extract(builder.build(), true, indexSpace);
        assertThat(features.containsKey("userBiasIdx"), equalTo(true));
        assertThat(features.get("userBiasIdx").size(), equalTo(1));
        assertThat(features.get("userBiasIdx").get(0).getValue(), equalTo(1.0));
    }
}
