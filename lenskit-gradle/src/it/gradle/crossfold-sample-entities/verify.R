library('assertthat')
message("reading all ratings")
ratings = read.csv("build/ratings.csv",
                   header=FALSE, col.names=c("user", "item", "rating", "timestamp"))

test_data = do.call(rbind, lapply(1:5, function(i) {
    message("reading test set ", i)
    test = read.csv(sprintf("build/crossfold.out/part%02d.test.csv", i),
                    header=FALSE, col.names=c("user", "item", "rating", "timestamp"))
    message("loaded ", nrow(test), " rows of test data")
    # we should have 100 ratings
    assert_that(are_equal(nrow(test), 100))
    message("reading train set ", i)
    # we should have a bunch of test ratings
    train = read.csv(sprintf("build/crossfold.out/part%02d.train.csv", i),
                     header=FALSE, col.names=c("user", "item", "rating", "timestamp"))
    assert_that(are_equal(nrow(test) + nrow(train), nrow(ratings)))
    overlap = merge(test, train)
    assert_that(are_equal(nrow(overlap), 0))
    test
}))

# we should have 500 ratings
assert_that(are_equal(nrow(test_data), 500))