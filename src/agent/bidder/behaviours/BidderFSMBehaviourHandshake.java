package agent.bidder.behaviours;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import agent.bidder.Bidder;
import agent.market.behaviours.MarketFSMBehaviourHandshake;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import util.Auction;
import util.FishMarketProtocol;

/**
 * 
 * @author cfollet & rbary
 *
 */
@SuppressWarnings("serial")
public class BidderFSMBehaviourHandshake extends FSMBehaviour {

	/** states **/
	private static final String STATE_SUBSCRIPTION_PROCESS_START = "STATE_SUBSCRIPTION_PROCESS_START";
	private static final String STATE_WAIT_AUCTION_LIST = "STATE_WAIT_AUCTION_LIST";
	private static final String STATE_PICK_AUCTION = "STATE_PICK_AUCTION";
	private static final String STATE_WAIT_SUBSCRIPTION_REPLY = "STATE_WAIT_SUBSCRIPTION_REPLY";
	private static final String STATE_TERMINATE_FAILURE = "STATE_TERMINATE_FAILURE";
	private static final String STATE_TERMINATE_SUCCESS = "STATE_TERMINATE_SUCCESS";

	/** transitions **/
	public static final int TRANSITION_REQUEST_AUCTION_LIST = 0;
	public static final int TRANSITION_AUCTION_LIST_RECEIVED = 1;
	public static final int TRANSITION_REQUEST_SUBSCRIPTION = 2;
	public static final int TRANSITION_SUBSCRIPTION_ACCEPTED = 3;
	public static final int TRANSITION_SUBSCRIPTION_REFUSED = 4;
	public static final int TRANSITION_TERMINATE_FAILURE = 5;
	public static final int TRANSITION_TERMINATE_SUCCESS = 6;
	public static final int TRANSITION_RETURN_TO_SUBSCRIPTION_PROCESS_START = 7;
	public static final int TRANSITION_WAIT_AUCTION_LIST = 8;
	public static final int TRANSITION_WAIT_SUBSCRIPTION_RESULT = 9;
	public static final int TRANSITION_WAIT_USER_CHOICE = 10;

	/**
	 * An empty auction list has been received.
	 */
	public static final int STATUS_EMPTY_AUCTION_LIST = 0;

	/**
	 * Used to pass the auction list to pick auctions from.
	 */
	private ACLMessage request;

	private String lastSubscribedAuction;

	private Set<String> subscribedAuctions = new HashSet<String>();

	public BidderFSMBehaviourHandshake(Agent a) {
		super(a);

		// Declare and register states

		SubscriptionProcessStartBidderBehaviour initialState = new SubscriptionProcessStartBidderBehaviour(a);

		this.registerFirstState(initialState, STATE_SUBSCRIPTION_PROCESS_START);

		WaitAuctionListBidderBehaviour waitAuctionListBehaviour = new WaitAuctionListBidderBehaviour(a, this);

		this.registerState(waitAuctionListBehaviour, STATE_WAIT_AUCTION_LIST);

		PickAuctionBidderBehaviour pickAuctionBehaviour = new PickAuctionBidderBehaviour(a, this);

		this.registerState(pickAuctionBehaviour, STATE_PICK_AUCTION);

		WaitSubscriptionReplyBidderBehaviour waitSubscriptionReplyBehaviour = new WaitSubscriptionReplyBidderBehaviour(
				a, this);

		this.registerState(waitSubscriptionReplyBehaviour, STATE_WAIT_SUBSCRIPTION_REPLY);

		TerminateFailureBidderBehaviour terminateFailureBidderBehaviour = new TerminateFailureBidderBehaviour(a);

		this.registerLastState(terminateFailureBidderBehaviour, STATE_TERMINATE_FAILURE);

		TerminateSuccessBidderBehaviour terminateSuccessBidderBehaviour = new TerminateSuccessBidderBehaviour(a);

		this.registerLastState(terminateSuccessBidderBehaviour, STATE_TERMINATE_SUCCESS);

		// Transitions
		this.registerTransition(STATE_SUBSCRIPTION_PROCESS_START, STATE_WAIT_AUCTION_LIST,
				BidderFSMBehaviourHandshake.TRANSITION_REQUEST_AUCTION_LIST);

		this.registerTransition(STATE_WAIT_AUCTION_LIST, STATE_PICK_AUCTION,
				BidderFSMBehaviourHandshake.TRANSITION_AUCTION_LIST_RECEIVED);

		this.registerTransition(STATE_WAIT_AUCTION_LIST, STATE_WAIT_AUCTION_LIST,
				BidderFSMBehaviourHandshake.TRANSITION_WAIT_AUCTION_LIST);

		this.registerTransition(STATE_PICK_AUCTION, STATE_WAIT_SUBSCRIPTION_REPLY,
				BidderFSMBehaviourHandshake.TRANSITION_REQUEST_SUBSCRIPTION);

		this.registerTransition(STATE_PICK_AUCTION, STATE_PICK_AUCTION,
				BidderFSMBehaviourHandshake.TRANSITION_WAIT_USER_CHOICE);

		this.registerTransition(STATE_WAIT_SUBSCRIPTION_REPLY, STATE_WAIT_SUBSCRIPTION_REPLY,
				BidderFSMBehaviourHandshake.TRANSITION_WAIT_SUBSCRIPTION_RESULT);

		// Return to process start when auction list empty
		this.registerTransition(STATE_PICK_AUCTION, STATE_SUBSCRIPTION_PROCESS_START,
				BidderFSMBehaviourHandshake.TRANSITION_RETURN_TO_SUBSCRIPTION_PROCESS_START);

		this.registerTransition(STATE_WAIT_SUBSCRIPTION_REPLY, STATE_TERMINATE_SUCCESS,
				BidderFSMBehaviourHandshake.TRANSITION_SUBSCRIPTION_ACCEPTED);

		this.registerTransition(STATE_WAIT_SUBSCRIPTION_REPLY, STATE_PICK_AUCTION,
				BidderFSMBehaviourHandshake.TRANSITION_SUBSCRIPTION_REFUSED);
		;

		// transitions to subscription process end
		this.registerTransition(STATE_SUBSCRIPTION_PROCESS_START, STATE_TERMINATE_FAILURE,
				BidderFSMBehaviourHandshake.TRANSITION_TERMINATE_FAILURE);

		this.registerTransition(STATE_WAIT_AUCTION_LIST, STATE_TERMINATE_FAILURE,
				BidderFSMBehaviourHandshake.TRANSITION_TERMINATE_FAILURE);

		this.registerTransition(STATE_PICK_AUCTION, STATE_TERMINATE_FAILURE,
				BidderFSMBehaviourHandshake.TRANSITION_TERMINATE_FAILURE);

	}

	/**
	 * @return the last received message containing an auction list.
	 */
	public ACLMessage getRequest() {
		return this.request;
	}

	/**
	 * @param request
	 *            A message containing an auction list to store.
	 */
	public void setRequest(ACLMessage request) {
		this.request = request;
	}

	/**
	 * Stores seller in list of subscribed auctions.
	 *
	 * @param seller
	 */
	public void subscribeToAuction(String seller) {
		this.subscribedAuctions.add(seller);
	}

	/**
	 * Returns true if auction has already been subscribed to.
	 *
	 * @param auctionId
	 * @return
	 */
	public boolean hasSubscribedToAuction(String auctionId) {
		return this.subscribedAuctions.contains(auctionId);
	}

	public void setLastSubscribedAuction(String auction) {
		this.lastSubscribedAuction = auction;
	}

	public String getLastSubscribedAuction() {
		return this.lastSubscribedAuction;
	}

	// Behaviours

	private static class PickAuctionBidderBehaviour extends OneShotBehaviour {
		/** Logging. */
		private static final Logger LOGGER = Logger.getLogger(PickAuctionBidderBehaviour.class.getName());

		private BidderFSMBehaviourHandshake myFSM;

		private int transition;

		private ACLMessage lastAuctionListMessage = null;

		public PickAuctionBidderBehaviour(Agent a, BidderFSMBehaviourHandshake fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) myAgent;

			ACLMessage mess = this.myFSM.getRequest();

			if (mess != null) {
				// Handling new list
				this.myFSM.setRequest(null);
				this.lastAuctionListMessage = mess;

				// Extract auction list
				HashSet<Auction> auctionList = this.extractAuctionsFromMessage(mess);

				Bidder.displayAuctionList(auctionList);
			}

			if (Bidder.hasAuctionSelected()) {
				// Auction has been picked by user
				Auction selectedAuction = Bidder.getSubscribedAuction();

				ACLMessage subscription = this.lastAuctionListMessage.createReply();
				subscription.setPerformative(FishMarketProtocol.Performatives.TO_SUBSCRIBE);

				subscription.addReceiver(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);

				subscription.setContent(selectedAuction.getID());

				Bidder.send(subscription);

				// Reset state
				Bidder.setAuctionSelected(false);

				this.transition = BidderFSMBehaviourHandshake.TRANSITION_REQUEST_SUBSCRIPTION;
			} else {
				// wait some more
				this.myFSM.block(500);

				this.transition = BidderFSMBehaviourHandshake.TRANSITION_WAIT_USER_CHOICE;
			}
		}

		private HashSet<Auction> extractAuctionsFromMessage(ACLMessage message) {
			Object content = null;
			HashSet<Auction> auctionList = null;

			if (message != null) {
				try {
					content = message.getContentObject();
				} catch (UnreadableException e) {
					PickAuctionBidderBehaviour.LOGGER.log(Level.SEVERE, null, e);
				}

				if (content != null) {
					auctionList = (HashSet<Auction>) content;
				}
			}

			if (auctionList == null) {
				auctionList = new HashSet<Auction>();
			}

			return auctionList;
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private class SubscriptionProcessStartBidderBehaviour extends OneShotBehaviour {

		private int transition;

		public SubscriptionProcessStartBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			// Prepare and send message
			AID marketAID = Bidder.getMarketAgentAID();

			if (marketAID != null) {
				ACLMessage requestAuctionListMessage = new ACLMessage(FishMarketProtocol.Performatives.TO_REQUEST);

				requestAuctionListMessage.addReceiver(marketAID);

				requestAuctionListMessage.addReceiver(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);

				Bidder.send(requestAuctionListMessage);

				this.transition = BidderFSMBehaviourHandshake.TRANSITION_REQUEST_AUCTION_LIST;
				;
			} else {
				((Bidder) myAgent).alertNoMarket();

				this.transition = BidderFSMBehaviourHandshake.TRANSITION_TERMINATE_FAILURE;
			}
			// transition to next step

		}

		@Override
		public int onEnd() {
			// Implemented because of possible early return to end state.

			return this.transition;
		}
	}

	private class TerminateFailureBidderBehaviour extends OneShotBehaviour {
		public TerminateFailureBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());
			System.out.println("Subscription process interrupted.");
		}
	}

	private class TerminateSuccessBidderBehaviour extends OneShotBehaviour {

		public TerminateSuccessBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());
			System.out.println("Subscribed successfully to auction.");

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.createBidderFSM();
		}
	}

	private static class WaitAuctionListBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourHandshake myFSM;

		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				MarketFSMBehaviourHandshake.MESSAGE_FILTER,
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_PROVIDE));

		private int transition;

		public WaitAuctionListBidderBehaviour(Agent a, BidderFSMBehaviourHandshake myFSM) {
			super(a);
			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			// Receive message
			ACLMessage mess = Bidder.receive(MESSAGE_FILTER);

			if (mess != null) {
				// TODO : remove
				System.out.println("List received");

				myFSM.setRequest(mess);
				this.transition = BidderFSMBehaviourHandshake.TRANSITION_AUCTION_LIST_RECEIVED;
				// this.myFSM.restart();
			} else {
				// wait for incoming message
				this.transition = BidderFSMBehaviourHandshake.TRANSITION_WAIT_AUCTION_LIST;

				this.myFSM.block();
			}

			// transition to next step

		}

		@Override
		public int onEnd() {
			// Implemented because of possible early return to end state.
			return this.transition;
		}
	}

	private static class WaitSubscriptionReplyBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourHandshake myFSM;

		private int transition;

		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate
				.and(MarketFSMBehaviourHandshake.MESSAGE_FILTER,
						MessageTemplate.or(
								MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ACCEPT),
								MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_REFUSE)));

		public WaitSubscriptionReplyBidderBehaviour(Agent a, BidderFSMBehaviourHandshake fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			// Receive message
			ACLMessage mess = Bidder.receive(MESSAGE_FILTER);

			String seller = null;

			if (mess != null) {

				if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_ACCEPT) {
					// subscription succeeded
					this.transition = BidderFSMBehaviourHandshake.TRANSITION_SUBSCRIPTION_ACCEPTED;

					seller = mess.getContent();
				} else {
					this.transition = BidderFSMBehaviourHandshake.TRANSITION_SUBSCRIPTION_REFUSED;

					Bidder.displayBidInformation("Subscription refused.");

				}
			} else {
				// wait for incoming message
				this.myFSM.block();
				this.transition = BidderFSMBehaviourHandshake.TRANSITION_WAIT_SUBSCRIPTION_RESULT;
			}

			// transition to next step
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

}
