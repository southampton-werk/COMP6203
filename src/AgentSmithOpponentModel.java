import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Objective;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;

public class AgentSmithOpponentModel {

	Domain d;
	Bid[] listOfBids;


	public void orderOfOptions()
	{

		List<Issue> issueList = d.getIssues();
		Integer[][] counter = new Integer[issueList.size()][];
		HashMap<Integer,ValueDiscrete> valueIndex = new HashMap<Integer,ValueDiscrete>();

		//create counter
		for(Integer i = 0; i < issueList.size(); i ++)
		{
			IssueDiscrete id = (IssueDiscrete) issueList.get(i);
			counter[i] = new Integer[id.getNumberOfValues()];
			for(Integer p= 0; p < id.getNumberOfValues(); p ++)
			{
				valueIndex.put(p, id.getValue(p));
				counter[i][p] = 0;
			}
		}
		//find freq
		for(int i = 0; i< issueList.size(); i ++)
		{
			for(int p = 0; p < counter[i].length; p ++)
			{
				for(Bid b: listOfBids)
				{
					ValueDiscrete bidValue = (ValueDiscrete) b.getValue(i);
					if(bidValue.equals(valueIndex.get(p)))
					{
						counter[i][p] ++;
					}


				}
			}
		}

		Integer[][] optionOrder = new Integer[issueList.size()][];
		
		
		
		for(int i = 0; i< issueList.size(); i ++)
		{
			ArrayIndexComparator comparator = new ArrayIndexComparator(counter[i]);
			Integer[] indexes = comparator.createIndexArray();
			Arrays.sort(indexes, comparator);	
			
			for(int p = 0; p < indexes.length;p ++)
			{
				indexes[p] = ((indexes.length - indexes[p] + 1)/indexes.length);
			}
			
			optionOrder[i] = indexes;
		}



	}

	public class ArrayIndexComparator implements Comparator<Integer>
	{
		private final Integer[] array;

		public ArrayIndexComparator(Integer[] array)
		{
			this.array = array;
		}

		public Integer[] createIndexArray()
		{
			Integer[] indexes = new Integer[array.length];
			for (int i = 0; i < array.length; i++)
			{
				indexes[i] = i; // Autoboxing
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2)
		{
			// Autounbox from Integer to int to use as array indexes
			return array[index1].compareTo(array[index2]);
		}
	}
}
