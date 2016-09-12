message("Loading predictions")
predictions = read.csv("predictions.csv")

message("Pivoting prediction frame")
# we should just reshape, but this is broken in Renjin
preds.wide = reshape(predictions[c("User", "Item", "Algorithm", "Prediction")],
                     timevar="Algorithm", idvar=c("User", "Item"),
                     direction="wide")

message("Checking predictions")
pred.range = abs(preds.wide$Prediction.Standard - preds.wide$Prediction.Normalizing)
bad.preds = pred.range > 0.001
nbad = sum(bad.preds, na.rm=TRUE)

if (nbad > 0) {
    print(head(subset(preds.wide, bad.preds)))
    stop("item-item had ", nbad, " bad predictions")
} else {
    message("Tests passed!")
}
