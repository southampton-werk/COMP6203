import genius.core.actions.Action;

/**
 * Acceptance strategy for Agent Smith
 * If the opponent is predicted to offer more utility in future then this agent will not accept
 * as there is a possibility it could gain more utility later
 * Otherwise, there is no point continuing so it accepts the lastest offer
 * Any bid needs to be above a threshold to be accepted
 * If the negotiation is nearing the end, the last offer if accepted to avoid a utility of 0
 */
public class AgentSmithAcceptanceStrategy {

    private AgentSmith agent;

    /**
     * Constructor to pass reference to the agent
     * @param agent - the reference to AgentSmith
     */
    public AgentSmithAcceptanceStrategy(AgentSmith agent) {
        this.agent = agent;
    }

    /**
     * A method to return if the agent should accept the current offer
     *
     * No bid below the threshold is accepted and
     * if the negotiation is near the end the last offer is accepted
     * @return true = accept, false = make new offer
     */
    public boolean accept() {
        // TODO: write accept method
        return false;
    }
}
