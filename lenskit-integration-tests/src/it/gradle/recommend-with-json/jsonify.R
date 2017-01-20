library(rjson)

items = read.csv("ml100k/movies.csv")

outf = file("movies.json")
for (i in 1:nrow(items)) {
    js = list(id=items[i,1], name=items[i,2])
    cat(sprintf("%s\n", toJSON(js)), file=outf)
}
close(outf)