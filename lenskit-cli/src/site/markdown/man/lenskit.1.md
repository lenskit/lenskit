# lenskit(1)

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

-d, --debug
:   Increase verbosity, printing debug messages to the console.  By default, only messages at INFO
    and higher levels are logged.  The log file, if specified, always receives debug-level output.

--debug-grapht
:   Output INFO (or DEBUG, if **--debug** is also used) logging messages from Grapht.  Grapht is
    pretty noisy, so by default its output is filtered to warnigns and errors.  If you need to
    debug a problem that is occurring in Grapht, use this option.

## Subcommands

Each command is documented in its own man page, *lenskit-command*(1).

[version](lenskit-version.1.html)
:   Print the LensKit version.

[train-model](lenskit-train-model.1.html)
:   Train a recommender model and save it to disk.

[predict](lenskit-predict.1.html)
:   Predict user ratings for items, using a configuration or a trained model.

[recommend](lenskit-recommend.1.html)
:   Recommend items for users, using a configuration or a trained model.

[graph](lenskit-graph.1.html)
:   Output a GraphViz diagram of a recommender configuration (either from configuration files or a
    trained model).

[eval](lenskit-eval.1.html)
:   Run a LensKit evaluation script.

[pack-ratings](lenskit-pack-ratings.1.html)
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
    logging configuration.

## See Also

-  Man pages for subcommands: [**lenskit-version**(1)](lenskit-version.1.html),
   [**lenskit-train-model**(1)](lenskit-train-model.1.html),
   [**lenskit-predict**(1)](lenskit-predict.1.html),
   [**lenskit-recommend**(1)](lenskit-recommend.1.html),
   [**lenskit-graph**(1)](lenskit-graph.1.html),
   [**lenskit-eval**(1)](lenskit-eval.1.html),
   [**lenskit-pack-ratings**(1)](lenskit-pack-ratings.1.html)
-  The [LensKit home page](http://lenskit.grouplens.org)
-  The [LensKit manual](http://github.com/grouplens/lenskit/wiki/Manual)

## Project Information

This command is a part of LensKit, an open source recommender systems toolkit
developed by [GroupLens Research](http://grouplens.org).
Copyright 2010-2014 Regents of the University of Minnesota and contributors.

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
