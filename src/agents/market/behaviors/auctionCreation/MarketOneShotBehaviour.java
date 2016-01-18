package agents.market.behaviors.auctionCreation;

import agents.market.Market;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.FishMarketProtocol;

public class MarketOneShotBehaviour extends OneShotBehaviour {
	
	/** The main FSM behavior for the Market Agent **/
	private MarketFSMBehaviour _marketFSMBehaviour;
	
	/** the next current transition **/
	private int _transition;
	
	/** the current state provide by the main FSMBehavior **/
	private String _currentState;
	
	/** eventual incoming message filters  **/
	//private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(op1, op2);
	

	public MarketOneShotBehaviour(Market marketAgent, MarketFSMBehaviour marketFSMBehaviour,String currentState){
		super(marketAgent);
		this._marketFSMBehaviour = marketFSMBehaviour;
		this._currentState = currentState;
	}

	@Override
	public void action() {
		switch (_currentState) {
		case MarketFSMBehaviour.STATE_WAIT_AUCTION_CREATION_REQUEST :
			// do something
			break;
		case MarketFSMBehaviour.STATE_PROCESS_CREATION_REQUEST:
			//do something
			break;
		case MarketFSMBehaviour.STATE_ACCEPT_AUCTION_CREATION:
			//do something
			break;
		case MarketFSMBehaviour.STATE_FINISH_AUCTION_CREATION:

		default:
			//state not define but perhaps do something
			break;
		}
	}
	
	@Override
	public int onEnd(){
		return _transition;
	}

}
