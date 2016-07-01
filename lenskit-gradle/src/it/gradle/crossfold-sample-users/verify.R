for (i in 1:5) {
    message("checking test set ", i)
    test = read.csv(sprintf("build/crossfold.out/part%02d.test.csv", i),
                    header=FALSE, col.names=c("user", "item", "rating", "timestamp"))
    message("loaded ", nrow(test), " rows of test data")
    u.ntest = aggregate(test$item, list(test$user), length)
    message("aggregated data for ", nrow(u.ntest), " users")
    # sample users
    stopifnot(nrow(u.ntest) == 10)
    # and each user should have 5s
    stopifnot(all(u.ntest$x == 5))
}
