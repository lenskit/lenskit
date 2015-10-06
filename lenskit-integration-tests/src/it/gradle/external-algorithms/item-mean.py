import sys


class ItemMeanData(object):
    def __init__(self):
        self.global_sum = 0
        self.global_count = 0
        self.item_sums = {}
        self.item_counts = {}

    def train(self, trainfile):
        with open(trainfile) as f:
            for line in f:
                user, item, rating = line.strip().split(',')[:3]
                item = int(item)
                rating = float(rating)
                self.global_sum += rating
                self.global_count += 1
                if item not in self.item_sums:
                    self.item_sums[item] = rating
                    self.item_counts[item] = 1
                else:
                    self.item_sums[item] += rating
                    self.item_counts[item] += 1

    def global_mean(self):
        return self.global_sum / self.global_count

    def item_set(self):
        return set(self.item_counts.iterkeys())

    def item_mean_offsets(self):
        means = {}
        gmean = self.global_mean()
        for item, n in self.item_counts.iteritems():
            means[item] = self.item_sums[item] / n - gmean
        return gmean, means

    def score_items(self, to_score, output):
        global_mean, item_means = self.item_mean_offsets()
        for user, items in to_score.iteritems():
            for item in items:
                pred = global_mean
                if item in item_means:
                    pred += item_means[item]
                print >> output, "%s,%s,%.3f" % (user, item, pred)


def load_test_pairs(testfile):
    to_score = {}
    with open(testfile) as testf:
        for line in testf:
            user, item = line.strip().split(',')[:2]
            user = int(user)
            item = int(item)
            if user in to_score:
                to_score[user].add(item)
            else:
                to_score[user] = set([item])
    return to_score


def load_query_users(userfile, items):
    to_score = {}
    with open(userfile) as userf:
        for line in userf:
            user = int(line.strip())
            to_score[user] = items
    return to_score


if sys.argv[1] == '--for-users':
    userfile, trainfile = sys.argv[2:4]
    testfile = None
    outfile = None
else:
    trainfile, testfile, outfile = sys.argv[1:4]
    userfile = None

model = ItemMeanData()
model.train(trainfile)

if testfile is not None:
    to_score = load_test_pairs(testfile)
elif userfile is not None:
    to_score = load_query_users(userfile, model.item_set())
else:
    print >> sys.stderr, "no user file specified"
    sys.exit(1)

if outfile is None:
    model.score_items(to_score, sys.stdout)
else:
    with open(outfile, 'w') as outf:
        model.score_items(to_score, outf)
