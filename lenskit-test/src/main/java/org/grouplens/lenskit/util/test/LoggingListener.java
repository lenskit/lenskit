package org.grouplens.lenskit.util.test;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * JUnit test listener that updates logging information.
 */
public class LoggingListener extends RunListener {
    private static final Logger logger = LoggerFactory.getLogger("testing");

    @Override
    public void testStarted(Description s) {
        logger.info("STARTING TEST {}", s);
        MDC.put("test", s.getDisplayName());
    }

    @Override
    public void testFinished(Description s) {
        MDC.remove("test");
        logger.info("FINISHED TEST {}", s);
    }

    @Override
    public void testFailure(Failure failure) {
        logger.error("test {} failed", failure.getDescription());
    }
}
