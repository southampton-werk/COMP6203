import genius.core.Bid;

/**
 * Bidding strategy for Agent Smith - a cooperative agent
 * It always aims for nash point / pareto optimal bids and is orientated to a win-win result
 * It does this by finding the most likely bids to have greatest utility for the opponent
 * and chooses the one with the best utility for itself
 * Only bids above a threshold will be offered (which decreases over time)
 * It always starts by offering the bid with the best utility for itself
 * It does not interrupt modelling attempts of others i.e. no random bids
 */
public class AgentSmithBiddingStrategy {

    private AgentSmith agent;

    /**
     * Constructor to pass reference to the agent
     * @param agent
     */
    public AgentSmithBiddingStrategy(AgentSmith agent) {
        this.agent = agent;
    }

    /**
     * Method to get the initial bid of the agent (i.e. at time=0)
     * Always starts with best bid (highest utility for itself)
     * @return initial bid
     */
    public Bid getInitialBid() {
        return null;
    }

    /**
     * Method to get the next bid the agent should make
     * Uses it's estimated utility threshold to find bids and
     * generates a list of bids most likely to have greatest utility for opponent using the opponent model
     * It then pick best utility for itself from list
     * @return next bid
     */
    public Bid getNextBid() {
        return null;
    }
}
