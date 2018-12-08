package group14;

import genius.core.Bid;

import java.util.ArrayList;
import java.util.List;

/**
 * Bidding strategy for Agent Smith - a cooperative agent
 * It always aims for nash point / pareto optimal bids and is orientated to a win-win result
 * It does this by estimating the bid at the Nash point and offering that to the opponent
 * It always starts by offering the bid with the best utility for itself
 * to give let the opponent model become more accurate hence allowing a better estimation of the Nash point
 * It does not interrupt modelling attempts of others i.e. no random bids
 */
public class AgentSmithBiddingStrategy {

    private Agent14 agent;
    // all deadlines must add to less than 1!
    private double modellingDeadline = 0.1; // Deadline for initial opponent modelling to stop
    private double nashOfferDeadline = modellingDeadline + 0.6; // Deadline for Nash computed bids to stop
    private double closestToNashDeadline = nashOfferDeadline + 0.2; // Deadline for calculating closest to Nash to stop
    private NashPointGenerator nashPointGenerator;
    private List<Bid> alreadyOffered;

    /**
     * Constructor to pass reference to the agent
     * @param agent - the reference to group14.Agent14
     */
    public AgentSmithBiddingStrategy(Agent14 agent) {
        this.agent = agent;
        nashPointGenerator = new NashPointGenerator(agent.getDomain(), agent.getUtilitySpace(), agent.getOpponentModel());
        alreadyOffered = new ArrayList<Bid>();
    }

    /**
     * Method to get the initial bid of the agent (i.e. at time=0)
     * Always starts with best bid (highest utility for itself)
     * @return initial bid
     */
    private Bid getInitialBid() {
        try {
            return agent.getUtilitySpace().getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to get the next bid the agent should make
     * Uses it's estimated utility threshold to find bids and
     * generates a list of bids above that threshold to get all bids with best utility for itself
     * It then picks the most likely to have greatest utility for opponent using the opponent model
     * @return next bid
     */
    private Bid getNashBid() {
        // Assumes opponent model updated with every offer
        nashPointGenerator.updateBidSpace(agent.getOpponentModel());
        return nashPointGenerator.getNashPoint();
    }

    private Bid getNextBid() {
        // Slowly lowering utility threshold
        agent.setUtilityThreshold(agent.getUtilityThreshold() * 0.95);

        List<Bid> orderedBids = agent.getUserModel().getBidRanking().getBidOrder();
        Bid closestToNash = null;
        double closestDistance = 1; // Max distance possible
        // Bids are ranked from lowest to highest utility, want to start with highest
        for (int i = orderedBids.size()-1; i >=0; i--) {
            Bid b = orderedBids.get(i);
            // Only consider those bids above the threshold
            if (agent.getUtility(b) > agent.getUtilityThreshold()) {
                double distanceToNash = nashPointGenerator.distanceToNash(b);
                // If distance is -1, no Nash point exists
                // If the distance is 0, this bid is the Nash point (which has already been offered before)
                if (distanceToNash != -1 && distanceToNash != 0
                        && !alreadyOffered.contains(b) // trying to avoid sending the same bid again if it wasn't accepted the first time
                        && distanceToNash < closestDistance) {
                    closestDistance = distanceToNash;
                    closestToNash = b;
                }
            }
        }
        // If no bid returned - all offered already, try sending Nash bid again
        return closestToNash == null ? getNashBid() : closestToNash;
    }

    /**
     * A method to get the bid the agent should make which is dependent on time.
     * @return the bid the agent should offer
     */
    public Bid getBid() {
        double time = agent.getTimeLine().getTime();
        Bid returnBid = null;
        // First bid made by this agent
        // The agent is stubborn for the first 10% of the time, offering only it's initial bid
        // This is so the agent has a chance to generate a good model of the opponent (no discount factor)
        if ((agent.getLastReceivedOffer() == null || agent.getMyLastOffer() == null) && time < modellingDeadline) {
           returnBid = getInitialBid();
        } else if (time < nashOfferDeadline) {
            returnBid = getNashBid();
            // Utility threshold updated to the last bid offered
            // for the majority of the time, this will be the bid at the nash point
            // This means the agent will not accept anything with utility lower than at the Nash point
            agent.setUtilityThreshold(nashPointGenerator.getNashUtility());
        } else if (time < closestToNashDeadline) {
            // Finished offering Nash point so now lower utility threshold and offer bids closest to the Nash point
            returnBid = getNextBid();
        } else if (time < 1){
            // Otherwise must be in the last stretch of the negotiation
            returnBid = agent.getBestOfferSoFar();
        }
        alreadyOffered.add(returnBid);
        return returnBid;
    }
}
