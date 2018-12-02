import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.utility.AbstractUtilitySpace;

/**
 * TODO: Update Agent Smith description
 * Cooperative but stubborn
 * Slow to concede - initial high threshold, lowered slowly
 * The threshold will never fall below a set limit
 *
 * Utility estimator -
 * Bidding strategy -
 * Opponent model -
 * Acceptance strategy -
 */
public class AgentSmith extends AbstractNegotiationParty {
    private final String description = "Agent Smith";

    private AgentSmithBiddingStrategy biddingStrategy;
    private AgentSmithOpponentModel opponentModel;
    private AgentSmithAcceptanceStrategy acceptanceStrategy;

    private Bid lastReceivedOffer; // Current offer on the table
    private Bid myLastOffer; // Latest offer made by the agent
    private double utilityThreshold; // TODO: Set utility threshold and descrease over time
    // Threshold decreased linearly to begin with then try exponential

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        biddingStrategy = new AgentSmithBiddingStrategy(this);
        opponentModel = new AgentSmithOpponentModel();
        acceptanceStrategy = new AgentSmithAcceptanceStrategy(this);
        utilitySpace = estimateUtilitySpace();
        //evaluateEstimatedUtilitySpace();
        // This is where the utility estimation is done - at the start only
        // Rank bids here - time limit?
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

        if (acceptanceStrategy.accept()) {
            return new Accept(this.getPartyId(), lastReceivedOffer);
        } else {
            // First bid made by this agent
            if (lastReceivedOffer == null || myLastOffer == null) {
                myLastOffer = biddingStrategy.getInitialBid();
            } else {
                myLastOffer = biddingStrategy.getNextBid();
            }
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
        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
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
