package org.grouplens.lenskit.util;

import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class to manage log data with {@link MDC}.  This class allows MDC parameters
 * to be set, and popped back off when the context is finished.
 */
public class LogContext {
    private final Map memory = MDC.getCopyOfContextMap();

    /**
     * Set a key in the MDC environment.
     * @param key The key to set.
     * @param value The key's value.
     * @see {@link MDC#put(String, String)}
     */
    public void put(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Finish the context.  This restores the MDC context map to the value it had when the
     * log context was created.
     */
    public void finish() {
        MDC.setContextMap(memory == null ? Collections.EMPTY_MAP : memory);
    }
}
