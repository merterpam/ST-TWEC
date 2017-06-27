package com.erpam.mert.TWEC.model;

import java.io.Serializable;
import java.util.HashSet;

public class ClusterElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private HashSet<Integer> indexSet;
	private Tweet tweet;
	
	public ClusterElement(Tweet tweet)
	{
		setTweet(tweet);
		setIndexSet(new HashSet<Integer>());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return tweet.equals(((ClusterElement) obj).tweet);
	}

	public HashSet<Integer> getIndexSet() {
		return indexSet;
	}

	public void setIndexSet(HashSet<Integer> indexSet) {
		this.indexSet = indexSet;
	}

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}

	public int getTweetSize() {
		return getIndexSet().size();
	}
}