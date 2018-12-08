package group14;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.utility.AbstractUtilitySpace;

import java.util.List;
import java.util.Random;

/**
 * TODO: Update Agent Smith description
 * Cooperative but stubborn
 * Slow to concede - initial high threshold
 * Offers it's maximum utility bid for first few rounds to allow time to generate a good opponent model
 * After this, the Nash point is estimated using the agent's estimated utility and opponent model
 * The minimum utility threshold is then set to just below the Nash point
 *
 * Utility estimator -
 * Bidding strategy -
 * Opponent model -
 * Acceptance strategy -
 */
public class Agent14 extends AbstractNegotiationParty {
    private final String description = "Group 14 - Agent Smith";

    private AgentSmithBiddingStrategy biddingStrategy;
    private AgentSmithOpponentModel opponentModel;
    private AgentSmithAcceptanceStrategy acceptanceStrategy;

    private Bid lastReceivedOffer; // Current offer on the table
    private Bid myLastOffer; // Latest offer made by the agent
    private double utilityThreshold;
    // Initial idea - Threshold decreased linearly to begin with then try exponential
    // Trying to set threshold to Nash point instead

    private Bid bestOfferSoFar = null; // Best bid offered so far from opponent

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        utilitySpace = estimateUtilitySpace();
        //evaluateEstimatedUtilitySpace();
        // This is where the utility estimation is done - at the start only
        // Rank bids here - time limit?

        try {
            // Setting utility threshold as high as possible to begin with
            // This means it's unlikely any bids will be accepted to begin with and gives the agent
            // a chance to model the opponent to find the Nash point
            utilityThreshold = this.getUtility(this.getUtilitySpace().getMaxUtilityBid());
        } catch (Exception e) {
            e.printStackTrace();
            // Random high threshold set if cannot access maximum possible utility
            utilityThreshold = 0.95;
        }

        opponentModel = new AgentSmithOpponentModel(this.getDomain());
        acceptanceStrategy = new AgentSmithAcceptanceStrategy(this);
        biddingStrategy = new AgentSmithBiddingStrategy(this);
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        // Using Stacked Alternating Offers Protocol so only actions are Accept, Offer and EndNegotiation
        // EndNegotiation not used - Reservation value is zero so our agent prefers to accept any deal rather than end the negotiation

        if (acceptanceStrategy.accept(lastReceivedOffer)) {
            System.out.println("Accepted at: " + getTimeLine().getTime());
            return new Accept(this.getPartyId(), lastReceivedOffer);
        } else {
            try {
                System.out.println("Starting to send new bid: " + getTimeLine().getTime());
                myLastOffer = biddingStrategy.getBid();
            } catch(Exception e) {
                e.printStackTrace();
                // Fallback in case exception occurred getting bid, always offer something
                myLastOffer = generateRandomBid();
            }
            // Double fallback in case no exception when generating bid but bid returned still null, always offer something
            if (myLastOffer == null) {
                myLastOffer = generateRandomBid();
            }
            System.out.println("Bid sent at: " + getTimeLine().getTime());
            return new Offer(this.getPartyId(), myLastOffer);
        }
    }

    @Override
    public AbstractUtilitySpace estimateUtilitySpace() {
        Domain domain = getDomain();
        AgentSmithUtilityEstimator factory = new AgentSmithUtilityEstimator(domain);
        BidRanking bidRanking = userModel.getBidRanking();
        factory.estimateUsingBidRanks(bidRanking);
        AbstractUtilitySpace us = factory.getUtilitySpace();

        return us;
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;
            // storing last received offer
            lastReceivedOffer = offer.getBid();
            System.out.println("Bid received at: " + getTimeLine().getTime());
            opponentModel.recievedBid(offer.getBid());
            System.out.println("Opponent model finished updating at: " + getTimeLine().getTime());
            // Storing the best bid offered by the opponent (i.e. the one with highest utility for us)
            if (bestOfferSoFar == null) {
                bestOfferSoFar = lastReceivedOffer;
            } else {
                if (this.getUtility(lastReceivedOffer) > this.getUtility(bestOfferSoFar)) {
                    bestOfferSoFar = lastReceivedOffer;
                }
            }
        }
    }

    /**
     * A human-readable description for this party.
     * @return agent description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Get the current utility threshold of the agent
     * @return utility threshold
     */
    public double getUtilityThreshold() {
        return utilityThreshold;
    }

    /**
     * Get the last offer the agent received
     * @return last received offer
     */
    public Bid getLastReceivedOffer() {
        return lastReceivedOffer;
    }

    /**
     * Get the last offer this agent made
     * @return the agent's last offer
     */
    public Bid getMyLastOffer() {
        return myLastOffer;
    }

    /**
     * Get the best offer made so far from the opponent
     * @return opponent's best offer so far
     */
    public Bid getBestOfferSoFar() {
        return bestOfferSoFar;
    }

    /**
     * Set the utility threshold of this agent
     * @param threshold new utility threshold
     */
    public void setUtilityThreshold(double threshold) {
        this.utilityThreshold = threshold;
    }

    /**
     * Get the model for the agent's opponent
     * @return opponent model
     */
    public AgentSmithOpponentModel getOpponentModel() {
        return opponentModel;
    }

    private void evaluateEstimatedUtilitySpace(){
        AbstractUtilitySpace ours = utilitySpace;
        AbstractUtilitySpace real = ((ExperimentalUserModel) userModel).getRealUtilitySpace();
        System.out.println("Ours: \n" + ours);
        System.out.println("Real: \n" + real);

        try {
            System.out.println("Our Best: " + ours.getMaxUtilityBid());
            System.out.println("Real Best: " + real.getMaxUtilityBid());
            System.out.println("Our Worst: " + ours.getMinUtilityBid());
            System.out.println("Real Worst: " + real.getMinUtilityBid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ours,real");
        for (int i = 0; i < 10000; i++) {
            Bid randomBid =  getDomain().getRandomBid(new Random());
            System.out.println(real.getUtility(randomBid) + "," + ours.getUtility(randomBid));
        }
    }
}
