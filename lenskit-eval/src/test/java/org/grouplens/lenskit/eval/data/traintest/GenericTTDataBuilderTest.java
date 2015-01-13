package org.grouplens.lenskit.eval.data.traintest;

import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.data.GenericDataSource;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static net.java.quickcheck.generator.CombinedGenerators.uniqueValues;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someNonEmptyLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.nonEmptyStrings;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GenericTTDataBuilderTest {
    @Test
    public void testAttributeOrder() {
        for (List<String> strings: someNonEmptyLists(uniqueValues(nonEmptyStrings(), 10))) {
            GenericTTDataBuilder bld = new GenericTTDataBuilder(nonEmptyStrings().next());
            bld.setTrain(new GenericDataSource("train", EventCollectionDAO.create(Collections.<Rating>emptyList())));
            bld.setTest(new GenericDataSource("test", EventCollectionDAO.create(Collections.<Rating>emptyList())));
            for (String str: strings) {
                bld.setAttribute(str, nonEmptyStrings().next());
            }
            TTDataSet ds = bld.build();
            assertThat(ds.getAttributes().size(), equalTo(strings.size()));
            assertThat(ds.getAttributes().keySet(), contains(strings.toArray()));
        }
    }
}
