# lenskit-recommend

## Name

**lenskit recommend** - recommend items for users.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **recommend** [OPTIONS] *USER*...

## Description

The `recommend` command recommends items for some users.  It loads a recommender from a
trained model file and/or LensKit configuration scripts and uses the configured algorithm to
produce recommendations.

## Options

*USER*
:   A user to recommend for.

--help
:   Show usage help.

-n *N*
:   Produce *N* recommendations.  The default is 10.

-m *FILE*, --model-file *FILE*
:   Load a trained recommender engine from *FILE*.

-c *SCRIPT*, --config-file *SCRIPT*
:   Configure the recommender using *SCRIPT*.  This option can be specified multiple times, and
    later configurations take precedence over earlier ones.  If `--model-file` is also specified,
    the scripts are used to modify the trained model.

--print-channel *CHAN*
:   In addition to item scores, also print the value in side channel *CHAN*.

### Input Data Options

This command can read data in several different ways.  To give the recommendation process some
data to work with, one of the following mutually-exclusive options must be present:

--ratings-file *FILE*
:   Read ratings from the delimited text file *FILE*.

--csv-file *FILE*
:   Read ratings from the CSV file *FILE*.  This is identical to passing `--ratings-file=FILE` with
    `--delimiter=,`.

--tsv-file *FILE*
:   Read ratings from the tab-separated file *FILE*. This is identical to passing
    `--ratings-file=FILE` with `--delimiter=^I`, but doesn't require you to know how to encode
    tab characters in your shell.

--pack-file *FILE*
:   Read ratings from the packed rating file *FILE*.  Packed files can be created with the
    [**pack-ratings**](lenskit-pack-ratings.1.html) command.

Additionally, the following options provide additional control over the data input:

-d *DELIM*, --delimiter *DELIM*
:   Use *DELIM* as the delimiter for delimited text files.  Only effective in conjunction with
    `--ratings-file`.

### Script Environment Options

This command takes the standard LensKit script environment options for controlling how configuration
scripts are interpreted:

-C *URL*, --classpath *URL*
:   Add *URL* (which can be a path to a local directory or JAR file) to the classpath for loading
    the configuration scripts.  This URL can contain additional components for the recommenders.
    This option can be specified multiple times to add multiple locations to the classpath.

-D *PROP*=*VALUE*, --define *PROP*=*VALUE*
:   Define the property *PROP* to equal *VALUE*.  This option is currently ignored for this command.
    To set Java system properties, use the `JAVA_OPTS` environment variable (see
    [**lenskit**(1)](lenskit.1.html)).

## See Also

[**lenskit**(1)](./lenskit.1.html)

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
