package com.erpam.mert.TWEC.model;

import java.io.Serializable;

/**
 * Created by mert on 25/06/2017.
 */
public class Tweet implements Serializable{

    private String processedTweet;
    private int suffixTreeId;
    private String tweet;
    private String tweetLabel;

    public Tweet(String tweet, String tweetLabel) {
        this.tweet = tweet;
        this.tweetLabel = tweetLabel;
    }

    public String getProcessedTweet() {
        return processedTweet;
    }

    public void setProcessedTweet(String processedTweet) {
        this.processedTweet = processedTweet;
    }

    public int getSuffixTreeId() {
        return suffixTreeId;
    }

    public void setSuffixTreeId(int suffixTreeId) {
        this.suffixTreeId = suffixTreeId;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getTweetLabel() {
        return tweetLabel;
    }

    public void setTweetLabel(String tweetLabel) {
        this.tweetLabel = tweetLabel;
    }


    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Tweet) {
            Tweet anotherTweet = (Tweet) anObject;
            return this.getSuffixTreeId() == anotherTweet.getSuffixTreeId();
        }
        return false;
    }
}
