package com.erpam.mert.ST_TWEC.model;

import com.abahgat.suffixtree.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Cluster implements Comparable<Cluster>, Serializable {

    private static final long serialVersionUID = 1L;

    private boolean displayedForMerge;
    private ArrayList<ClusterElement> elements;
    private transient double evaluationScore;
    private String label;
    private boolean merged;
    private HashSet<Integer> mergedClusterIndexes;
    private transient Node node;
    private transient double purity;
    private int sampleTweetId;
    private int tweetSize;

    public Cluster() {
        setTweetSize(0);
        setMerged(false);
        setDisplayedForMerge(false);
        setElements(new ArrayList<ClusterElement>());
        setMergedClusterIndexes(new HashSet<Integer>());

    }

    public void addTweet(Tweet tweet) {
        setTweetSize(getTweetSize() + 1);

        ClusterElement element = null;
        for (int i = 0; i < getElements().size(); i++) {
            if (getElements().get(i).getTweet().equals(tweet)) {
                element = getElements().get(i);
            }
        }

        if (element == null) {
            element = new ClusterElement(tweet);
            getElements().add(element);
        }

        element.getIndexSet().add(tweet.getSuffixTreeId());

    }

    public ArrayList<ClusterElement> getElements() {
        return elements;
    }

    public void addElement(ClusterElement element) {
        if (!getElements().contains(element)) {
            getElements().add(element);
            setTweetSize(getTweetSize() + element.getTweetSize());
        }

    }

    public void extendLabel(List<Tweet> tweets) {
        String sampleTweet = tweets.get(getSampleTweetId()).getProcessedTweet();
        int firstIndex = sampleTweet.indexOf(label);
        if (firstIndex != -1) {

            int lastIndex = sampleTweet.indexOf(' ', firstIndex + label.length() - 1);
            if (lastIndex == -1)
                lastIndex = sampleTweet.length() - 1;

            firstIndex = sampleTweet.lastIndexOf(' ', firstIndex) + 1;
            try {
                label = sampleTweet.substring(firstIndex, lastIndex);
            } catch (Exception e) {
                System.out.println("Label:" + label + " tweet:" + sampleTweet);
            }
        }

    }

    public String getLabel() {
        return label;
    }

    public void setLabel() {
        this.label = this.node.getText();
    }

    public int getTweetSize() {
        return tweetSize;
    }

    public void setTweetSize(int tweetSize) {
        this.tweetSize = tweetSize;
    }

    @Override
    public int compareTo(Cluster rhs) {
        return rhs.getTweetSize() - this.getTweetSize();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public double getEvaluationScore() {
        return evaluationScore;
    }

    public void setEvaluationScore(double evaluationScore) {
        this.evaluationScore = evaluationScore;
    }

    public double getPurity() {
        return purity;
    }

    public void setPurity(double purity) {
        this.purity = purity;
    }

    public int getSampleTweetId() {
        return sampleTweetId;
    }

    public void setSampleTweetId(int sampleTweetId) {
        this.sampleTweetId = sampleTweetId;
    }

    public void setElements(ArrayList<ClusterElement> elements) {
        this.elements = elements;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isDisplayedForMerge() {
        return displayedForMerge;
    }

    public void setDisplayedForMerge(boolean displayedForMerge) {
        this.displayedForMerge = displayedForMerge;
    }

    public HashSet<Integer> getMergedClusterIndexes() {
        return mergedClusterIndexes;
    }

    public void setMergedClusterIndexes(HashSet<Integer> mergedClusterIndexes) {
        this.mergedClusterIndexes = mergedClusterIndexes;
    }
}
