package org.grouplens.lenskit.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshLogger implements com.jcraft.jsch.Logger {
    private Logger logger = LoggerFactory.getLogger("jsch");

    public boolean isEnabled(int level) {
        switch (level) {
        case 0: // DEBUG
            return logger.isDebugEnabled();
        case 1: // INFO
            return logger.isInfoEnabled();
        case 2: // WARN
            return logger.isWarnEnabled();
        case 3: // ERROR
        case 4: // FATAL
            return logger.isErrorEnabled();
        default:
            return true;
        }
    }

    @Override
    public void log(int level, String message) {
        switch (level) {
        case 0: // DEBUG
            logger.debug("ssh: {}", message);
            break;
        case 1: // INFO
            logger.info("ssh: {}", message);
            break;
        case 2: // WARN
            logger.warn("ssh: {}", message);
            break;
        case 3: // ERROR
            logger.error("ssh: {}", message);
            break;
        case 4: // FATAL
            logger.error("FATAL SSH ERROR: {}", message);
            break;
        default:
            logger.error("ssh: {}");
        }
    }
}
