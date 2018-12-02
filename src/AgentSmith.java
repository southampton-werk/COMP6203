import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

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
public class AgentSmith extends AbstractNegotiationParty {
    private final String description = "Agent Smith";

    private AgentSmithBiddingStrategy biddingStrategy;
    private AgentSmithOpponentModel opponentModel;
    private AgentSmithAcceptanceStrategy acceptanceStrategy;
    private AgentSmithUtilityEstimator utilityEstimator;

    private Bid lastReceivedOffer; // Current offer on the table
    private Bid myLastOffer; // Latest offer made by the agent
    private double utilityThreshold; // TODO: Set utility threshold and decrease over time
    // Threshold decreased linearly to begin with then try exponential

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        biddingStrategy = new AgentSmithBiddingStrategy(this);
        opponentModel = new AgentSmithOpponentModel();
        acceptanceStrategy = new AgentSmithAcceptanceStrategy(this);
        utilityEstimator = new AgentSmithUtilityEstimator();

        // This is where the utility estimation is done - at the start only
        // Rank bids here - time limit?

        try {
            utilityThreshold = this.getUtilitySpace().getUtility(this.getUtilitySpace().getMaxUtilityBid());
        } catch (Exception e) {
            e.printStackTrace();
            // Random high threshold set if cannot access maximum possible utility
            utilityThreshold = 0.95;
        }
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
            // The agent is stubborn for the first 10% of the time, offering only it's initial bid
            // This is so the agent has a chance to generate a good model of the opponent (no discount factor)
            if ((lastReceivedOffer == null || myLastOffer == null) && getTimeLine().getTime() < 0.1) {
                myLastOffer = biddingStrategy.getInitialBid();
                if (myLastOffer == null) {
                    // Fallback in case exception occurred getting initial bid, always offer something
                    myLastOffer = generateRandomBid();
                }
            } else {
                myLastOffer = biddingStrategy.getNextBid();
            }
            return new Offer(this.getPartyId(), myLastOffer);
        }
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
            // TODO: store best offer received so far
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

}
