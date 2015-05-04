# lenskit-input-data

## Name

Specifying input data for LensKit commands.

## Description

Several subcommands of [**lenskit**(1)](man:lenskit(1)) allow for input data to be specified.
They all take the same options to control their input data, documented here.

## Primary Input File

LensKit commands can read data in several different ways.  To give the recommendation or model-building
process some data to work with, one of the following mutually-exclusive options must be present:

--ratings-file *FILE*
:   Read ratings from the delimited text file *FILE*.

--csv-file *FILE*
:   Read ratings from the CSV file *FILE*.  This is identical to passing `--ratings-file=FILE` with
    `--delimiter=,`.

--tsv-file *FILE*
:   Read ratings from the tab-separated file *FILE*. This is identical to passing
    `--ratings-file=FILE` with `--delimiter=^I`, but doesn't require you to know how to encode
    tab characters in your shell.

--events-file *FILE*
:   Read events from the text file *FILE*. One event will be read per line, usually in a delimited column format such
    as a CSV or TSV file.

--pack-file *FILE*
:   Read ratings from the packed rating file *FILE*.  Packed files can be created with the
    [`pack-ratings`](man:lenskit-pack-ratings(1)) command.

## Supplementary Input Files

In addition to the rating or event data, you can also provide 

--item-names *FILE*
:   Load an item ID to name mapping from the CSV file *FILE*.  This will be used to provide an
    `ItemNameDAO` and `ItemDAO`.  It expects a CSV file where the first column is the item ID and
    the second column is the name. Standard CSV quoting is supported.

## Input Options

The options in this section provide additional control over the input.

### Delimited Ratings and Event Files

These options control how events (`--events-file`) or ratings (`--ratings-file`) are parsed.

-d *DELIM*, --delimiter *DELIM*
:   Use *DELIM* as the delimiter for delimited text files.  Only effective in conjunction with
    `--ratings-file` or `--events-file`; does not affect the parsing of `--item-names`.

-H *N*, --header-lines *N*
:   Skip *N* header lines at the top of the event or ratings file.

-t *TYPE*, --event-type *TYPE*
:   When used with `--events-file`, specifies the type of events that are being read from the file.

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
