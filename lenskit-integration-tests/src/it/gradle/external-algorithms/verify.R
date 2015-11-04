message("Loading predictions")
predictions = read.csv("predictions.csv")

message("Pivoting prediction frame")
# we should just reshape, but this is broken in Renjin
#preds.wide = reshape(predictions[c("User", "Item", "Algorithm", "Prediction")],
#                     timevar="Algorithm", idvar=c("User", "Item"),
#                     direction="wide")
# BEGIN manual reshape
preds.wide = unique(predictions[c("User", "Item")])
algos = unique(predictions$Algorithm)
for (algo in algos) {
    algo = algos[algo]
    message("collecting predictions for ", algo)
    algo.preds = predictions[predictions$Algorithm == algo, c("User", "Item", "Prediction")]
    names(algo.preds) = c("User", "Item", paste("Prediction", as.character(algo), sep="."))
    preds.wide = merge(preds.wide, algo.preds, all.x=TRUE)
}
# END manual reshape

message("Checking predictions")
pred.range = abs(preds.wide$Prediction.External - preds.wide$Prediction.Baseline)
bad.preds = pred.range > 0.001
nbad = sum(bad.preds, na.rm=TRUE)

if (nbad > 0) {
    print(head(subset(preds.wide, bad.preds)))
    stop("item-item had ", nbad, " bad predictions")
} else {
    message("Tests passed!")
}
