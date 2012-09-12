# Create a chart comparing the algorithms

library("ggplot2")
library("grid")

all.data <- read.csv("eval-results.csv")

all.agg <- aggregate(cbind(RMSE.ByRating, RMSE.ByUser, nDCG)
                     ~ Algorithm,
                     data=all.data, mean)

rmse.both <- rbind(
  data.frame(Algorithm=all.agg$Algorithm, RMSE=all.agg$RMSE.ByRating,
             mode="Global"),
  data.frame(Algorithm=all.agg$Algorithm, RMSE=all.agg$RMSE.ByUser,
             mode="Per-User"))

chart.rmse <- qplot(RMSE, Algorithm, data=rmse.both, shape=mode, color=mode)

chart.ndcg <- qplot(nDCG, Algorithm, data=all.agg)

chart.build <- ggplot(all.data, aes(Algorithm, BuildTime / 1000)) +
  geom_boxplot() +
  ylab("Build time (seconds)")

chart.test <- ggplot(all.data, aes(Algorithm, TestTime / 1000)) +
  geom_boxplot() +
  ylab("Test time (seconds)")

print("Outputting to accuracy.pdf")
pdf("accuracy.pdf", paper="letter", width=0, height=0)
error.layout <- grid.layout(nrow=2, heights=unit(0.5, "npc"))
pushViewport(viewport(layout=error.layout, layout.pos.col=1))
print(chart.rmse, vp=viewport(layout.pos.row=1))
print(chart.ndcg, vp=viewport(layout.pos.row=2))
popViewport()
dev.off()

print("Outputting to speed.pdf")
pdf("speed.pdf", paper="letter", width=0, height=0)
times.layout <- grid.layout(nrow=2, heights=unit(0.5, "npc"))
pushViewport(viewport(layout=times.layout, layout.pos.col=1))
print(chart.build, vp=viewport(layout.pos.row=1))
print(chart.test, vp=viewport(layout.pos.row=2))
popViewport()
dev.off()
