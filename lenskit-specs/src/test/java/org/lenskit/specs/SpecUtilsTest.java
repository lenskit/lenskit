package org.lenskit.specs;

import org.junit.Test;
import org.lenskit.specs.data.TextDataSourceSpec;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SpecUtilsTest {
    @Test
    public void copySimpleSpec() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName("foo");
        spec.setFile(Paths.get("rodney.csv"));
        spec.setDelimiter("@");

        TextDataSourceSpec copy = SpecUtils.copySpec(spec);
        assertThat(copy, equalTo(spec));
        assertThat(copy, not(sameInstance(spec)));
    }
}
