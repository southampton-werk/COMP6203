import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.DomainImpl;
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
import genius.core.representative.UncertainUtilitySpace;

public class AgentSmithOpponentModel {

	Domain d;
	Bid[] listOfBids;
	Integer[][] emptyCounter;
	Double[][] optionOrder;
	Integer[][] counter;
	Double[] issueWeights;
	HashMap<ArrayList<Integer>,String> discreteValueIndex = new HashMap<ArrayList<Integer>,String>();
	HashMap<Integer,Integer> issueEvaluator = new HashMap<Integer,Integer>();


	public static void main(String[] args) throws IOException {

		DomainImpl d = new DomainImpl("domaintest.xml");

		Bid[] listOfBids = new Bid[3];

		for(int i = 0; i < 3; i ++)
		{

			listOfBids[i] = d.getRandomBid(new Random());


		}

		AgentSmithOpponentModel om = new AgentSmithOpponentModel();
		om.d = d;
		om.listOfBids = listOfBids;
		om.createIndexAndCounter();
		om.findFrequency();

		om.orderOfOptions();
		om.issueWeights();

		Double[][] optionOrder = om.optionOrder;
		Double[] issueWeights = om.issueWeights;

		for(int i = 0; i < optionOrder.length; i ++)
		{
			System.out.println("Issue:"  +i);
			System.out.println("Issue Weight" + issueWeights[i]);
			for(int p = 0; p < optionOrder[i].length; p ++)
			{
				System.out.println("Value:" + p);
				System.out.println("Order of Option:" + optionOrder[i][p]);
			}
			System.out.println("");
		}


	}

	public Double opponentBidUtility(Bid b)
	{

		Double utility = 0.0;


		return utility;
	}

	public void createIndexAndCounter()
	{
		List<Issue> issueList = d.getIssues();
		emptyCounter = new Integer[issueList.size()][];
		discreteValueIndex = new HashMap<ArrayList<Integer>,String>();
		issueEvaluator = new HashMap<Integer,Integer>();

		ArrayList<Integer> valueKey;

		//create counter
		for(Integer i = 0; i < issueList.size(); i ++)
		{
			Issue iss = issueList.get(i);
			issueEvaluator.put(i, iss.getNumber());

			IssueDiscrete id = (IssueDiscrete) issueList.get(i);
			emptyCounter[i] = new Integer[id.getNumberOfValues()];
			for(Integer p= 0; p < id.getNumberOfValues(); p ++)
			{
				valueKey = new ArrayList<Integer>();
				valueKey.add(i);
				valueKey.add(p);
				discreteValueIndex.put(valueKey, id.getValue(p).getValue());
				emptyCounter[i][p] = 0;
			}
		}


	}


	public void findFrequency()
	{

		counter = emptyCounter.clone();
		//find freq

		ArrayList<Integer> valueKey;

		for(int i = 0; i< counter.length; i ++)
		{
			for(int p = 0; p < counter[i].length; p ++)
			{
				for(Bid b: listOfBids)
				{
					//TODO hashmap from issue to its evaluator
					Value bidValue = b.getValue(issueEvaluator.get(i));

					ValueDiscrete vd = (ValueDiscrete) bidValue;
					valueKey = new ArrayList<Integer>();
					valueKey.add(i);
					valueKey.add(p);
					if(vd.getValue().equals(discreteValueIndex.get(valueKey)))
					{
						counter[i][p] ++;
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

			optionOrder[i] = new Double[indexes.length];
			for(int p = 0; p < indexes.length;p ++)
			{
				Double d = (double) (indexes.length - indexes[p] );
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
			unnormalisedIssueWeights[i] = 0.0;
			for(int p = 0; p < counter[i].length; p ++)
			{

				unnormalisedIssueWeights[i] += (double) (counter[i][p] ^ 2) / (listOfBids.length ^ 2);
			}

			sumWeights += unnormalisedIssueWeights[i];
		}


		issueWeights = new Double[counter.length];
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
