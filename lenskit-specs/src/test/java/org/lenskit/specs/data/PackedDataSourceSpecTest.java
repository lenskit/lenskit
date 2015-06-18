package org.lenskit.specs.data;

import org.junit.Test;
import org.lenskit.specs.SpecUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PackedDataSourceSpecTest {
    @Test
    public void testBasicSpecBean() {
        PackedDataSourceSpec spec = new PackedDataSourceSpec();
        spec.setName("pack");
        spec.setFile("data.pack");
        assertThat(spec.getName(), equalTo("pack"));
        assertThat(spec.getFile(), equalTo("data.pack"));
        assertThat(spec.getDomain(), nullValue());
    }

    @Test
    public void testBasicSpecRoundTrip() {
        PackedDataSourceSpec spec = new PackedDataSourceSpec();
        spec.setName("pack");
        spec.setFile("data.pack");

        String json = SpecUtils.stringify(spec);

        DataSourceSpec s2 = SpecUtils.parse(DataSourceSpec.class, json);
        assertThat(s2, instanceOf(PackedDataSourceSpec.class));

        PackedDataSourceSpec pds2 = (PackedDataSourceSpec) s2;

        assertThat(pds2.getName(), equalTo("pack"));
        assertThat(pds2.getFile(), equalTo("data.pack"));
        assertThat(pds2.getDomain(), nullValue());
    }
}
