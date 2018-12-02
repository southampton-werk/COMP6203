import genius.core.Bid;
import genius.core.Domain;
import genius.core.utility.UtilitySpace;

import java.util.List;

public class NashPointGenerator {

    /**
     * A point for a bilateral negotiation which can be interpreted as a point on a graph of the bid space
     * Each point is associated with a bid and the utilities of each agent
     */
    private class BidPoint {

        private Bid bid;
        private double agentUtility;
        private double opponentUtility;

        /**
         * A constructor to build a bid point form a bid and the associated utilities
         *
         * @param bid             the bid to represent
         * @param agentUtility    the utility of Agent Smith for that bid
         * @param opponentUtility the utility of the opponent for that bid
         */
        public BidPoint(Bid bid, double agentUtility, double opponentUtility) {
            this.bid = bid;
            this.agentUtility = agentUtility;
            this.opponentUtility = opponentUtility;
        }

        /**
         * A method to determine is this bid point is dominated by another
         * To dominate, the other bid point must have utilities which are greater than or equal to this one
         * with at least one value strictly greater
         *
         * @param other the bid point to compare against
         * @return if this bid point is dominated by the other
         */
        public boolean isDominatedBy(BidPoint other) {
            boolean dominated = false;
            if (other != this) {
                if ((other.getAgentUtility() < getAgentUtility()) || (other.getOpponentUtility() < getOpponentUtility())) {
                    // One of the utilities is smaller
                    dominated = false;
                } else if ((other.getAgentUtility() > getAgentUtility()) || (other.getOpponentUtility() > getOpponentUtility())) {
                    // At least one utility is strictly greater
                    dominated = true;
                }
            }
            return dominated;
        }

        /**
         * Returning the product of the agent and opponent utilities for this bid point
         * If reservation value is zero for both, this can be used to calculate the Nash point
         *
         * @return the product of the utilities
         */
        public double getUtilityProduct() {
            return agentUtility * opponentUtility;
        }

        public Bid getBid() {
            return bid;
        }

        public double getAgentUtility() {
            return agentUtility;
        }

        public double getOpponentUtility() {
            return opponentUtility;
        }
    }

    private Domain domain;
    private UtilitySpace agentUtilitySpace;
    private UtilitySpace opponentUtilitySpace;
    private List<BidPoint> bidSpace; // All bid points
    private List<BidPoint> paretoFrontier;
    private BidPoint nashPoint;

    /**
     * A constructor to set the attributes needed to calculate the nash point
     * @param domain the domain of the negotiation
     * @param agentUtilitySpace the utility space for Agent Smith - this should not change after initially estimated
     * @param opponentUtilitySpace the utility space for the opponent - this will change during the negotiation
     */
    public NashPointGenerator(Domain domain, UtilitySpace agentUtilitySpace, UtilitySpace opponentUtilitySpace) {
        this.domain = domain;
        this.agentUtilitySpace = agentUtilitySpace;
        this.opponentUtilitySpace = opponentUtilitySpace;
        // TODO: create method to create bid space (all bid points)
    }

    /**
     * A method to update the bid space
     * This should be called whenever the opponent utility space is modified
     * @param opponentUtilitySpace the estimated utility space for the opponent
     */
    public void updateBidSpace(UtilitySpace opponentUtilitySpace) {
        // TODO: update bid space
    }

    /**
     * A method to get the bid at the nash point
     * @return the nash point bid
     */
    public Bid getNashPoint(){
        // TODO: finish method for getting Nash point
        // Calculate pareto frontier by finding strictly dominating bids
        // Loop through pareto frontier and find nash point
        // Generalise so can use these methods to draw graph later if needed
        return null;
    }

    // TODO: write method to access bid space / pareto frontier if wanting to draw graph later
}
