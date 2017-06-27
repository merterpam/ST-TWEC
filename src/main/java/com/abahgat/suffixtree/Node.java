package com.abahgat.suffixtree;
/**
 * Copyright 2012 Alessandro Bahgat Shehata
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Represents a node of the generalized suffix tree graph
 * @see GeneralizedSuffixTree
 */

public class Node implements Comparable<Node>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int[] indexSet;

	private int indexSize;
	
	private boolean suffixDuplicate = false;
	private int suffixRatioLength = -1;
	
	private boolean prefixDuplicate = false;
	private int prefixRatioLength = -1; 
	
	public boolean inACluster = false;

	//public String text; 
	private int substringLength = 0;
	private Edge sourceEdge;
	/**
	 * The payload array used to store the data (indexes) associated with this node.
	 * In this case, it is used to store all property indexes.
	 * 
	 * As it is handled, it resembles an ArrayList: when it becomes full it 
	 * is copied to another bigger array (whose size is equals to data.length +
	 * INCREMENT).
	 * 
	 * Originally it was a List<Integer> but it took too much memory, changing
	 * it to int[] take less memory because indexes are stored using native
	 * types.
	 */
	private int[] data;
	/**
	 * Represents index of the last position used in the data int[] array.
	 * 
	 * It should always be less than data.length
	 */
	private transient int lastIdx = 0;
	/**
	 * The starting size of the int[] array containing the payload
	 */
	private transient static final int START_SIZE = 0;
	/**
	 * The increment in size used when the payload array is full
	 */
	private transient static final int INCREMENT = 1;
	/**
	 * The set of edges starting from this node
	 */
	private final Map<Character, Edge> edges;
	/**
	 * The suffix link as described in Ukkonen's paper.
	 * if str is the string denoted by the path from the root to this, this.suffix
	 * is the node denoted by the path that corresponds to str without the first char.
	 */
	private Node suffix;
	/**
	 * The total number of <em>different</em> results that are stored in this
	 * node and in underlying ones (i.e. nodes that can be reached through paths
	 * starting from <tt>this</tt>.
	 * 
	 * This must be calculated explicitly using computeAndCacheCount
	 * @see Node#computeAndCacheCount() 
	 */


	/**
	 * Creates a new Node
	 */
	Node() {
		edges = new EdgeBag();
		suffix = null;
		data = new int[START_SIZE];
		//text = "";
		setSourceEdge(null);
	}

	int [] getNodeData()
	{
		return data;
	}
	/**
	 * Returns all the indexes associated to this node and its children.
	 * @return all the indexes associated to this node and its children



	/**
	 * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
	 */
	void addRef(int index) {
		if (contains(index)) {
			return;
		}

		addIndex(index);

		// add this reference to all the suffixes as well
		Node iter = this.suffix;
		while (iter != null) {
			if (iter.contains(index)) {
				break;
			}
			iter.addRef(index);
			iter = iter.suffix;
		}

	}

	/**
	 * Tests whether a node contains a reference to the given index.
	 * 
	 * <b>IMPORTANT</b>: it works because the array is sorted by construction
	 * 
	 * @param index the index to look for
	 * @return true <tt>this</tt> contains a reference to index
	 */
	private boolean contains(int index) {
		int low = 0;
		int high = lastIdx - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = data[mid];

			if (midVal < index)
				low = mid + 1;
			else if (midVal > index)
				high = mid - 1;
			else
				return true;
		}
		return false;
		// Java 5 equivalent to
		// return java.util.Arrays.binarySearch(data, 0, lastIdx, index) >= 0;
	}



	void addEdge(char ch, Edge e) {
		edges.put(ch, e);
	}

	Edge getEdge(char ch) {
		return edges.get(ch);
	}

	Map<Character, Edge> getEdges() {
		return edges;
	}

	Node getSuffix() {
		return suffix;
	}

	void setSuffix(Node suffix) {
		this.suffix = suffix;
	}

	private void addIndex(int index) {
		if (lastIdx == data.length) {
			int[] copy = new int[data.length + INCREMENT];
			System.arraycopy(data, 0, copy, 0, data.length);
			data = copy;
		}
		data[lastIdx++] = index;
	}

	public void increaseSubStringLength(int length)
	{
		substringLength += length;

	}

	public int getSubstringLength()
	{
		return substringLength;
	}


	public String getText()
	{
		char[] text = new char[substringLength];
		Edge sEdge = this.sourceEdge;
		int index = text.length-1;
		while(sEdge != null)
		{
			for(int i = sEdge.getLabel().length() -1; i >= 0; i--)
			{
				text[index] = sEdge.getLabel().charAt(i);
				index--;
			}
			sEdge = sEdge.getSource().sourceEdge;
		}
		
		return new String(text);
	}

	@Override
	public int compareTo(Node rhs) {

		return this.substringLength - rhs.substringLength;
	}

	public Edge getSourceEdge() {
		return sourceEdge;
	}

	public void setSourceEdge(Edge sourceEdge) {
		this.sourceEdge = sourceEdge;
	}
	
	public Node getSourceNode()
	{
		if(sourceEdge != null)
		{
			return sourceEdge.getSource();
		}
		return null;
	}

	public HashSet<Integer> fetchIndexSet() {
		
		HashSet<Integer> results = new HashSet<Integer>();
		
		ArrayList<Node> childNodes = new ArrayList<Node>();
		childNodes.add(this);
		for(int i = 0; i < childNodes.size(); i++)
		{
			Node childNode = childNodes.get(i);
			for (Edge e : childNode.getEdges().values()) {
				childNodes.add(e.getDest());
			}
			
			for(Integer index: childNode.data)
			{
				results.add(index);
			}
		}
		
		return results;
		
	}

	public int[] getIndexSet() {
		return indexSet;
	}

	public void setIndexSet(int[] indexSet) {
		this.indexSet = indexSet;
	}

	public int getIndexSize() {
		return indexSize;
	}

	public void setIndexSize(int indexSize) {
		this.indexSize = indexSize;
	}

	public boolean isSuffixDuplicate() {
		return suffixDuplicate;
	}

	public void setSuffixDuplicate(boolean suffixDuplicate) {
		this.suffixDuplicate = suffixDuplicate;
	}

	public int getSuffixRatioLength() {
		return suffixRatioLength;
	}

	public void setSuffixRatioLength(int suffixRatioLength) {
		this.suffixRatioLength = suffixRatioLength;
	}

	public boolean isPrefixDuplicate() {
		return prefixDuplicate;
	}

	public void setPrefixDuplicate(boolean prefixDuplicate) {
		this.prefixDuplicate = prefixDuplicate;
	}

	public int getPrefixRatioLength() {
		return prefixRatioLength;
	}

	public void setPrefixRatioLength(int prefixRatioLength) {
		this.prefixRatioLength = prefixRatioLength;
	}
	
}
