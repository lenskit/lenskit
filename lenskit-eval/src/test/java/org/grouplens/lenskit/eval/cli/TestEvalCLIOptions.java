package org.grouplens.lenskit.eval.cli;

import static org.grouplens.lenskit.eval.cli.EvalCLIOptions.parse;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.grouplens.lenskit.eval.config.EvalConfig;
import org.junit.Test;

import java.io.File;

/**
 * Test the eval CLI option parser.
 * @author Michael Ekstrand
 */
public class TestEvalCLIOptions {
    @Test
    public void testNoArguments() {
        String[] args = {};
        EvalCLIOptions opts = parse(args);
        // properties aren't null
        assertThat(opts.getProperties(),
                   notNullValue());
        // args is empty array
        assertThat(opts.getArgs(), notNullValue());
        assertThat(opts.getArgs().length, equalTo(0));
        // config file is proper default
        assertThat(opts.getScriptFile(),
                   equalTo(new File("eval.groovy")));

        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.force(), equalTo(false));
        assertThat(cfg.getThreadCount(), equalTo(1));
    }

    @Test
    public void testForce() {
        String[] args = {"--force"};
        EvalCLIOptions opts = parse(args);
        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.force(), equalTo(true));
    }

    @Test
    public void testScript() {
        String[] args = {"-ffoo.groovy"};
        EvalCLIOptions opts = parse(args);
        assertThat(opts.getScriptFile(), equalTo(new File("foo.groovy")));
    }

    @Test
    public void testDefine() {
        String[] args = {"-Dscroll.name=hackem muche"};
        EvalCLIOptions opts = parse(args);
        assertThat(opts.getProperties().getProperty("scroll.name"),
                   equalTo("hackem muche"));
        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.get("scroll.name"), equalTo("hackem muche"));
    }

    @Test
    public void testThreadCount() {
        String[] args = {"-j4"};
        EvalCLIOptions opts = parse(args);
        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.getThreadCount(), equalTo(4));
    }

    @Test
    public void testAutoThreadCount() {
        String[] args = {"-j"};
        EvalCLIOptions opts = parse(args);
        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.getThreadCount(),
                   equalTo(Runtime.getRuntime().availableProcessors()));
    }

    @Test
    public void testMultiThreadCount() {
        String[] args = {"-j", "-F"};
        EvalCLIOptions opts = parse(args);
        EvalConfig cfg = new EvalConfig(opts.getProperties());
        assertThat(cfg.getThreadCount(),
                   equalTo(Runtime.getRuntime().availableProcessors()));
        assertThat(cfg.force(), equalTo(true));
    }
}
