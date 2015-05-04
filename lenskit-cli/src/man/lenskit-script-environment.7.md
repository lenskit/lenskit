# lenskit-input-data

## Name

Configuring the environment for interpreting LensKit scripts.

## Description

Several subcommands of [`lenskit`(1)](./lenskit.1.html) interpret scripts, either to configure recommenders
or to run evaluations.  These subcommands support some common options for configuring the Java environment
in which these scripts are interpreted; this page documents those options.

## Script Environment Options

-C *URL*, --classpath *URL*
:   Add *URL* (which can be a path to a local directory or JAR file) to the classpath for loading
    the configuration scripts.  This URL can contain additional components for the recommenders.
    This option can be specified multiple times to add multiple locations to the classpath.

-D *PROP*=*VALUE*, --define *PROP*=*VALUE*
:   Define the property *PROP* to equal *VALUE*.  These properties will be available in the `properties`
    object in the interpreted script.  To set Java system properties, use the `JAVA_OPTS` environment 
    variable (see [**lenskit**(1)](lenskit.1.html)).

## See Also

[**lenskit**(1)](man:lenskit(1))

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
