# lenskit-pack-ratings

## Name

**lenskit pack-ratings** - pack rating data into a binary file for efficient access.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **pack-ratings** *OPTIONS*

## Description

The `pack-ratings` command packs rating data into a binary file that LensKit can efficiently map
into memory.  These files make many recommender operations significantly faster and less memory
intensive, including model building and recommendation with certain algorithms.

## Options

--help
:   Show usage help.

-o *FILE*, --output-file *FILE*
:   Write the resulting recommender model to *FILE*.  If not specified, the ratings will be packed
    into the file `ratings.pack`.

--no-timestamps
:   Ignore timestamps in the input data and omit them from the packed ratings.

This command also takes the standard [input data options](man:lenskit-input-data(7)).

## Known Issues

If you want timestamped data, the input data must be sorted by timestamp.  LensKit will eventually
be able to sort data in the packing process, but cannot currently do so.

## See Also

[**lenskit**(1)](man:lenskit(1)), [**lenskit-input-data**(7)](man:lenskit-input-data(7))

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
