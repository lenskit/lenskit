# Data Manifest Schema

This document describes the format of data manifests.

Data manifests are JSON-style data.  Currently they are generally described in YAML files, but they may be described in other formats such as JSON.

## Overall Layout

A manifest file represents either a list or a map.

If it is a map and contains at least one of the keys `file` or `type`, then the map is taken to describe a single data file (see below).

Otherwise, the file describes a collection of data files.  If it is a map, then those files are labelled with their map keys; otherwise, they are labeled with their positions (starting from 0).

For example:

```yaml
ratings:
	file: "ratings.csv"
	format: csv
	entity: rating
	header: true
items:
	file: "items.csv"
	entity: movie
	header: true
	columns:
	    movieId: id
	    title: name
```

defines a data source consisting of ratings data, read from `ratings.csv`, and movie titles read from `items.csv`.  The files have labels.  The same manifest can be written with numeric labels as follows:

```yaml
-	file: "ratings.csv"
	format: csv
	entity: rating
	header: true
-	file: "items.csv"
	entity: movie
	header: true
	columns:
	    movieId: id
	    title: name
```

## Data Source Description

Individual data sources are described with the following schema.

`type`
:   The data source type.  Currently only `textfile` is supported.

The remainder of the keys are defined by the particular data source.

### `textfile` data sources

`textfile` sources read data from text files, usually line-delimited.

`file`
:   the file to read.

`format`
:   The file format.  Can be one of:
	 - `delimited` — delimited, columnar text (default delimiter is `\t`)
	 - `csv` — `delimited` with a delimiter of `,`
	 - `tsv` — tab-separated (`delimited` with delimiter of `\t`)

`entity_type`
:   The name of the entity type contained in this file.  The entity type is also used to provide defaults for the columns.  The default is `rating`.

`builder`
:   The entity builder to be used for these entities.  The entity type may provide a default; otherwise, `org.lenskit.data.entities.BasicEntityBuilder` is used.  The keyword `basic` can be used to refer to the basic entity builder, to override a default entity builder if desired.

`delimiter`
:   The delimiter string for `delimited` files.

`header`
:   Whether the file has a header.  If `true`, the file has a single-line header; if `false`, no header is assumed.  If an integer, it is the number of lines to skip (the lines are *not* interpreted as containing field names).  The default is `false`.

`columns`
:   A map or list describing the columns in the file (for columnar formats).  If `header` is `true`, then this can be a map whose keys are column header labels and whose values are column descriptors; otherwise, it is a list of column descriptors.  If any column is missing or has a `null` descriptor, then that column is ignored.

	A column descriptor can be either a string, giving a column name, or a map with keys `name` (the column name) and `type` (the column type).
	
`indexes`
:   A list of attribute names to be indexed for fast lookup.  If no indexes are specified, item and user IDs are indexed by default if present.
	
`meta`
:   Metadata about the data, such as the `domain` for rating values.`
	
## Attribute Data Types

The following types are supported for attributes:

`int` or `Integer`
:   Java `integer`.

`long` or `Long`
:   Java `long`.

`double`, `real`, or `Double`
:   Java `double`.

`string` or `String`
:   Java `String`.

Java class name
:   The corresponding class.  Must be convertible with Joda-Convert.

The entity type may provide default types for various attribute names, in addition to providing a default set of columns if `columns` is missing entirely.  If no default is available and the type is not specified, attributes are assumed to be strings.
