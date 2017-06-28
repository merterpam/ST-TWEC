package com.erpam.mert.ST_TWEC;

import com.erpam.mert.ST_TWEC.model.Cluster;
import com.erpam.mert.ST_TWEC.model.ClusterElement;
import com.erpam.mert.ST_TWEC.model.Tweet;
import com.utils.Utility;
import com.utils.io.Writer;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class ClusterEvaluator implements Serializable {

    private static final long serialVersionUID = 1L;

    //For evaluation
    private double avgEvaluationScore;
    private double weightedAvgEvaluationScore;
    private double avgClassLabelScore;
    private double weightedAvgClassLabelScore;
    private double avgPurity;
    private double compressionRatio;

    private List<Cluster> clusters;
    private float clusterThreshold;
    private double evaluationTime;
    private String filename;
    private Map<Integer, Tweet> noContentTweets;
    private int[] occurrenceMask;
    private List<Tweet> tweets;

    public ClusterEvaluator(TweetClusteringTool clusterTool, float clusterThreshold, String filename, List<Tweet> noContentTweets, List<Tweet> tweets) {
        this.clusters = clusterTool.getClusters();
        this.clusterThreshold = clusterThreshold;
        this.filename = filename;
        this.noContentTweets = new HashMap<>();
        this.occurrenceMask = clusterTool.getOccurrenceMask();
        this.tweets = tweets;

        for (Tweet tweet : noContentTweets) {
            this.noContentTweets.put(tweet.getSuffixTreeId(), tweet);
        }
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public void setOccurrenceMask(int[] occurrenceMask) {
        this.occurrenceMask = occurrenceMask;
    }

    public void evaluateClusters() {
        long startTime = System.nanoTime();
        int clusterSize = 0;

        for (Cluster cluster : clusters) {
            if (!cluster.isMerged()) {

                double clusterScore = 0;
                double clusterComparison = 0;
                ArrayList<ClusterElement> elements = cluster.getElements();

                HashSet<Integer> mergedIndexes = new HashSet<>();
                getMergedIndexes(mergedIndexes, cluster);
                for (int j : mergedIndexes) {
                    Cluster mC = clusters.get(j);
                    elements.addAll(mC.getElements());
                }

                HashMap<String, Integer> labelMap = new HashMap<>();
                for (int i = 0; i < elements.size(); i++) {
                    ClusterElement elementI = elements.get(i);

                    String elementLabel = elementI.getTweet().getTweetLabel();
                    if (labelMap.containsKey(elementLabel))
                        labelMap.put(elementLabel, labelMap.get(elementLabel) + elementI.getTweetSize());
                    else
                        labelMap.put(elementLabel, elementI.getTweetSize());

                    String elementITweet = elementI.getTweet().getTweet();
                    int elementICount = elementI.getTweetSize();


                    if (elementICount > 1) {
                        String lcs = getLCS(elementITweet, elementI.getTweet().getTweet());
                        double score = ((double) lcs.length()) / elementITweet.length();

                        int totalCount = elementICount * elementICount;
                        clusterScore += score * totalCount;
                        clusterComparison += totalCount;
                    }

                    for (int j = i + 1; j < elements.size(); j++) {
                        ClusterElement elementJ = elements.get(j);
                        String lcs = getLCS(elementITweet, elementJ.getTweet().getTweet());
                        double score = ((double) lcs.length() * 2) / (elementITweet.length() + elementJ.getTweet().getTweet().length());

                        int totalCount = elementICount * elementJ.getTweetSize();
                        clusterScore += score * totalCount;
                        clusterComparison += totalCount;
                    }
                }

                int maxLabelC = 0;
                for (String key : labelMap.keySet()) {
                    if (labelMap.get(key) > maxLabelC) {
                        maxLabelC = labelMap.get(key);
                    }
                }

                cluster.setPurity(((double) maxLabelC));

                double evaluationScore = clusterScore / clusterComparison;
                cluster.setEvaluationScore(evaluationScore);

                clusterSize++;
            }
        }


        double totalEvaluationScore = 0;
        double totalWeightedEvaluationScore = 0;
        double totalPurity = 0;
        int totalSize = 0;
        for (Cluster cluster : clusters) {
            totalEvaluationScore += cluster.getEvaluationScore();
            totalPurity += cluster.getPurity();
            totalWeightedEvaluationScore += cluster.getEvaluationScore() * cluster.getTweetSize();


            totalSize += cluster.getTweetSize();
        }
        avgEvaluationScore = totalEvaluationScore / clusterSize;
        avgPurity = totalPurity;
        weightedAvgEvaluationScore = totalWeightedEvaluationScore / totalSize;
        compressionRatio = ((double) clusterSize) / totalSize;

        long endTime = System.nanoTime();
        evaluationTime = Utility.convertElapsedTime(startTime, endTime);
        System.out.println("\nEvaluation is done in " + evaluationTime + " secs");
    }

    private String getLCS(String string1, String string2) {
        int M = string1.length();
        int N = string2.length();

        int[][] opt = new int[M + 1][N + 1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = M - 1; i >= 0; i--) {
            for (int j = N - 1; j >= 0; j--) {
                if (string1.charAt(i) == string2.charAt(j)) {
                    opt[i][j] = opt[i + 1][j + 1] + 1;
                } else {
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
                }
            }
        }

        // recover LCS itself and print it to standard output
        StringBuilder sb = new StringBuilder();

        int i = 0, j = 0;
        while (i < M && j < N) {
            if (string1.charAt(i) == string2.charAt(j)) {
                sb.append(string1.charAt(i));
                i++;
                j++;
            } else if (opt[i + 1][j] >= opt[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return sb.toString();
    }

    public File printClusters(String directoryPath) {

        int unclusteredTweetSize = 0;
        Writer writer = new Writer();
        writer.openWriter(directoryPath + "output_" + filename + "_" + clusterThreshold + ".txt");
        int count = 0;
        for (Cluster cluster : clusters) {
            if (!cluster.isMerged()) {
                writer.writeLine("Label: " + cluster.getLabel() + " " + cluster.getTweetSize());

                for (ClusterElement element : cluster.getElements()) {
                    writer.writeLine(element.getTweet().getTweet() + "\t" + element.getTweetSize());
                }
                count += cluster.getTweetSize();


                HashSet<Integer> mergedIndexes = new HashSet<>();
                getMergedIndexes(mergedIndexes, cluster);
                for (int j : mergedIndexes) {
                    Cluster mC = clusters.get(j);
                    for (ClusterElement element : mC.getElements()) {
                        writer.writeLine(element.getTweet().getTweet() + "\t" + element.getTweetSize());
                    }
                    count += mC.getTweetSize();
                }
                writer.writeLine("");
            }

        }

        writer.closeWriter();

        writer.openWriter(directoryPath + "unclusteredTweets_" + filename + "_" + clusterThreshold + ".txt");
        for (int i = 0; i < tweets.size(); i++) {
            if (occurrenceMask[i] == -1 && !noContentTweets.containsKey(i)) {
                unclusteredTweetSize++;
                writer.writeLine(tweets.get(i).getTweet());
            }

        }
        writer.closeWriter();
        writer.openWriter("noContent.txt");
        for (int i : noContentTweets.keySet()) {
            Tweet tweet = noContentTweets.get(i);
            writer.writeLine(tweet.getTweet());
        }
        writer.closeWriter();

        System.out.println();
        System.out.println("There are " + tweets.size() + " tweets");
        System.out.println("There are " + noContentTweets.size() + " tweets which have little/no content");
        System.out.println("There are " + unclusteredTweetSize + " unclustered tweets");
        System.out.println("There are " + clusters.size() + " clusters with more than one elements");
        System.out.println("These clusters have " + count + " elements in total \n");

        return new File(directoryPath + "output_" + filename + "_" + clusterThreshold + ".txt");
    }

    public void printSummary(String directoryPath, double clusterTime) {
        Writer writer = new Writer();
        writer.openWriter(directoryPath + "evaluation_summary" + filename + "_" + clusterThreshold + ".txt");

        int unclusteredTweetSize = 0;
        for (int i = 0; i < tweets.size(); i++) {
            if (occurrenceMask[i] == -1 && !noContentTweets.containsKey(i)) {
                unclusteredTweetSize++;
            }

        }

        int clusteredTweetSize = (tweets.size() - noContentTweets.size() - unclusteredTweetSize);
        avgPurity /= clusteredTweetSize;

        writer.writeLine("There are " + tweets.size() + " tweets");
        writer.writeLine("There are " + noContentTweets.size() + " tweets which have little/no content");
        writer.writeLine("There are " + unclusteredTweetSize + " unclustered tweets");
        writer.writeLine("There are " + clusters.size() + " clusters with more than one elements");
        writer.writeLine("These clusters have " + clusteredTweetSize + " elements in total \n");

        writer.writeLine("Average: " + avgEvaluationScore);
        writer.writeLine("Weighted Average: " + weightedAvgEvaluationScore);
        writer.writeLine("Average Class: " + avgClassLabelScore);
        writer.writeLine("Weighted Average Class: " + weightedAvgClassLabelScore);
        writer.writeLine("Average Purity: " + avgPurity);
        writer.writeLine("Compression Ratio: " + compressionRatio);
        writer.writeLine("Cluster Time: " + clusterTime);
        writer.writeLine("Evaluation Time: " + evaluationTime);

        writer.writeLine("Threshold	NumberOfClusters	NumberOfUnclusteredTweets	AverageSim	WeightedAverageSim	AveragePurity	TimeForClusteringInSeconds	TimeForEvalInSeconds");
        writer.writeLine(clusterThreshold + "	" + clusters.size() + "	" + unclusteredTweetSize + "	" + avgEvaluationScore + "	" + weightedAvgEvaluationScore + "	" + avgPurity + "	" + clusterTime + "	" + evaluationTime);
        writer.closeWriter();
    }

    public File printEvaluationResults(String directoryPath) {
        Writer writer = new Writer();
        writer.openWriter(directoryPath + "evaluation_" + filename + "_" + clusterThreshold + ".txt");
        for (Cluster cluster : clusters) {
            if (!cluster.isMerged()) {
                writer.writeLine(cluster.getLabel() + "\t"
                        + cluster.getTweetSize() + "\t"
                        + cluster.getEvaluationScore());
            }
        }

        writer.writeLine("Average: " + avgEvaluationScore);
        writer.writeLine("Weighted Average: " + weightedAvgEvaluationScore);
        writer.writeLine("Average Class: " + avgClassLabelScore);
        writer.writeLine("Weighted Average Class: " + weightedAvgClassLabelScore);
        writer.writeLine("Compression Ratio: " + compressionRatio);
        writer.closeWriter();

        return new File(directoryPath + "evaluation_" + filename + "_" + clusterThreshold + ".txt");
    }

    private void getMergedIndexes(HashSet<Integer> mergedIndexes, Cluster c) {
        for (int i : c.getMergedClusterIndexes()) {
            if (!clusters.get(i).isMerged()) {
                mergedIndexes.add(i);
                clusters.get(i).setMerged(true);
                getMergedIndexes(mergedIndexes, clusters.get(i));
            }
        }
    }

}
