package group14;

import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.Domain;
import genius.core.utility.UtilitySpace;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to generate the estimated Nash point given the current negotiation domain, the estimated utility space
 * for Agent Smith, and the estimated opponent model
 */
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

        public void setOpponentUtility(double utility) {
            this.opponentUtility = utility;
        }
    }

    private Domain domain;
    private UtilitySpace agentUtilitySpace;
    private AgentSmithOpponentModel opponentModel;
    private List<BidPoint> bidSpace; // All bid points
    private List<BidPoint> paretoFrontier;
    private BidPoint nashPoint;
    private static final int ITERATION_LIMIT = 25000; // Maximum number of iterations before it cuts off
    private boolean bidSpaceUpdated;

    /**
     * A constructor to set the attributes needed to calculate the nash point
     * @param domain the domain of the negotiation
     * @param agentUtilitySpace the utility space for Agent Smith - this should not change after initially estimated
     * @param opponentModel the opponent model for the opponent - this will change during the negotiation
     */
    public NashPointGenerator(Domain domain, UtilitySpace agentUtilitySpace, AgentSmithOpponentModel opponentModel) {
        this.domain = domain;
        this.agentUtilitySpace = agentUtilitySpace;
        this.opponentModel = opponentModel;
        paretoFrontier = new ArrayList<BidPoint>();
    }

    /**
     * A method to create all the bid points
     */
    private void createBidSpace() {
        bidSpace = new ArrayList<BidPoint>();
        BidIterator iterator = new BidIterator(domain);
        int iterations = 0;

        while (iterator.hasNext() && iterations < ITERATION_LIMIT) {
            Bid bid = iterator.next();
            bidSpace.add(new BidPoint(bid, agentUtilitySpace.getUtility(bid), opponentModel.opponentBidUtility(bid)));
            iterations++;
        }
        bidSpaceUpdated = true;
    }

    /**
     * A method to update the bid space
     * This should be called whenever the opponent utility space is modified
     * @param opponentModel the estimated model for the opponent
     */
    public void updateBidSpace(AgentSmithOpponentModel opponentModel) {
        this.opponentModel = opponentModel;
        if (bidSpace == null) {
            createBidSpace();
        } else {
            for (BidPoint bp : bidSpace) {
                bp.setOpponentUtility(this.opponentModel.opponentBidUtility(bp.getBid()));
            }
        }
        bidSpaceUpdated = true;
    }

    /**
     * A method to get the bid at the nash point
     * @return the nash point bid
     */
    public Bid getNashPoint(){
        // Only compute if bid space has been updated since last computation or no Nash point exists
        if (bidSpaceUpdated || nashPoint == null) {
            bidSpaceUpdated = false;

            // Calculate pareto frontier by finding strictly dominating bids
            List<BidPoint> frontierPointsToRemove = new ArrayList<BidPoint>();
            for (BidPoint bp : bidSpace) {
                if (paretoFrontier.isEmpty()) {
                    paretoFrontier.add(bp);
                }
                for (BidPoint f : paretoFrontier) {
                    if (f.isDominatedBy(bp)) {
                        frontierPointsToRemove.add(f);
                    }
                }
                // A bid has been dominated - the old one needs removing and the new one adding
                if (!frontierPointsToRemove.isEmpty()) {
                    paretoFrontier.removeAll(frontierPointsToRemove);
                    if (!paretoFrontier.contains(bp)) {
                        paretoFrontier.add(bp);
                    }
                    // Reset points to remove
                    frontierPointsToRemove = new ArrayList<BidPoint>();
                }
            }

            double maxUtilityProduct = -1;
            double currentUtilityProduct = 0;
            // Loop through pareto frontier and find Nash point
            for (BidPoint bp : paretoFrontier) {
                currentUtilityProduct = bp.getUtilityProduct();
                if (currentUtilityProduct > maxUtilityProduct) {
                    nashPoint = bp;
                    maxUtilityProduct = currentUtilityProduct;
                }
            }
        }

        if (nashPoint == null) {
            System.out.println("Nash point was null");
        } else {
            System.out.println("Nash bid returned: " + nashPoint.getBid() + " with utility " + nashPoint.getAgentUtility());
        }
        return nashPoint == null ? null : nashPoint.getBid();
    }

    /**
     * A method to get the Agent Smith's utility at the Nash point
     * @return agent's utility at Nash point
     */
    public double getNashUtility() {
        return nashPoint.getAgentUtility();
    }
    // TODO: Generalise so can use these methods to draw graph later if needed
}
