package org.grouplens.lenskit.eval.maven;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.maven.plugin.logging.Log;

/**
 * Logback appender that writes to the Maven log.
 * @author Michael Ekstrand
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
