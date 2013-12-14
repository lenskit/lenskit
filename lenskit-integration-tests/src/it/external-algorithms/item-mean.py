import sys

trainfile, testfile, outfile = sys.argv[1:3]

global_sum = 0
global_count = 0
item_sums = {}
item_counts = {}
with open(trainfile) as f:
    for line in f:
        user, item, rating = line.split(',')[:3]
        rating = float(rating)
        global_sum += rating
        global_count += 1
        if item not in item_sums:
            item_sums[item] = rating
            item_counts[item] = 1
        else:
            item_sums[item] += rating
            item_counts[item] += 1

global_mean = global_sum / global_count
item_means = {}
for item in item_sums.iterkeys():
    n = item_counts[item]
    item_means[item] = item_sums[item] / n - global_mean

with open(outfile, 'w') as outf:
    with open(testfile) as testf:
        for line in testf:
            user, item = line.split(',')[:2]
            pred = global_mean
            if item in item_means:
                pred += item_means[item]
            print >>outf, "%s,%s,%.3f" % (user, item, pred)