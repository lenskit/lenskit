library(assertthat)

message("Loading predictions")
predictions = read.csv("predictions.csv", header=TRUE)
message("loaded ", nrow(predictions), " predictions")

message("loading test pairs")
pairs = read.csv("pairs.csv", col.names=c("user", "item"), header=FALSE)
message("loaded ", nrow(pairs), " pairs")

assert_that(are_equal(nrow(pairs), nrow(predictions)))

merged = merge(predictions, pairs)
assert_that(are_equal(nrow(merged), nrow(pairs)))