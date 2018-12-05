package group14;

import genius.core.Bid;

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
    private double modellingDeadline = 0.1; // Deadline for initial opponent modelling to stop
    private double nashOfferDeadline = 0.8; // Deadline for Nash computed bids to stop
    private NashPointGenerator nashPointGenerator;

    /**
     * Constructor to pass reference to the agent
     * @param agent - the reference to group14.Agent14
     */
    public AgentSmithBiddingStrategy(Agent14 agent) {
        this.agent = agent;
        nashPointGenerator = new NashPointGenerator(agent.getDomain(), agent.getUtilitySpace(), agent.getOpponentModel());
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
    private Bid getNextBid() {
        // TODO: Assumes opponent model updated with every offer - might need moving
        nashPointGenerator.updateBidSpace(agent.getOpponentModel());
        return nashPointGenerator.getNashPoint();
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
            returnBid = getNextBid();
            // Utility threshold updated to the last bid offered
            // for the majority of the time, this will be the bid at the nash point
            // This means the agent will not accept anything with utility lower than at the Nash point
            agent.setUtilityThreshold(nashPointGenerator.getNashUtility());
        } else if (time < 1){
            // Otherwise must be in the last stretch of the negotiation
            // TODO: Lower threshold below Nash point if opponent not accepting in time?
            returnBid = agent.getBestOfferSoFar();
        }
        return returnBid;
    }
}
