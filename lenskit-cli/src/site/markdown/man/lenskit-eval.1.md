# lenskit-eval(1)

## Name

**lenskit eval** - run an offline evaluation of recommender behavior and performance.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **eval** [OPTIONS] [*TARGET*...]

## Description

The `eval` command runs a LensKit evaluation script to measure the behavior and performance
(such as recommendation or prediction accuracy) of one or more recommender algorithms.

Evaluation scripts are written in Groovy, using an embedded domain-specific language for describing
LensKit evaluations.  This is documented more in the LensKit manual; there is a link in See Also.

The **lenskit eval** subcommand serves the same purpose as the now-deprecated **lenskit-eval**
command, with slightly different invocation syntax.  Use **lenskit eval** in new scripts and
experiments.

## Options

*TARGET*
:   Run the target *TARGET* in the evaluation script.  If no targets are specified on the command
    line, the script is run (which is sufficient for scripts that do not use targets), or the
    default target specified by the script is run.

--help
:   Show usage help.

-f *SCRIPT*, --file *SCRIPT*
:   Load evaluation script *SCRIPT*.  The default is `eval.groovy`.

-j *N*, --thread-count *N*
:   Use up to *N* threads for parallelizable portions of the evaluation.

-F, --force
:   Force eval tasks to re-run, even if they detect that their outputs are up-to-date.  Not all
    tasks do up-to-date checking.

### Script Environment Options

This command takes the standard LensKit script environment options for controlling how configuration
scripts are interpreted:

-C *URL*, --classpath *URL*
:   Add *URL* (which can be a path to a local directory or JAR file) to the classpath for loading
    the evaluation script.  This URL can contain additional components for the recommenders or
    evaluation.  This option can be specified multiple times to add multiple locations to the
    classpath.

-D *PROP*=*VALUE*, --define *PROP*=*VALUE*
:   Define the property *PROP* to equal *VALUE*.  These properties are not Java system properties,
    but are available via the `config` object in evaluation scripts.  This object can be accessed
    as a hash in Groovy.

## See Also

-   [**lenskit**(1)](./lenskit.1.html)
-   [Using the LensKit Evaluator](http://github.com/grouplens/lenskit/wiki/Evaluator)

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
