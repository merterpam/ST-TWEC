package com.abahgat.suffixtree;

public class ScoredPair implements Comparable<ScoredPair>{

	public double lcsScore;
	public int firstIndex;
	public int secondIndex;
	
	
	public ScoredPair(double score, int firstIndex, int secondIndex) {
		this.lcsScore = score;
		this.firstIndex = firstIndex;
		this.secondIndex = secondIndex;
	}


	@Override
	public int compareTo(ScoredPair rhs) {
		double result = this.lcsScore - rhs.lcsScore;
		if(result < 0)
			return -1;
		else if(result > 0)
			return 1;
		else
			return 0;
	}

}
