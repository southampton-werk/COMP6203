import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.ISSUETYPE;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.IssueInteger;
import genius.core.issue.IssueReal;
import genius.core.issue.Objective;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.issue.ValueInteger;
import genius.core.issue.ValueReal;

public class AgentSmithOpponentModel {

	Domain d;
	Bid[] listOfBids;

	Integer[][] emptyCounter;
	Double[][] optionOrder;
	Integer[][] counter;
	Double[] issueWeights;
	HashMap<Integer,String> discreteValueIndex = new HashMap<Integer,String>();
	HashMap<Integer,Double> realValueIndex = new HashMap<Integer,Double>();
	HashMap<Integer,Integer> integerValueIndex = new HashMap<Integer,Integer>();


	public void createIndexAndCounter()
	{
		List<Issue> issueList = d.getIssues();
		emptyCounter = new Integer[issueList.size()][];
		discreteValueIndex = new HashMap<Integer,String>();
		realValueIndex = new HashMap<Integer,Double>();
		integerValueIndex = new HashMap<Integer,Integer>();

		//create counter
		for(Integer i = 0; i < issueList.size(); i ++)
		{
			Issue iss = issueList.get(i);
			if(iss.getType().equals(ISSUETYPE.DISCRETE))
			{
				IssueDiscrete id = (IssueDiscrete) issueList.get(i);
				emptyCounter[i] = new Integer[id.getNumberOfValues()];
				for(Integer p= 0; p < id.getNumberOfValues(); p ++)
				{
					discreteValueIndex.put(p, id.getValue(p).getValue());
					emptyCounter[i][p] = 0;
				}
			}
			//TODO what if real number really small or really large
			else if(iss.getType().equals(ISSUETYPE.REAL))
			{
				IssueReal id = (IssueReal) issueList.get(i);
				emptyCounter[i] = new Integer[(int) (id.getUpperBound() - id.getLowerBound())];
				for(Integer p= 0; p < id.getNumber(); p ++)
				{
					realValueIndex.put(p, id.getLowerBound() +p);
					emptyCounter[i][p] = 0;
				}	
			}
			else if(iss.getType().equals(ISSUETYPE.INTEGER))
			{
				IssueInteger id = (IssueInteger) issueList.get(i);
				emptyCounter[i] = new Integer[id.getUpperBound() - id.getLowerBound()];
				for(Integer p= 0; p < id.getNumber(); p ++)
				{
					integerValueIndex.put(p, id.getLowerBound() + p);
					emptyCounter[i][p] = 0;
				}	
			}

		}
	}

	public void findFrequency()
	{
		Integer[][] counter = emptyCounter.clone();
		//find freq
		for(int i = 0; i< counter.length; i ++)
		{
			for(int p = 0; p < counter[i].length; p ++)
			{
				for(Bid b: listOfBids)
				{

					Value bidValue = b.getValue(i);
					if(bidValue.getType().equals(ISSUETYPE.DISCRETE))
					{
						ValueDiscrete vd = (ValueDiscrete) bidValue;
						if(vd.getValue().equals(discreteValueIndex.get(p)))
						{
							counter[i][p] ++;
						}
					}
					else if(bidValue.getType().equals(ISSUETYPE.REAL))
					{
						ValueReal vr = (ValueReal) bidValue;
						if(vr.getValue() == realValueIndex.get(p))
						{
							counter[i][p] ++;
						}
					}
					else if(bidValue.getType().equals(ISSUETYPE.INTEGER))
					{
						ValueInteger vi = (ValueInteger) bidValue;
						if(vi.getValue() == integerValueIndex.get(p))
						{
							counter[i][p] ++;
						}
					}




				}
			}
		}
	}

	public void orderOfOptions()
	{

		optionOrder = new Double[ counter.length][];
		for(int i = 0; i<  counter.length; i ++)
		{
			ArrayIndexComparator comparator = new ArrayIndexComparator(counter[i]);
			Integer[] indexes = comparator.createIndexArray();
			Arrays.sort(indexes, comparator);	

			for(int p = 0; p < indexes.length;p ++)
			{
				Double d = (double) (indexes.length - indexes[p] + 1);
				d /= (double) indexes.length;
				optionOrder[i][p] = d;
			}

		}



	}

	public void issueWeights()
	{
		Double[] unnormalisedIssueWeights = new Double[counter.length];

		double sumWeights = 0;
		for(int i = 0; i < counter.length;i ++)
		{
			
			for(int p = 0; p < counter[i].length; p ++)
			{
				unnormalisedIssueWeights[i] += (double) (counter[i][p] ^ 2) / (listOfBids.length ^ 2);
			}
			
			sumWeights += unnormalisedIssueWeights[i];
		}
		
		for(int i = 0; i < counter.length;i ++)
		{
			issueWeights[i] = unnormalisedIssueWeights[i] / sumWeights;
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
