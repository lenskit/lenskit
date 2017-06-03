library(assertthat)
recs = read.csv(gzfile("predictions.csv.gz"))
assert_that(are_equal(recs$Prediction, recs$Rating))

count = aggregate(Item ~ User, recs, length)
assert_that(nrow(count) > 0)
assert_that(all(count$Item == 5))