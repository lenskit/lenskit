/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
