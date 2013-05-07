/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.maven;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.maven.plugin.logging.Log;

/**
 * Logback appender that writes to the Maven log.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MavenLogAppender<E extends ILoggingEvent> extends UnsynchronizedAppenderBase<E> {
    private Layout<E> layout;
    private static InheritableThreadLocal<Log> mavenLogger = new InheritableThreadLocal<Log>();

    public void setLayout(Layout<E> lay) {
        layout = lay;
    }

    public Layout<E> getLayout() {
        return layout;
    }

    /**
     * Set the logger for the current thread. It will be propagated down to subloggers.
     * @param log The Maven logger.
     */
    public static void setLog(Log log) {
        mavenLogger.set(log);
        log.info("initializing Maven logging");
    }

    /**
     * Remove the logger for the current thread.
     */
    public static void removeLog() {
        mavenLogger.remove();
    }

    @Override
    protected void append(E event) {
        Log log = mavenLogger.get();
        if (log == null) {
            return;
        }

        String fmt = layout.doLayout(event);
        Level lvl = event.getLevel();
        if (lvl.isGreaterOrEqual(Level.ERROR)) {
            log.error(fmt);
        } else if (lvl.isGreaterOrEqual(Level.WARN)) {
            log.warn(fmt);
        } else if (lvl.isGreaterOrEqual(Level.INFO)) {
            log.info(fmt);
        } else {
            log.debug(fmt);
        }
    }
}
