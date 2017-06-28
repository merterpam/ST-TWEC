package com.erpam.mert;

import com.erpam.mert.ST_TWEC.ClusterEvaluator;
import com.erpam.mert.ST_TWEC.TweetClusteringTool;
import com.erpam.mert.ST_TWEC.TweetPreprocessor;
import com.erpam.mert.ST_TWEC.model.Tweet;
import com.utils.Utility;

import java.util.ArrayList;

public class SampleApplication {

    /**
     * Entry point of Tweet Clustering SampleApplication
     */
    public static void main(String[] args) {

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
    }
}
