package org.lenskit.results;

import org.junit.Test;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultListTest {
    @Test
    public void testEmptyList() {
        ResultList<Result> r = Results.newResultList();
        assertThat(r.isEmpty(), equalTo(true));
        assertThat(r.size(), equalTo(0));
        assertThat(r.idList(), hasSize(0));
    }

    @Test
    public void testSingletonList() {
        ResultList<Result> r = Results.<Result>newResultList(Results.create(42L, 3.5));
        assertThat(r, hasSize(1));
        assertThat(r, contains((Result) Results.create(42L, 3.5)));
        assertThat(r.idList(), hasSize(1));
        assertThat(r.idList(), contains(42L));
    }

    @Test
    public void testMultiList() {
        ResultList<Result> r = Results.<Result>newResultList(Results.create(42L, 3.5),
                                                             Results.create(37L, 4.2));
        assertThat(r, hasSize(2));
        assertThat(r, contains((Result) Results.create(42L, 3.5),
                               (Result) Results.create(37L, 4.2)));
    }
}
