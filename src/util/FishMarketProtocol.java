package util;

import jade.lang.acl.ACLMessage;

public class FishMarketProtocol {
	
    public static class Performatives {
		public static final int TO_CREATE = ACLMessage.REQUEST;
		public static final int TO_ANNOUNCE = ACLMessage.CFP;
		public static final int TO_ATTRIBUTE = ACLMessage.ACCEPT_PROPOSAL;
		public static final int TO_GIVE = ACLMessage.AGREE;
		public static final int REP_BID = ACLMessage.INFORM;
		public static final int TO_BID = ACLMessage.PROPOSE;
		public static final int TO_PAY = ACLMessage.CONFIRM;
		public static final int TO_SUBSCRIBE = ACLMessage.SUBSCRIBE;
		public static final int AUCTION_OVER = ACLMessage.CANCEL;
		public static final int TO_CANCEL = ACLMessage.FAILURE;
		public static final int TO_WITHDRAW = ACLMessage.REFUSE;
		public static final int TO_REQUEST = ACLMessage.REQUEST;
		public static final int TO_PROVIDE = ACLMessage.INFORM;
		public static final int TO_ACCEPT = ACLMessage.CONFIRM;
		public static final int TO_REFUSE = ACLMessage.REFUSE;
    }
    
    public static class steps {
	public static final String STEP_AUCTION_CREATION = FishMarketProtocol.steps.class.getName()
		+ ":STEP_AUCTION_CREATION";
	public static final String STEP_BIDDERS_SUBSCRIPTION = FishMarketProtocol.steps.class.getName()
		+ ":STEP_BIDDERS_SUBSCRIPTION";
	public static final String STEP_AUCTION_IN_PROGRESS = FishMarketProtocol.steps.class.getName()
		+ ":STEP_AUCTION_IN_PROGRESS";
    }
}
