package agents.market.behaviors.auctionCreation;

import agents.market.Market;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.messaging.TopicUtility;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.FishMarketProtocol;

public class MarketFSMBehaviour extends FSMBehaviour {
	
	private ACLMessage _request;
	
	public static final AID MESSAGE_STEP = TopicUtility.createTopic(FishMarketProtocol.steps.STEP_AUCTION_CREATION);
	
	public static final MessageTemplate MESSAGE_FILTER = MessageTemplate.MatchTopic(MarketFSMBehaviour.MESSAGE_STEP);
	
	/** All the following states are relating to a auction creation **/
		public static final String STATE_WAIT_AUCTION_CREATION_REQUEST  = 
				"STATE_WAIT_AUCTION_CREATION_REQUEST";
		public static final String STATE_PROCESS_CREATION_REQUEST =
				"STATE_PROCESS_CREATION_REQUEST";
		public static final String STATE_ACCEPT_AUCTION_CREATION = 
				"STATE_ACCEPT_AUCTION_CREATION";
		public static final String STATE_FINISH_AUCTION_CREATION = 
				"STATE_FINISH_AUCTION_CREATION";

	
	/** Transitions **/
	private static final int TRANSITION_TO_WAIT = 0;
	private static final int TRANSITION_TO_PROCESS = 1;
	private static final int TRANSITION_TO_ACCEPT = 2;
	private static final int TRANSITION_TO_FINISH = 3;
	
	public MarketFSMBehaviour() {
		super();
	}
	public MarketFSMBehaviour(Market marketAgent) {
		super(marketAgent);
		
		/** States registration **/
		this.registerFirstState(new MarketOneShotBehaviour(marketAgent, this,STATE_WAIT_AUCTION_CREATION_REQUEST),
				STATE_WAIT_AUCTION_CREATION_REQUEST);
		this.registerState(new MarketOneShotBehaviour(marketAgent,this,STATE_PROCESS_CREATION_REQUEST),
				STATE_PROCESS_CREATION_REQUEST);
		this.registerState(new MarketOneShotBehaviour(marketAgent, this,STATE_ACCEPT_AUCTION_CREATION),
				STATE_ACCEPT_AUCTION_CREATION);
		this.registerLastState(new MarketOneShotBehaviour(marketAgent, this,STATE_FINISH_AUCTION_CREATION),
				STATE_FINISH_AUCTION_CREATION);
		
		/** Transitions registration **/
		this.registerDefaultTransition
			(STATE_ACCEPT_AUCTION_CREATION,STATE_WAIT_AUCTION_CREATION_REQUEST);
		this.registerTransition
			(STATE_WAIT_AUCTION_CREATION_REQUEST,STATE_WAIT_AUCTION_CREATION_REQUEST,TRANSITION_TO_WAIT);
		this.registerTransition
			(STATE_PROCESS_CREATION_REQUEST,STATE_WAIT_AUCTION_CREATION_REQUEST,TRANSITION_TO_WAIT);
		this.registerTransition
			(STATE_WAIT_AUCTION_CREATION_REQUEST,STATE_PROCESS_CREATION_REQUEST,TRANSITION_TO_PROCESS);
		this.registerTransition
			(STATE_PROCESS_CREATION_REQUEST,STATE_ACCEPT_AUCTION_CREATION,TRANSITION_TO_ACCEPT);
		this.registerTransition
			(STATE_WAIT_AUCTION_CREATION_REQUEST,STATE_FINISH_AUCTION_CREATION,TRANSITION_TO_FINISH);
	}
	
	public ACLMessage getRequest() {
		return _request;
	}
	
	public void setRequest(ACLMessage request) {
		this._request = request;
	}	
}
