import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class UtilityEstimator extends AdditiveUtilitySpaceFactory {

    private Domain domain;

    /**
     * Generates an simple Utility Space on the domain, with equal weights and zero values.
     * Everything is zero-filled to already have all keys contained in the utility maps.
     *
     * @param d
     */
    public UtilityEstimator(Domain d) {
        super(d);
        domain = d;
    }

    @Override
    public void estimateUsingBidRanks(BidRanking r) {
        HashMap<Issue, HashMap<ValueDiscrete, List<Integer>>> issueValues = new HashMap<>();

        int position = 0;
        for(Bid b : r.getBidOrder()){
            position++;
            List<Issue> issues = b.getIssues();
            for(Issue i: issues) {
                Integer issueNo = i.getNumber();

                ValueDiscrete v = (ValueDiscrete) b.getValue(issueNo);
                HashMap<ValueDiscrete, List<Integer>> valuePositions = issueValues.computeIfAbsent(i, k -> new HashMap<>()); // TODO: Confirm equality works here
                List<Integer> positionList = valuePositions.computeIfAbsent(v, k -> new ArrayList<>());
                positionList.add(position);
            }
        }

        HashMap<Issue, HashMap<ValueDiscrete, Double>> means = new HashMap<>();
        HashMap<Issue, HashMap<ValueDiscrete, Double>> stddevs = new HashMap<>();
        for(Issue issue: issueValues.keySet()){
            HashMap<ValueDiscrete, List<Integer>> valuePositions = issueValues.get(issue);
            for(ValueDiscrete value : valuePositions.keySet()){
                List<Integer> positions  = valuePositions.get(value);
                double mean = positions.stream().mapToInt(e -> e).average().orElseThrow(() -> new RuntimeException("Value exists with no positions for it"));
                double stddev = 0d;
                for(int v : positions){
                    stddev += (v - mean) * (v - mean);
                }
                stddev /= (double) positions.size();
                stddev = 1d / stddev; // So higher stddev = lower weighting
                mean = r.getSize() - mean;
                means.get(issue).put(value, mean);
                stddevs.get(issue).put(value, stddev);
                this.setUtility(issue, value, mean * stddev);
            }
        }
        super.normalizeWeightsByMaxValues();

    }


}
