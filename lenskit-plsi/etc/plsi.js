// Generic PLSI with no EM tempering
rec.name = "PLSI with Untempered EM"
rec.module = org.grouplens.lenskit.plsi.PLSIModule
rec.module.core.iterations = 100
rec.module.core.onlineIterations = 5
rec.module.core.features = 100
rec.module.core.learningDampingRate = 1.0