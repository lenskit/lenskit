/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MavenLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = 6034792425437060623L;
    
    private String name;
    private Log log;
    
    public MavenLogger(String name, Log log) {
        this.name = name;
        this.log = log;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String)
     */
    @Override
    public void trace(String msg) {
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg) {
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(String format, Object[] argArray) {
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void trace(String msg, Throwable t) {
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String msg) {
        log.debug(name + " " + msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg) {
        debug(MessageFormatter.format(format, arg).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        debug(MessageFormatter.format(format, arg1, arg2).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug(String format, Object[] argArray) {
        debug(MessageFormatter.arrayFormat(format, argArray).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug(String msg, Throwable t) {
        debug(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String)
     */
    @Override
    public void info(String msg) {
        log.info(name + " " + msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg) {
        info(MessageFormatter.format(format, arg).getMessage());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        info(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public void info(String format, Object[] argArray) {
        info(MessageFormatter.arrayFormat(format, argArray).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info(String msg, Throwable t) {
        info(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String)
     */
    @Override
    public void warn(String msg) {
        log.warn(name + " " + msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg) {
        warn(MessageFormatter.format(format, arg).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn(String format, Object[] argArray) {
        warn(MessageFormatter.arrayFormat(format, argArray).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        warn(MessageFormatter.format(format, arg1, arg2).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn(String msg, Throwable t) {
        warn(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String)
     */
    @Override
    public void error(String msg) {
        log.error(name + " " + msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg) {
        error(MessageFormatter.format(format, arg).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        error(MessageFormatter.format(format, arg1, arg2).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
     */
    @Override
    public void error(String format, Object[] argArray) {
        error(MessageFormatter.arrayFormat(format, argArray).toString());
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String msg, Throwable t) {
        error(msg);
    }

}
