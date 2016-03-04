package org.lenskit.config;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class LenskitConfigDSLTest {
    @Test
    public void testInitialBaseURI() {
        LenskitConfigDSL dsl = LenskitConfigDSL.forConfig(new LenskitConfiguration());
        assertThat(new File(dsl.getBaseURI().getPath()),
                   equalTo(SystemUtils.getUserDir()));
    }

    @Test
    public void testConfiguredBaseURI() {
        LenskitConfigDSL dsl = LenskitConfigDSL.forConfig(new LenskitConfiguration());
        dsl.setBaseURI(new File("/tmp").toURI());
        assertThat(new File(dsl.getBaseURI().getPath()),
                   equalTo(new File("/tmp").getAbsoluteFile()));
    }
}
