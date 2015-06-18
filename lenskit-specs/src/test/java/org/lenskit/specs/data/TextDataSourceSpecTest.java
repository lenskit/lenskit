package org.lenskit.specs.data;

import org.junit.Test;
import org.lenskit.specs.SpecUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TextDataSourceSpecTest {
    @Test
    public void testBasicTextDataSource() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName("foo");
        spec.setFile("wombat.csv");

        assertThat(spec.getName(), equalTo("foo"));
        assertThat(spec.getFile(), equalTo("wombat.csv"));
        assertThat(spec.getDomain(), nullValue());
    }

    @Test
    public void testTextDataSourceRoundTrip() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName("foo");
        spec.setFile("wombat.csv");

        String json = SpecUtils.stringify(spec);

        DataSourceSpec s2 = SpecUtils.parse(DataSourceSpec.class, json);

        assertThat(s2, instanceOf(TextDataSourceSpec.class));

        TextDataSourceSpec tds2 = (TextDataSourceSpec) s2;

        assertThat(tds2.getName(), equalTo("foo"));
        assertThat(tds2.getFile(), equalTo("wombat.csv"));
        assertThat(tds2.getDomain(), nullValue());

        assertThat(tds2, equalTo(spec));
    }
}
