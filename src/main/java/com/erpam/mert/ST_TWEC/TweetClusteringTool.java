package com.erpam.mert.ST_TWEC;

import com.abahgat.suffixtree.Node;
import com.abahgat.suffixtree.SuffixTree;
import com.erpam.mert.ST_TWEC.model.Cluster;
import com.erpam.mert.ST_TWEC.model.ClusterElement;
import com.erpam.mert.ST_TWEC.model.Tweet;
import com.utils.Utility;

import java.io.Serializable;
import java.util.*;

public class TweetClusteringTool implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Cluster> clusters;
    private float lengthBurstRatio = 0.8f;
    private float mergeRatio = 0.6f;
    private int[] occurrenceMask;
    private float sizeBurstRatio = 1.2f;
    private SuffixTree tree;
    private List<Tweet> tweets;

    public TweetClusteringTool(List<Tweet> tweets) {
        this.tweets = tweets;
        this.tree = new SuffixTree();
    }

    /**
     * Prepares the suffix tree for clustering, makes the following operations in the order:
     * {@link #buildSuffixTree()}
     * <p>
     * {@link #createNodeArray()}
     * <p>
     * {@link #populateTree()}
     * <p>
     * {@link #determineDuplicateNodes()}
     */

    public void prepareSuffixTree() {
        buildSuffixTree();
        createNodeArray();
        populateTree();
        determineDuplicateNodes();
    }


    public void createClusters(float clusterRatio) {
        long startTime = System.nanoTime();
        clusters = tree.createClusters(tweets, clusterRatio);
        long endTime = System.nanoTime();
        System.out.println("Clusters are created in  " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    public void clearClusterFlags() {
        tree.clearFlags();
    }

    public void leaveOverlapping() {
        long startTime = System.nanoTime();

        Collections.sort(clusters);

        occurrenceMask = new int[tweets.size()];
        for (int i = 0; i < occurrenceMask.length; i++) {
            occurrenceMask[i] = -1;
        }
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            for (int j = 0; j < cluster.getElements().size(); j++) {
                ClusterElement element = cluster.getElements().get(j);

                for (int index : element.getIndexSet()) {
                    occurrenceMask[index] = i;
                }

            }
        }

        long endTime = System.nanoTime();
        System.out.println("Clusters are sorted and overlapping is removed in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    public void removeOverlappingAndMergeLinear() {
        long startTime = System.nanoTime();

        occurrenceMask = new int[tweets.size()];
        for (int i = 0; i < occurrenceMask.length; i++) {
            occurrenceMask[i] = -1;
        }

        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            for (ClusterElement element : cluster.getElements()) {
                for (int index : element.getIndexSet()) {
                    if (occurrenceMask[index] == -1) {
                        occurrenceMask[index] = i;
                    } else {
                        int id = occurrenceMask[index];
                        if (clusters.get(id).getTweetSize() <= clusters.get(i).getTweetSize()) {
                            occurrenceMask[index] = i;
                        }
                    }
                }
            }
        }

        HashSet<Integer> removedIndices = new HashSet<Integer>();

        for (int i = 0; i < clusters.size(); i++) {
            HashMap<Integer, Integer> indexMap = new HashMap<>();
            Cluster cluster = clusters.get(i);
            int tweetSize = cluster.getTweetSize();
            for (int j = 0; j < cluster.getElements().size(); j++) {
                ClusterElement element = cluster.getElements().get(j);

                boolean duplicateElement = false;
                for (int index : element.getIndexSet()) {
                    if (occurrenceMask[index] != -1 && occurrenceMask[index] != i) {
                        int clusterIndex = occurrenceMask[index];
                        Integer count = indexMap.get(clusterIndex);
                        if (count == null)
                            count = element.getIndexSet().size();
                        else
                            count += element.getIndexSet().size();
                        indexMap.put(clusterIndex, count);
                        duplicateElement = true;
                        break;
                    }
                }

                if (duplicateElement) {
                    cluster.setTweetSize(cluster.getTweetSize() - element.getTweetSize());
                    cluster.getElements().remove(j);
                    j--;
                }
            }

            int maxClusterIndex = 0;
            int maxCount = 0;
            for (Integer clusterIndex : indexMap.keySet()) {
                int currentCount = indexMap.get(clusterIndex);
                if (currentCount > maxCount) {
                    maxCount = currentCount;
                    maxClusterIndex = clusterIndex;
                }
            }

            if (maxCount > tweetSize * mergeRatio) {
                for (ClusterElement clusterElement : cluster.getElements()) {
                    clusters.get(maxClusterIndex).addElement(clusterElement);
                }

                removedIndices.add(i);
            } else if (cluster.getTweetSize() < 2) {
                for (int j = 0; j < cluster.getElements().size(); j++) {
                    ClusterElement element = cluster.getElements().get(j);
                    for (int index : element.getIndexSet()) {
                        occurrenceMask[index] = -1;
                    }
                }

                removedIndices.add(i);
            }
        }

        ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
        for (int i = 0; i < clusters.size(); i++) {
            if (!removedIndices.contains(i))
                newClusters.add(clusters.get(i));
        }
        clusters = newClusters;
        Collections.sort(clusters);

        long endTime = System.nanoTime();
        System.out.println("Clusters are sorted and overlapping is removed in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    public void removeOverlappingAndMerge() {
        long startTime = System.nanoTime();

        Collections.sort(clusters);

        occurrenceMask = new int[tweets.size()];
        for (int i = 0; i < occurrenceMask.length; i++) {
            occurrenceMask[i] = -1;
        }

        for (int i = 0; i < clusters.size(); i++) {
            HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
            Cluster cluster = clusters.get(i);
            int tweetSize = cluster.getTweetSize();
            for (int j = 0; j < cluster.getElements().size(); j++) {
                ClusterElement element = cluster.getElements().get(j);

                boolean duplicateElement = false;
                for (int index : element.getIndexSet()) {
                    if (occurrenceMask[index] != -1) {
                        int clusterIndex = occurrenceMask[index];
                        Integer count = indexMap.get(clusterIndex);
                        if (count == null)
                            count = element.getIndexSet().size();
                        else
                            count += element.getIndexSet().size();
                        indexMap.put(clusterIndex, count);
                        duplicateElement = true;
                        break;
                    }
                }

                if (duplicateElement) {
                    cluster.setTweetSize(cluster.getTweetSize() - element.getTweetSize());
                    cluster.getElements().remove(j);
                    j--;
                } else {
                    for (int index : element.getIndexSet()) {
                        occurrenceMask[index] = i;
                    }
                }
            }

            int maxClusterIndex = 0;
            int maxCount = 0;
            for (Integer clusterIndex : indexMap.keySet()) {
                int currentCount = indexMap.get(clusterIndex);
                if (currentCount > maxCount) {
                    maxCount = currentCount;
                    maxClusterIndex = clusterIndex;
                }
            }

            if (maxCount > tweetSize * mergeRatio) {
                for (ClusterElement clusterElement : cluster.getElements()) {
                    clusters.get(maxClusterIndex).addElement(clusterElement);
                }

                clusters.remove(i);
                i--;
            } else if (cluster.getTweetSize() < 2) {
                for (int j = 0; j < cluster.getElements().size(); j++) {
                    ClusterElement element = cluster.getElements().get(j);
                    for (int index : element.getIndexSet()) {
                        occurrenceMask[index] = -1;
                    }
                }
                clusters.remove(i);
                i--;
            }
        }

        Collections.sort(clusters);
        long endTime = System.nanoTime();
        System.out.println("Clusters are sorted and overlapping is removed in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    public void createAndExtendLabels() {
        long startTime = System.nanoTime();
        int size = clusters.size();
        for (int i = 0; i < size; i++) {
            Cluster c = clusters.get(i);

            c.setLabel();
            c.extendLabel(tweets);
        }

        long endTime = System.nanoTime();
        System.out.println("Cluster labels are assigned and extended in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }


    public List<Node> getNodes() {
        return this.tree.getNodes();
    }


    public ArrayList<Cluster> getClusters() {
        return clusters;
    }

    public int[] getOccurrenceMask() {
        return occurrenceMask;
    }

    public float getLengthBurstRatio() {
        return lengthBurstRatio;
    }

    public void setLengthBurstRatio(float lengthBurstRatio) {
        this.lengthBurstRatio = lengthBurstRatio;
    }

    public float getSizeBurstRatio() {
        return sizeBurstRatio;
    }

    public void setSizeBurstRatio(float sizeBurstRatio) {
        this.sizeBurstRatio = sizeBurstRatio;
    }

    public float getMergeRatio() {
        return mergeRatio;
    }

    public void setMergeRatio(float mergeRatio) {
        this.mergeRatio = mergeRatio;
    }

    /**
     * Builds the suffix tree by inserting tweets into the tree
     * The id of each tweet is determined by the order they are inserted to the tree
     */
    void buildSuffixTree() {
        long startTime = System.nanoTime();
        for (int i = 0; i < tweets.size(); i++) {
            Tweet tweet = tweets.get(i);
            tweet.setSuffixTreeId(i);
            tree.put(tweet.getProcessedTweet(), tweet.getSuffixTreeId());
        }
        long endTime = System.nanoTime();
        System.out.println("Tweets are added to the suffix tree in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    /**
     * Creates a node array from the nodes of the suffix tree
     * The node array is constructed by breadth-first traversal of suffix tree
     */
    void createNodeArray() {
        long startTime = System.nanoTime();
        tree.createNodeArray();
        long endTime = System.nanoTime();
        System.out.println("Node array is created in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    /**
     * Determines and marks nodes which contain similar tweets and/or represent similar strings
     * The duplication is determined by observing the relation between a parent/suffix- child node
     * A child node is a duplicate of its parent/suffix, if the increase ratio of size and lengthfrom child to parent is not big enough {@link #sizeBurstRatio, {@link #lengthBurstRatio}}
     */
    void determineDuplicateNodes() {
        long startTime = System.nanoTime();
        tree.determineDuplicateNodes(sizeBurstRatio, lengthBurstRatio);
        long endTime = System.nanoTime();
        System.out.println("Duplicate nodes are determined in  " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }

    /**
     * Calculates and stores the number of tweets which goes through at each node
     */
    void populateTree() {
        long startTime = System.nanoTime();
        tree.populateIndexSet();
        long endTime = System.nanoTime();
        System.out.println("Index sets in nodes are populated in " + Utility.convertElapsedTime(startTime, endTime) + " secs");
    }
}