rating_file = as.character(cmdArgs[1])
message("reading ", rating_file, "\r")

ratings = read.delim(rating_file, header=FALSE, col.names=c("user", "item", "rating", "timestamp"))
n = nrow(ratings)
purchases = data.frame(id=1:n, user=ratings$user, item=ratings$item, timestamp=ratings$timestamp)

test1 = vector("logical", n)
test2 = vector("logical", n)
test1[sample(1:n, 5000)] = TRUE
test2[sample(1:n, 5000)] = TRUE

message("writing data\r")
write.table(purchases[test1,], file="bookmark-test-1.csv", sep=",", row.names=FALSE)
write.table(purchases[test2,], file="bookmark-test-2.csv", sep=",", row.names=FALSE)
write.table(purchases[!test1,], file="bookmark-train-1.csv", sep=",", row.names=FALSE)
write.table(purchases[!test2,], file="bookmark-train-2.csv", sep=",", row.names=FALSE)
