# Create a chart comparing the algorithms

library("doBy")
library("lattice")

all.data <- read.csv("eval-output.csv")

all.agg <- summaryBy(MAE + RMSE.ByRating + RMSE.ByUser
                     + nDCG ~ Algorithm,
                     all.data)

err.global <- data.frame(Algorithm=all.agg$Algorithm,
                         RMSE=all.agg$RMSE.ByRating.mean,
                         mode="Global")

err.user <- data.frame(Algorithm=all.agg$Algorithm,
                       RMSE=all.agg$RMSE.ByUser.mean,
                       mode="Per User")

err.bound <- rbind(err.global, err.user)

chart.mae <- barchart(MAE.mean ~ Algorithm, all.agg,
                      ylab="MAE")

chart.rmse <- barchart(RMSE ~ Algorithm, err.bound,
                       groups=mode, auto.key=TRUE,
                       ylab="RMSE")

chart.ndcg <- barchart(nDCG.mean ~ Algorithm, all.agg,
                       ylab="nDCG")

chart.build <- bwplot(BuildTime ~ Algorithm, all.data,
                      main="Build times",
                      ylab="Time (seconds)")

chart.test <- bwplot(TestTime ~ Algorithm, all.data,
                     main="Test times",
                     ylab="Time (seconds)")

print("Outputting to error.pdf")
pdf("error.pdf", paper="letter", width=0, height=0)
print(chart.mae, position=c(0,0.6666,1,1), more=TRUE)
print(chart.rmse, position=c(0,0.3333,1,0.6666), more=TRUE)
print(chart.ndcg, position=c(0,0,1,0.3333))
dev.off()

print("Outputting to times.pdf")
pdf("times.pdf", paper="letter", width=0, height=0)
print(chart.build, position=c(0,0.5,1,1), more=TRUE)
print(chart.test, position=c(0,0,1,0.5))
dev.off()

pdf("rmse.pdf", width=4, height=3)
barchart(RMSE ~ Algorithm, err.bound,
         groups=mode, auto.key=TRUE,
         ylab="RMSE", scales=list(x=list(rot=45)))
dev.off()
