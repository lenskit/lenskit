library('assertthat')
message("reading test items")
results = read.csv("user-items.csv")
message("read ", nrow(results), " results")
results.agg = aggregate(TargetItem ~ Algorithm + Partition + User, results, length)
# all users should have 5 results
stopifnot(all(results.agg$TargetItem == 5))

gres = read.csv("results.csv")
rpagg = aggregate(RecipRank ~ Algorithm + Partition, results, mean)
res.mrr = rpagg$RecipRank[order(rpagg$Partition)]
assert_that(are_equal(res.mrr, gres$MRR[order(gres$Partition)]))