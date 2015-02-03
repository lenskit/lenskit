package org.grouplens.lenskit.core;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.lenskit.data.text.EventFile;
import org.grouplens.lenskit.data.text.EventFormat;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.grouplens.lenskit.inject.RecommenderGraphBuilder;
import org.grouplens.lenskit.inject.StaticInjector;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LenskitConfigurationTest {
    /**
     * Test that we can bind a file parameter to a string and have it work.
     *
     * @throws InjectionException if there is an (unexpected) injection failure.
     */
    @Test
    public void testSetFile() throws InjectionException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.clearRoots();
        config.addRoot(TextEventDAO.class);
        config.set(EventFile.class).to("ratings.foodat");
        config.bind(EventFormat.class).to(Formats.ml100kFormat());
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addConfiguration(config);
        Injector inj = new StaticInjector(rgb.buildGraph());
        File f = inj.getInstance(AnnotationBuilder.of(EventFile.class).build(), File.class);
        assertThat(f.getName(), equalTo("ratings.foodat"));
    }
}
