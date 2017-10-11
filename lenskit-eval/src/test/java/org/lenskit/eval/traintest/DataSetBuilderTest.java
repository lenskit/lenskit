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
package org.lenskit.eval.traintest;

import org.junit.Test;
import org.lenskit.data.dao.file.StaticDataSource;

import java.util.ArrayList;
import java.util.List;

import static net.java.quickcheck.generator.CombinedGenerators.uniqueValues;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someNonEmptyLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.nonEmptyStrings;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DataSetBuilderTest {
    @Test
    public void testAttributeOrder() {
        for (List<String> strings: someNonEmptyLists(uniqueValues(nonEmptyStrings(), 10))) {
            DataSetBuilder bld = new DataSetBuilder(nonEmptyStrings().next());
            StaticDataSource train = new StaticDataSource("train");
            bld.setTrain(train);
            StaticDataSource test = new StaticDataSource("test");
            bld.setTest(test);
            for (String str: strings) {
                bld.setAttribute(str, nonEmptyStrings().next());
            }
            DataSet ds = bld.build();
            assertThat(ds.getAttributes().size(), equalTo(strings.size() + 1));
            List<String> stringArray = new ArrayList<>();
            stringArray.add("DataSet");
            stringArray.addAll(strings);
            String[] strs = stringArray.toArray(new String[stringArray.size()]);
            assertThat(ds.getAttributes().keySet(), contains(strs));
        }
    }
}
