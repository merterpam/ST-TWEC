package com.utils.sortedlist;

import java.util.ArrayList;

public class SortedList<E extends Comparable<E>> extends ArrayList<E>{

	/**
	 * 
	 */
	private int limitSize;
	private static final long serialVersionUID = 1L;

	public SortedList(int limit)
	{
		super();
		limitSize = limit;
	}

	@Override
	public boolean add(E item)
	{
		boolean addedToList = false;
		if(this.size() == 0)
		{
			addedToList = super.add(item);
		}
		else
		{
			for(int i = 0; i < this.size(); i++)
			{
				if(item.compareTo(this.get(i)) > 0)
				{
					super.add(i, item);
					addedToList = true;
					break;
				}
			}
			if(!addedToList && this.size() < limitSize)
			{
				addedToList = super.add(item);
			}
		}

		if(this.size() > limitSize)
		{
			this.remove(this.size()-1);
		}
		return addedToList;
	}

}
