# ST-TWEC
Suffix Tree based Tweet Clustering

ST-TWEC is a suffix tree based clustering algorithm for Twitter. It uses a generalized suffix tree to cluster tweets based on substring similarity. For fixed-sized documents such has tweets, ST-TWEC has a linear space and time complexity. 

ST-TWEC uses abahgat’s Suffix Tree implementation for the generalized suffix tree implementation.

ST-TWEC has three sub-modules: Tweet pre-processor, clustering tool and cluster evaluator. Each sub-module uses the output of the previous module: Clustering tool takes the pre-processed tweets from tweet pre-processor and clusters them. Cluster evaluator takes the clusters and responsible for evaluation and printing clusters and evaluation results.

# Usage

ST-TWEC requires a Tweet instance for a tweet. Tweet instances can be manually created, but it can also be automatically read from files. Utility class has a static function for this purpose. It can read tweets from a file with the following format: 

```
tweet \t tweetLabel
tweet \t tweetLabel
tweet \t tweetLabel
...
```

Tweet label is the class label a tweet belongs to and can be left empty if a tweet has no label.

For clustering tool, you need to determine a cluster threshold between 0 and 1. As the threshold nears to 1, intra-cluster similarity increases.

For evaluation, you need to specify two directories for the output of evaluations and clusters. These directories should exist in your file system, else you will have an exception.

Below is a sample usage: 

```
String clusterDirectory = "clusters/";
String evaluationDirectory = "evaluations/";

String filename = "sampleDataset.txt";
float threshold = 0.4f;

ArrayList<Tweet> tweets = Utility.readStream(Utility.getStream(filename));

long startTime = System.nanoTime();

TweetPreprocessor tweetPreprocessor = new TweetPreprocessor(tweets);
tweetPreprocessor.preProcessTweets();

TweetClusteringTool clusterTool = new TweetClusteringTool(tweets);
clusterTool.prepareSuffixTree();

clusterTool.createClusters(threshold);
clusterTool.removeOverlappingAndMerge();
clusterTool.createAndExtendLabels();

double clusterTime = Utility.convertElapsedTime(startTime, System.nanoTime());

ClusterEvaluator evaluator = new ClusterEvaluator(clusterTool, threshold, filename, tweetPreprocessor.getNoContentTweets(), tweets);
evaluator.printClusters(clusterDirectory);

evaluator.evaluateClusters();
evaluator.printEvaluationResults(evaluationDirectory);
evaluator.printSummary(evaluationDirectory, clusterTime);
```
