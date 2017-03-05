stopifnot(file.exists("results.csv"))
stopifnot(file.exists("users.csv"))

results = read.csv("results.csv")
stopifnot(nrow(results) == 2)
stopifnot(length(intersect(names(results), c("MRR", "MAP"))) == 2)
stopifnot(length(intersect(results$Partition, c(1,2))) == 2)
stopifnot(all(results$MRR > 0))
stopifnot(all(results$MAP > 0))