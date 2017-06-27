package com.erpam.mert;

import com.erpam.mert.TWEC.ClusterEvaluator;
import com.erpam.mert.TWEC.TweetClusteringTool;
import com.erpam.mert.TWEC.TweetPreprocessor;
import com.erpam.mert.TWEC.model.Tweet;
import com.utils.Utility;

import java.util.ArrayList;

public class Application {

    /**
     * Entry point of Tweet Clustering Application
     */
    public static void main(String[] args) {

        String[] fileNames = {"combined"};
        float[] thresholds = {0.3f, 0.4f, 0.5f, 0.6f, 0.7f};

        String clusterDirectory = "clusters/";
        String evaluationDirectory = "evaluations/";

        for (String filename : fileNames) {
            for (float threshold : thresholds) {

                ArrayList<Tweet> tweets = Utility.readStream(Utility.getStream(filename + ".txt"));

                long startTime = System.nanoTime();

                TweetPreprocessor tweetPreprocessor = new TweetPreprocessor(0.6f, tweets);
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
                evaluator.printEvaluationResults("evaluations/");
                evaluator.printSummary(evaluationDirectory, clusterTime);
            }
        }
    }

}
