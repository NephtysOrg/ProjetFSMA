package agent.market.behaviours;

import java.util.Scanner;

import agent.market.Market;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.TopicUtility;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.Auction;
import util.FishMarketProtocol;

@SuppressWarnings("serial")
public class MarketFSMBehaviourAuctionCreation extends FSMBehaviour {
	private ACLMessage request;
	public static final AID MESSAGE_TOPIC = TopicUtility.createTopic(FishMarketProtocol.steps.STEP_AUCTION_CREATION);
	public static final MessageTemplate MESSAGE_FILTER = MessageTemplate
			.MatchTopic(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);
	private static final String STATE_WAIT_REQUEST = "STATE_WAIT_REQUEST";
	private static final String STATE_EVALUATE_REQUEST = "STATE_EVALUATE_REQUEST";
	private static final String STATE_ACCEPT_CREATION = "STATE_ACCEPT_CREATION";
	private static final String STATE_TERMINATE = "STATE_TERMINATE";
	public static final int TRANSITION_TO_EVALUATE_REQUEST = 0;
	public static final int TRANSITION_TO_TERMINATE = 1;
	public static final int TRANSITION_TO_WAIT_REQUEST = 2;
	public static final int TRANSITION_TO_CONFIRM_CREATION = 3;

	public MarketFSMBehaviourAuctionCreation(Market myMarketAgent) {
		super(myMarketAgent);
		// Register states
		// The last state must call myMarketAgent.setIsDone(true);
		this.registerFirstState(new WaitCreationRequestMarketBehaviour(myMarketAgent, this), STATE_WAIT_REQUEST);
		this.registerState(new EvaluateCreationResquestMarketBehaviour(myMarketAgent, this), STATE_EVALUATE_REQUEST);
		this.registerState(new ConfirmCreationMarketBehaviour(myMarketAgent, this), STATE_ACCEPT_CREATION);
		this.registerLastState(new TerminateCreationMarketBehaviour(myMarketAgent), STATE_TERMINATE);
		// Register transitions
		this.registerTransition(STATE_WAIT_REQUEST, STATE_EVALUATE_REQUEST, TRANSITION_TO_EVALUATE_REQUEST);
		this.registerTransition(STATE_WAIT_REQUEST, STATE_WAIT_REQUEST, TRANSITION_TO_WAIT_REQUEST);
		this.registerTransition(STATE_WAIT_REQUEST, STATE_TERMINATE, TRANSITION_TO_TERMINATE);
		this.registerTransition(STATE_EVALUATE_REQUEST, STATE_WAIT_REQUEST, TRANSITION_TO_WAIT_REQUEST);
		this.registerTransition(STATE_EVALUATE_REQUEST, STATE_ACCEPT_CREATION, TRANSITION_TO_CONFIRM_CREATION);
		this.registerDefaultTransition(STATE_ACCEPT_CREATION, STATE_WAIT_REQUEST);
	}

	public void setRequest(ACLMessage request) {
		this.request = request;
	}

	public ACLMessage getRequest() {
		return this.request;
	}

	// Behaviours
	private class ConfirmCreationMarketBehaviour extends OneShotBehaviour {
		private MarketFSMBehaviourAuctionCreation myFSM;

		public ConfirmCreationMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionCreation myFSM) {
			super(myMarketAgent);
			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			ACLMessage request = this.myFSM.getRequest();
			Market myMarketAgent = (Market) super.myAgent;
			// Create FSM Behaviour to manage the auction
			myMarketAgent.addBehaviour(new MarketFSMBehaviourAuctionProgress(myMarketAgent, request.getSender(),
					request.getConversationId()));
			// Send reply
			ACLMessage reply = request.createReply();
			reply.setPerformative(FishMarketProtocol.Performatives.TO_ACCEPT);
			// Set topic
			reply.addReceiver(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);
			// Set conversation ID
			reply.setConversationId(request.getConversationId());
			myMarketAgent.send(reply);
			// Delete request
			this.myFSM.setRequest(null);
			// Update GUI
			myMarketAgent.refreshView();
		}
	}

	private class EvaluateCreationResquestMarketBehaviour extends OneShotBehaviour {
		private MarketFSMBehaviourAuctionCreation myFSM;
		private int transition;

		public EvaluateCreationResquestMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionCreation myFSM) {
			super(myMarketAgent);
			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			Market myMarketAgent = (Market) myAgent;
			ACLMessage request = this.myFSM.getRequest();
			String auctionId = MarketFSMBehaviourAuctionProgress.createAuctionId(request.getSender());
			// DEBUG
			System.out.println("Market: evaluating creation request of auction with id " + auctionId);
			System.out.println("Topic is: " + MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);
			boolean accepted = true;
			Auction auction = myMarketAgent.findAuction(auctionId);
			if (auction == null) {
				auction = new Auction(auctionId);
			} else if (auction.getStatus() != Auction.STATUS_CANCELLED && auction.getStatus() != Auction.STATUS_OVER) {
				accepted = false;
			}
			if (accepted) {
				// DEBUG
				System.out.println("Market: auction is not yet registered");
				Scanner input = new Scanner(request.getContent());
				input.useDelimiter(":");
				String fishSupplyName = input.hasNext() ? input.next() : "";
				float price = input.hasNextFloat() ? input.nextFloat() : 0f;
				input.close();
				// Register auction
				auction.setStatus(Auction.STATUS_RUNNING);
				auction.setAuctionName(fishSupplyName);
				auction.setCurrentPrice(price);
				myMarketAgent.registerAuction(auction, request.getSender());
				request.setConversationId(auctionId);
				// Next transition
				this.transition = MarketFSMBehaviourAuctionCreation.TRANSITION_TO_CONFIRM_CREATION;
			} else {
				// DEBUG
				System.out.println("Market: auction is already registered");
				// Reply refuse
				ACLMessage reply = this.myFSM.getRequest().createReply();
				reply.setPerformative(FishMarketProtocol.Performatives.TO_REFUSE);
				// Set topic
				reply.addReceiver(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);
				super.myAgent.send(reply);
				// Next transition
				this.transition = MarketFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_REQUEST;
				this.myFSM.setRequest(null);
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private class TerminateCreationMarketBehaviour extends OneShotBehaviour {
		public TerminateCreationMarketBehaviour(Market myMarketAgent) {
			super(myMarketAgent);
		}

		@Override
		public void action() {
			((Market) super.myAgent).setIsDone(true);
			// Say bye !
		}
	}

	private static class WaitCreationRequestMarketBehaviour extends OneShotBehaviour {
		private MarketFSMBehaviourAuctionCreation myFSM;
		private int transition;
		private final static MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				MarketFSMBehaviourAuctionCreation.MESSAGE_FILTER,
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CREATE));

		public WaitCreationRequestMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionCreation myFSM) {
			super(myMarketAgent);
			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// Delete any previous request
			this.myFSM.setRequest(null);
			// Receive messages
			ACLMessage mess = myAgent.receive(WaitCreationRequestMarketBehaviour.MESSAGE_FILTER);
			if (((Market) myAgent).isDone()) {
				// DEBUG
				System.out.println("Market: setting transition to terminate auction creation request management!");
				this.transition = MarketFSMBehaviourAuctionCreation.TRANSITION_TO_TERMINATE;
			} else if (mess != null) {
				this.myFSM.setRequest(mess);
				// DEBUG
				System.out.println("Market: setting transition to evaluate auction creation request !");
				this.transition = MarketFSMBehaviourAuctionCreation.TRANSITION_TO_EVALUATE_REQUEST;
			} else {
				// DEBUG
				System.out.println("Market: setting transition to wait auction creation request !");
				this.transition = MarketFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_REQUEST;
				// DEBUG
				System.out.println("Market: blocking FSM to wait for auction creation request !");
				// Wait that myAgent receives message
				this.myFSM.block();
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}
}
