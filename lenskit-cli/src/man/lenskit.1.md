# lenskit

## Name

*lenskit* - a command-line tool for LensKit

## Synopsis

**lenskit** [OPTIONS] *subcommand* [arguments]

## Description

The LensKit command line tool provides several capabilities for examining, evaluating, and using
LensKit recommender algorithms.  It primarily operates on LensKit algorithm configurations and
eval scripts written in the corresponding Groovy DSLs.

The various specific tools are exposed via subcommands, much like **git**(1) and similar tools.
The subcommands are listed below (see [Subcommands](#Subcommands)), and each is described in more
detail in its own manual page.

## Options

--help
:   Print usage instructions.

--log-file *FILE*
:   Write logging output to *FILE*.

--log-level *LEVEL*
:   Output log messages at level *LEVEL* (or higher).  Can be one of ‘TRACE’, ‘DEBUG’, ‘INFO’, ‘WARN’, or ‘ERROR’.
    The default is ‘INFO’.
    
--log-file-level *LEVEL*
:   Specify a different level for the log file (specified with `--log-file`).  If this option is not specified, then
    the log level set with `--log-level` is used for both the console and the log file.

--debug-grapht
:   Output INFO (or DEBUG, if **--debug** is also used) logging messages from Grapht.  Grapht is
    pretty noisy, so by default its output is filtered to warnigns and errors.  If you need to
    debug a problem that is occurring in Grapht, use this option.

## Subcommands

Each command is documented in its own man page, *lenskit-command*(1).

[version](man:lenskit-version(1))
:   Print the LensKit version.

[train-model](man:lenskit-train-model(1))
:   Train a recommender model and save it to disk.

[predict](man:lenskit-predict(1))
:   Predict user ratings for items, using a configuration or a trained model.

[recommend](man:lenskit-recommend(1))
:   Recommend items for users, using a configuration or a trained model.

[global-recommend](man:lenskit-global-recommend(1))
:   Recommend items with respect to a set of reference items.

[graph](man:lenskit-graph(1))
:   Output a GraphViz diagram of a recommender configuration (either from configuration files or a
    trained model).

[pack-ratings](man:lenskit-pack-ratings(1))
:   Pack rating data into a binary file for more efficient access.

## Environment and System Properties

The LensKit CLI (or its launcher script) recognize the following environment variables:

JAVA_OPTS
:   Additional flags to pass to the JVM (such as `-Xmx4g` to set the memory limit).

JAVA_HOME
:   Where to find the Java Runtime Environment.

Also, the following Java system properties can be set for useful effects:

logback.configurationFile
:   The location of a Logback configuration file.  This overrides all built-in or command line
    logging configuration (e.g. `--log-file`).

## See Also

-  Common arguments: [**lenskit-input-data**(7)](man:lenskit-input-data(7)),
   [**lenskit-script-environment**(7)](man:lenskit-script-environment(7))
-  Man pages for subcommands: [**lenskit-version**(1)](man:lenskit-version(1)),
   [**lenskit-train-model**(1)](man:lenskit-train-model(1)),
   [**lenskit-predict**(1)](man:lenskit-predict(1)),
   [**lenskit-recommend**(1)](man:lenskit-recommend(1)),
   [**lenskit-global-recommend**(1)](man:lenskit-global-recommend(1)),
   [**lenskit-graph**(1)](man:lenskit-graph(1)),
   [**lenskit-pack-ratings**(1)](man:lenskit-pack-ratings(1))
-  The [LensKit home page](http://lenskit.org)
-  The [LensKit manual](http://lenskit.org/documentation)

## Project Information

This command is a part of LensKit, an open source recommender systems toolkit
developed by [GroupLens Research](http://grouplens.org).\\
Copyright 2010-2015 LensKit contributors\\
Copyright 2010-2014 Regents of the University of Minnesota

Work on LensKit has been funded by the National Science Foundation under
grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
