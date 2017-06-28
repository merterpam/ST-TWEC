package com.abahgat.suffixtree;


import com.erpam.mert.ST_TWEC.model.Cluster;
import com.erpam.mert.ST_TWEC.model.Tweet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SuffixTree extends GeneralizedSuffixTree implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2346660886748884744L;
    private ArrayList<Node> nodes = null;

    public SuffixTree() {
        super();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    /**
     * Puts the nodes in the tree in an Array by traversing breadth-first
     */
    public void createNodeArray() {
        if (nodes == null) {
            nodes = new ArrayList<>();
            nodes.add(root);

            for (int i = 0; i < nodes.size(); i++) {
                for (Edge e : nodes.get(i).getEdges().values()) {
                    e.setDestNodeId(nodes.size());
                    nodes.add(e.getDest());
                }
            }
        }

    }

    public void populateIndexSet() {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            HashSet<Integer> ret = new HashSet<>();
            for (int num : node.getNodeData()) {
                ret.add(num);
            }

            for (Edge e : node.getEdges().values()) {
                for (int num : e.getDest().getIndexSet()) {
                    ret.add(num);
                }
                e.getDest().setIndexSet(null);
            }

            int totalCount = ret.size();
            node.setIndexSize(totalCount);
            node.setIndexSet(new int[totalCount]);
            int index = 0;
            for (int num : ret) {
                node.getIndexSet()[index++] = num;
            }
        }
    }

    public void determineDuplicateNodes(float indexSetRatio, float substringRatio) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            int indexSetSize = node.getIndexSize();
            int maximumSetSize = (int) (indexSetSize * indexSetRatio);

            Node suffix = node.getSuffix();
            while (suffix != null && !suffix.isSuffixDuplicate() && suffix.getIndexSize() < maximumSetSize) {
                suffix.setSuffixDuplicate(true);
                suffix = suffix.getSuffix();
            }

            if (node.getSuffixRatioLength() == -1)
                node.setSuffixRatioLength(node.getSubstringLength());

            while (suffix != null && suffix.isSuffixDuplicate())
                suffix = suffix.getSuffix();

            if (suffix != null && suffix.getSubstringLength() > node.getSuffixRatioLength() * substringRatio) {
                node.setSuffixDuplicate(true);
                suffix.setSuffixRatioLength(Math.max(suffix.getSuffixRatioLength(), node.getSuffixRatioLength()));
            }


            Node prefix = node.getSourceNode();
            while (prefix != null && !prefix.isPrefixDuplicate() && prefix.getIndexSize() < maximumSetSize) {
                prefix.setPrefixDuplicate(true);
                prefix = prefix.getSourceNode();
            }

            if (node.getPrefixRatioLength() == -1)
                node.setPrefixRatioLength(node.getSubstringLength());

            while (prefix != null && prefix.isPrefixDuplicate())
                prefix = prefix.getSourceNode();

            if (prefix != null && prefix.getSubstringLength() > node.getPrefixRatioLength() * substringRatio) {
                node.setPrefixDuplicate(true);
                prefix.setPrefixRatioLength(Math.max(prefix.getPrefixRatioLength(), node.getPrefixRatioLength()));
            }
        }

    }

    public ArrayList<Cluster> createClusters(List<Tweet> tweets, float clusterRatio) {
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (Node node : nodes) {
            if ((node.getSourceNode() != null && node.getSourceNode().inACluster) || (node.getSuffix() != null && node.getSuffix().inACluster)) {
                node.inACluster = true;
            }
            if (!node.inACluster && (!node.isPrefixDuplicate() || !node.isSuffixDuplicate())) {
                int nodeLength = node.getSubstringLength();
                if (!node.isPrefixDuplicate())
                    nodeLength = Math.max(nodeLength, node.getPrefixRatioLength());
                if (!node.isSuffixDuplicate())
                    nodeLength = Math.max(nodeLength, node.getSuffixRatioLength());

                Cluster cluster = new Cluster();
                HashSet<Integer> indexSet = node.fetchIndexSet();
                for (int index : indexSet) {
                    Tweet tweet = tweets.get(index);
                    int minimumLength = (int) (tweet.getProcessedTweet().length() * clusterRatio);
                    if (nodeLength > minimumLength) {
                        if (cluster.getTweetSize() == 0) {
                            cluster.setSampleTweetId(index);
                        }

                        cluster.addTweet(tweets.get(index));
                    }
                }
                if (cluster.getTweetSize() > 1) {
                    cluster.setNode(node);
                    clusters.add(cluster);
                    if (cluster.getTweetSize() > node.getIndexSize() - 2) {
                        node.inACluster = true;
                    }
                }
            }
        }
        return clusters;
    }

    public void clearFlags() {
        for (Node node : nodes) {
            node.inACluster = false;
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        for (Node node : nodes) {
            for (Edge e : node.getEdges().values()) {
                e.setDest(nodes.get(e.getDestNodeId()));
            }
        }
    }
}
