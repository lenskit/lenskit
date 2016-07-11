fn = cmdArgs[[1]]
message("Reading data from ", fn)

items = read.table(fn, sep="|", stringsAsFactors=FALSE)
message("read ", nrow(items), " items")

out.fn = cmdArgs[[2]]
message("writing data to ", out.fn)
write.table(items, file=out.fn, sep=",", row.names=FALSE, col.names=FALSE)