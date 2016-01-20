package agent.seller.behaviours;

import agent.market.behaviours.MarketFSMBehaviourAuctionProgress;
import agent.seller.Seller;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.FishMarketProtocol;

@SuppressWarnings("serial")
/**
 * 
 * @author cfollet & rbary
 *
 */
public class SellerFSMBehaviourAuctionProgress  extends FSMBehaviour {
	/** The id of the conversation of this auction. */
	private final String conversationId;

	/** The number of bids received for the last announced price. */
	private int bidCount = 0;

	/** The number of wait cycle which has been done. */
	private int waitCycleCount = 0;

	/** states **/
	private static final String STATE_ANNONCE_PRICE = "STATE_ANNONCE_PRICE";
	private static final String STATE_WAIT_BID = "STATE_WAIT_BID";
	private static final String STATE_HANDLE_MULTIPLE_BID = "STATE_HANDLE_MULTIPLE_BID";
	private static final String STATE_SEND_TO_ATTRIBUTE = "STATE_SEND_TO_ATTRIBUTE";
	private static final String STATE_SEND_TO_GIVE = "STATE_SEND_TO_GIVE";
	private static final String STATE_WAIT_TO_PAY = "STATE_WAIT_TO_PAY";
	private static final String STATE_TERMINATE_SUCCESS = "STATE_TERMINATE_SUCCESS";
	private static final String STATE_TERMINATE_CANCEL = "STATE_TERMINATE_CANCEL";

	/** transitions **/
	public static final int TRANSITION_TO_WAIT_TO_BID = 0;
	public static final int TRANSITION_TO_TERMINATE_CANCEL = 1;
	public static final int TRANSITION_TO_HANDLE_MULTIPLE_BID = 2;
	public static final int TRANSITION_TO_ATTRIBUTE = 3;
	public static final int TRANSITION_TO_WAIT_TO_PAY = 4;
	public static final int TRANSITION_TO_TERMINATE_SUCCESS = 5;
	public static final int TRANSITION_TO_ANNOUNCE = 6;

	/**
	 * Creates the behaviour of the seller agent when his auction is running.
	 * 
	 * @param mySeller
	 *            the seller agent to which the behaviour is to be added.
	 */
	public SellerFSMBehaviourAuctionProgress(Seller mySeller, String conversationId) {
		super(mySeller);

		this.conversationId = conversationId;

		// Add states
		this.registerFirstState(new AnnoucePriceSellerBehaviour(mySeller, this), STATE_ANNONCE_PRICE);

		this.registerState(new WaitBidSellerBehaviour(mySeller, this), STATE_WAIT_BID);

		this.registerState(new HandleMultipleBidSellerBehaviour(mySeller, this), STATE_HANDLE_MULTIPLE_BID);

		this.registerState(new AttributeFishSupplySellerBehaviour(mySeller, this), STATE_SEND_TO_ATTRIBUTE);

		this.registerState(new GiveFishSupplySellerBehaviour(mySeller, this), STATE_SEND_TO_GIVE);

		this.registerState(new WaitPaymentSellerBehaviour(mySeller, this), STATE_WAIT_TO_PAY);

		this.registerState(new WaitPaymentSellerBehaviour(mySeller, this), STATE_WAIT_TO_PAY);

		this.registerLastState(new TerminateCancelSellerBehaviour(mySeller, this), STATE_TERMINATE_CANCEL);

		this.registerLastState(new TerminateSuccessSellerBehaviour(mySeller, this), STATE_TERMINATE_SUCCESS);

		// Add transitions
		this.registerDefaultTransition(STATE_ANNONCE_PRICE, STATE_WAIT_BID);

		this.registerTransition(STATE_WAIT_BID, STATE_WAIT_BID, TRANSITION_TO_WAIT_TO_BID);

		this.registerTransition(STATE_WAIT_BID, STATE_ANNONCE_PRICE, TRANSITION_TO_ANNOUNCE);

		this.registerTransition(STATE_WAIT_BID, STATE_TERMINATE_CANCEL, TRANSITION_TO_TERMINATE_CANCEL);

		this.registerTransition(STATE_WAIT_BID, STATE_SEND_TO_ATTRIBUTE, TRANSITION_TO_ATTRIBUTE);

		this.registerTransition(STATE_WAIT_BID, STATE_HANDLE_MULTIPLE_BID, TRANSITION_TO_HANDLE_MULTIPLE_BID);

		this.registerTransition(STATE_HANDLE_MULTIPLE_BID, STATE_SEND_TO_ATTRIBUTE, TRANSITION_TO_ATTRIBUTE);

		this.registerTransition(STATE_HANDLE_MULTIPLE_BID, STATE_ANNONCE_PRICE, TRANSITION_TO_ANNOUNCE);

		this.registerDefaultTransition(STATE_SEND_TO_ATTRIBUTE, STATE_SEND_TO_GIVE);

		this.registerDefaultTransition(STATE_SEND_TO_GIVE, STATE_WAIT_TO_PAY);

		this.registerTransition(STATE_WAIT_TO_PAY, STATE_WAIT_TO_PAY, TRANSITION_TO_WAIT_TO_PAY);

		this.registerTransition(STATE_WAIT_TO_PAY, STATE_TERMINATE_SUCCESS, TRANSITION_TO_TERMINATE_SUCCESS);
	}

	/**
	 * 
	 * @return the identifier of the the conversation for this auction.
	 */
	public String getConversationId() {
		return conversationId;
	}

	/**
	 * Notifies that a new bid has been received for the last announced price.
	 */
	public void notifyNewBid() {
		++this.bidCount;

		((Seller) this.myAgent).notifyNewBid();
	}

	/**
	 * 
	 * @return the number of bid received for the last announced price.
	 */
	public int getBidCount() {
		return this.bidCount;
	}

	/**
	 * Sets the number of bid received to 0.
	 */
	public void resetBidCount() {
		this.bidCount = 0;
	}

	/**
	 * Notifies that a new bid wait cycle is over.
	 */
	public void notifyWaitCycle() {
		++this.waitCycleCount;
	}

	/**
	 * 
	 * @return the number of bid wait cycles which have been done.
	 */
	public int getWaitCycleCount() {
		return this.waitCycleCount;
	}

	/**
	 * Sets the number of bid wait cycle to 0.
	 */
	public void resetWaitCycleCount() {
		this.waitCycleCount = 0;
	}

	/**
	 * 
	 * @return the base message filter for conversation for this auction.
	 */
	public MessageTemplate getMessageFilter() {
		return MessageTemplate.and(MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
				MessageTemplate.MatchConversationId(this.conversationId));
	}
	
	
	// Behaviours
	
	private class AnnoucePriceSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public AnnoucePriceSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			Seller mySeller = (Seller) super.myAgent;

			mySeller.notifyAuctionStarted();

			this.myFSM.resetBidCount();
			this.myFSM.resetWaitCycleCount();

			// DEBUG
			System.out.println("Seller: sending to_announce(" + mySeller.getCurrentPrice() + ") !");

			ACLMessage mess = new ACLMessage(FishMarketProtocol.Performatives.TO_ANNOUNCE);

			// Receiver
			mess.addReceiver(mySeller.getMarketAgent());

			// Set topic
			mess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			mess.setConversationId(this.myFSM.getConversationId());

			// Add price
			mess.setContent(String.valueOf(mySeller.getCurrentPrice()));

			// Send
			mySeller.send(mess);

			// Update GUI
			mySeller.notifyNewAnnounce();

			// DEBUG
			System.out.println("Seller: notifying GUI !");
		}
	}

	
	private class AttributeFishSupplySellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public AttributeFishSupplySellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: sending to attribute !");

			Seller mySeller = (Seller) super.myAgent;

			ACLMessage mess = new ACLMessage(FishMarketProtocol.Performatives.TO_ATTRIBUTE);

			// Receiver
			mess.addReceiver(mySeller.getMarketAgent());

			// Set topic
			mess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			mess.setConversationId(this.myFSM.getConversationId());

			// send
			mySeller.send(mess);

			// DEBUG (break point)
			System.out.println();
		}
	}

	
	private class GiveFishSupplySellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public GiveFishSupplySellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: sending to give !");

			Seller mySeller = (Seller) super.myAgent;

			ACLMessage mess = new ACLMessage(FishMarketProtocol.Performatives.TO_GIVE);

			// Receiver
			mess.addReceiver(mySeller.getMarketAgent());

			// Set topic
			mess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			mess.setConversationId(this.myFSM.getConversationId());

			// send
			mySeller.send(mess);

			// DEBUG (break point)
			System.out.println();
		}
	}

	
	private class HandleMultipleBidSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/** The selected transition to the next state. */
		private int transition;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public HandleMultipleBidSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: wait more bid timeout !");

			Seller mySeller = (Seller) super.myAgent;

			// Select transition
			float nextPrice = mySeller.getCurrentPrice() + mySeller.getPriceStep();
			float maxPrice = mySeller.getMaxPrice();

			float nextPriceStep = mySeller.getPriceStep() / 2f;
			float minPriceStep = mySeller.getMinPriceStep();

			boolean repBidOk = false;
			this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_ANNOUNCE;

			if (nextPrice < maxPrice) {
				mySeller.increasePrice();

				// DEBUG
				System.out.println("Seller: transition is set to announce, with price increased by priceStep !");
			} else if (nextPriceStep >= minPriceStep) {
				mySeller.decreasePriceStep();

				mySeller.increasePrice();

				// DEBUG
				System.out.println("Seller: transition is set to announce, with price increased by priceStep/2 !");
			} else {
				repBidOk = true;

				// DEBUG
				System.out.println("Seller: setting transition to attribute !");

				this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_ATTRIBUTE;
			}

			// DEBUG
			System.out.println("Seller: sending rep_bid(" + repBidOk + ") !");

			// send rep_bid
			ACLMessage reply = new ACLMessage(FishMarketProtocol.Performatives.REP_BID);

			// Receiver
			reply.addReceiver(mySeller.getMarketAgent());

			// Set topic
			reply.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			reply.setConversationId(this.myFSM.getConversationId());

			// Add selected bidder AID
			reply.setContent(String.valueOf(repBidOk));

			// Send
			mySeller.send(reply);
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	
	private class TerminateCancelSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public TerminateCancelSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: sending to cancel !");

			Seller mySeller = (Seller) super.myAgent;

			// Notify auction cancelled
			ACLMessage cancelMess = new ACLMessage(FishMarketProtocol.Performatives.TO_CANCEL);

			// Receiver
			cancelMess.addReceiver(mySeller.getMarketAgent());

			// Set topic
			cancelMess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			cancelMess.setConversationId(this.myFSM.getConversationId());

			// send
			mySeller.send(cancelMess);

			mySeller.notifyAuctionCancelled();

			// Remove running auction
			mySeller.removeBehaviour(this.myFSM);

			// Add create auction
			mySeller.reset();
			mySeller.addBehaviour(new SellerFSMBehaviourAuctionCreation(mySeller));
		}
	}

	
	private class TerminateSuccessSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public TerminateSuccessSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: sending auction over !");

			Seller mySeller = (Seller) super.myAgent;

			ACLMessage mess = new ACLMessage(FishMarketProtocol.Performatives.AUCTION_OVER);

			// Receiver
			mess.addReceiver(mySeller.getMarketAgent());

			// Set topic
			mess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			mess.setConversationId(this.myFSM.getConversationId());

			// Send
			mySeller.send(mess);

			mySeller.notifyAuctionOver();

			// Remove running auction
			mySeller.removeBehaviour(this.myFSM);

			// Add create auction
			mySeller.reset();
			mySeller.addBehaviour(new SellerFSMBehaviourAuctionCreation(mySeller));
		}
	}

	
	private class WaitBidSellerBehaviour extends WakerBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/** The selected transition to the next state. */
		private int transition;

		/** The duration of one cycle to wait for bids. */
		private static final long WAIT_BID_CYCLE_DURATION = 500l; // 0.5 sec

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public WaitBidSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller, WAIT_BID_CYCLE_DURATION);

			this.myFSM = myFSM;
		}

		@Override
		public void onWake() {
			this.myFSM.notifyWaitCycle();

			Seller mySeller = (Seller) super.myAgent;

			// Receive messages
			ACLMessage mess;

			do {
				mess = mySeller.receive(this.getMessageFilter());

				if (mess != null) {
					this.myFSM.notifyNewBid();
				}
			} while (mess != null);

			// Decide transition
			int bidCount = this.myFSM.getBidCount();

			long maxCycleCount = mySeller.getBidWaitingDuration() / WaitBidSellerBehaviour.WAIT_BID_CYCLE_DURATION;

			boolean timeout = this.myFSM.getWaitCycleCount() > maxCycleCount;

			if (timeout)// || bidCount == mySeller.getSubscriberCount())
			{
				/** Either: make new announce with lower price OR cancel */
				if (bidCount == 0) {
					float newStep = mySeller.getPriceStep() / 2f;
					float newPrice = mySeller.getCurrentPrice() - newStep;

					if (newPrice >= mySeller.getMinPrice() && newStep >= mySeller.getMinPriceStep()) {
						mySeller.decreasePriceStep();
						mySeller.decreasePrice();

						// DEBUG
						System.out.println("Seller: setting transition to announce.");

						this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_ANNOUNCE;
					} else {
						// DEBUG
						System.out.println("Seller: setting transition to terminate cancel.");

						this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_TERMINATE_CANCEL;
					}
				}

				/** Attribute */
				else if (bidCount == 1) {
					// DEBUG
					System.out.println("Seller: setting transition to attribute.");

					// DEBUG
					System.out.println("Seller: sending rep bid ok.");

					// send rep_bid(OK)
					ACLMessage reply = new ACLMessage(FishMarketProtocol.Performatives.REP_BID);

					// Receiver
					reply.addReceiver(mySeller.getMarketAgent());

					// Set topic
					reply.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

					// Set conversation id
					reply.setConversationId(this.myFSM.getConversationId());

					// Add price and send
					reply.setContent(String.valueOf(true));

					// Send
					mySeller.send(reply);

					this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_ATTRIBUTE;
				}

				/** Multiple bid decision */
				else {
					// DEBUG
					System.out.println("Seller: setting transition to send rep bid nok.");

					// Handle multiple bids
					this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_HANDLE_MULTIPLE_BID;
				}
			} else {
				// Continue to wait
				this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_BID;
			}
		}

		@Override
		public int onEnd() {
			// For any future return to this state
			this.reset(WAIT_BID_CYCLE_DURATION);

			return this.transition;
		}

		/**
		 * 
		 * @return the message filter to use in this behaviour.
		 */
		private MessageTemplate getMessageFilter() {
			Seller mySeller = (Seller) super.myAgent;

			return MessageTemplate.and(this.myFSM.getMessageFilter(),
					MessageTemplate.and(MessageTemplate.MatchContent(String.valueOf(mySeller.getCurrentPrice())),
							MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_BID)));
		}
	}

	
	private class WaitPaymentSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionProgress myFSM;

		/** The transition which will be chosen next. */
		private int transition;

		/** Allows filtering incoming messages. */
		private final MessageTemplate messageFilter;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public WaitPaymentSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionProgress myFSM) {
			super(mySeller);

			this.myFSM = myFSM;

			this.messageFilter = this.createMessageFilter();
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: checking message for to_pay !");

			// Receive messages
			ACLMessage mess = myAgent.receive(this.messageFilter);

			if (mess != null) {
				// DEBUG
				System.out.println("Seller: setting transition to terminate sucess !");

				// Auction over
				this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_TERMINATE_SUCCESS;
			} else {
				// DEBUG
				System.out.println("Seller: setting transition to wait to pay !");

				// Continue to wait
				this.transition = SellerFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_PAY;

				this.myFSM.block();
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}

		/**
		 * 
		 * @return the message filter to use in this behaviour.
		 */
		private MessageTemplate createMessageFilter() {
			return MessageTemplate.and(this.myFSM.getMessageFilter(),
					MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_PAY));
		}
	}

}

