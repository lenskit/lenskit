package org.grouplens.lenskit.data.source;

import org.junit.Test;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.data.TextDataSourceSpec;

import java.io.File;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DefaultDataSourceSpecHandlerTest {
    @Test
    public void testBuildTextDataSource() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName("foo");
        spec.setFile(Paths.get("foo.csv"));

        DataSource src = SpecUtils.buildObject(DataSource.class, spec);
        assertThat(src, instanceOf(TextDataSource.class));
        assert src != null;
        TextDataSource tds = (TextDataSource) src;
        assertThat(tds.getName(), equalTo("foo"));
        assertThat(tds.getFile(), equalTo(new File("foo.csv")));
    }
}
