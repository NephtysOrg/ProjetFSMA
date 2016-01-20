package agent.seller.behaviours;

import agent.market.behaviours.MarketFSMBehaviourAuctionCreation;
import agent.market.behaviours.MarketFSMBehaviourAuctionProgress;
import agent.seller.Seller;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.FishMarketProtocol;

@SuppressWarnings("serial")
/**
 * 
 * @author cfollet & rbary
 *
 */
public class SellerFSMBehaviourAuctionCreation extends FSMBehaviour {
	/** Holds the response from the market. */
	private ACLMessage response;

	/** The number of request already sent. */
	private int requestCount = 0;

	/** Base filter for filtering messages. */
	public static final MessageTemplate MESSAGE_FILTER = MessageTemplate
			.MatchTopic(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);

	/** states **/
	private static final String STATE_WAIT_CREATE_COMMAND = "STATE_WAIT_CREATE_COMMAND";
	private static final String STATE_REQUEST_CREATION = "STATE_REQUEST_CREATION";
	private static final String STATE_WAIT_RESPONSE = "STATE_WAIT_RESPONSE";
	private static final String STATE_WAIT_SUBSCRIBERS = "STATE_WAIT_SUBSCRIBERS";
	private static final String STATE_TERMINATE_SUCCESS = "STATE_TERMINATE_SUCCESS";
	private static final String STATE_TERMINATE_FAILURE = "STATE_TERMINATE_FAILURE";
	private static final String STATE_TERMINATE_CANCEL = "STATE_TERMINATE_CANCEL";
	
	/** transitions **/
	public static final int TRANSITION_TO_WAIT_CREATE_COMMAND = 0;
	public static final int TRANSITION_TO_WAIT_RESPONSE = 1;
	public static final int TRANSITION_TO_TERMINATE_FAILURE = 2;
	public static final int TRANSITION_TO_TERMINATE_CANCEL = 3;
	public static final int TRANSITION_TO_WAIT_SUBSCRIBERS = 4;
	public static final int TRANSITION_TO_REQUEST_CREATION = 5;
	public static final int TRANSITION_TO_TERMINATE_SUCCESS = 6;

	/**
	 * Create a behaviour which allows to interact with the market agent in
	 * order to create an auction.
	 * 
	 * @param mySeller
	 *            the seller agent to which the behaviour is to be added.
	 */
	public SellerFSMBehaviourAuctionCreation(Seller mySeller) {
		super(mySeller);

		// Add states
		// Final state must create AuctionSellerBehaviour
		this.registerFirstState(new WaitCreateCommandSellerBehaviour(mySeller, this), STATE_WAIT_CREATE_COMMAND);

		this.registerState(new RequestCreationSellerBehaviour(mySeller, this), STATE_REQUEST_CREATION);

		this.registerState(new WaitResponseSellerBehaviour(mySeller, this), STATE_WAIT_RESPONSE);

		this.registerState(new WaitSubscribersSellerBehaviour(mySeller, this), STATE_WAIT_SUBSCRIBERS);

		this.registerLastState(new TerminateSuccessSellerBehaviour(mySeller, this), STATE_TERMINATE_SUCCESS);

		this.registerLastState(new TerminateFailureSellerBehaviour(mySeller, this), STATE_TERMINATE_FAILURE);

		this.registerLastState(new TerminateCancelSellerBehaviour(mySeller, this), STATE_TERMINATE_CANCEL);

		// Add transitions
		this.registerTransition(STATE_WAIT_CREATE_COMMAND, STATE_WAIT_CREATE_COMMAND,
				TRANSITION_TO_WAIT_CREATE_COMMAND);

		this.registerTransition(STATE_WAIT_CREATE_COMMAND, STATE_REQUEST_CREATION, TRANSITION_TO_REQUEST_CREATION);

		this.registerTransition(STATE_REQUEST_CREATION, STATE_WAIT_RESPONSE, TRANSITION_TO_WAIT_RESPONSE);

		this.registerTransition(STATE_REQUEST_CREATION, STATE_TERMINATE_FAILURE, TRANSITION_TO_TERMINATE_FAILURE);

		this.registerTransition(STATE_WAIT_RESPONSE, STATE_WAIT_RESPONSE, TRANSITION_TO_WAIT_RESPONSE);

		this.registerTransition(STATE_WAIT_RESPONSE, STATE_REQUEST_CREATION, TRANSITION_TO_REQUEST_CREATION);

		this.registerTransition(STATE_WAIT_RESPONSE, STATE_WAIT_SUBSCRIBERS, TRANSITION_TO_WAIT_SUBSCRIBERS);

		this.registerTransition(STATE_WAIT_SUBSCRIBERS, STATE_WAIT_SUBSCRIBERS, TRANSITION_TO_WAIT_SUBSCRIBERS);

		this.registerTransition(STATE_WAIT_SUBSCRIBERS, STATE_TERMINATE_CANCEL, TRANSITION_TO_TERMINATE_CANCEL);

		this.registerTransition(STATE_WAIT_SUBSCRIBERS, STATE_TERMINATE_SUCCESS, TRANSITION_TO_TERMINATE_SUCCESS);
	}

	@Override
	public void reset() {
		this.response = null;
		this.requestCount = 0;

		super.reset();
	}

	/**
	 * 
	 * @return the response from the market
	 */
	public ACLMessage getResponse() {
		return response;
	}

	/**
	 * 
	 * @param response
	 *            the response from the market
	 */
	public void setResponse(ACLMessage response) {
		this.response = response;
	}

	/**
	 * 
	 * @return the number of request sent until now.
	 */
	public int getRequestCount() {
		return requestCount;
	}

	/**
	 * 
	 * @param count
	 *            the number of request sent until now.
	 */
	public void setRequestCount(int count) {
		this.requestCount = count;
	}

	/**
	 * Notifies that a new registration request has been sent.
	 * 
	 * @return the update number of sent requests.
	 */
	public int notifyNewRequest() {
		return ++this.requestCount;
	}
	
	
	// Behaviours
	
	private class RequestCreationSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/** The transition which will be selected. */
		private int transition;

		/** The maximal number of attempts to register an auction. */
		private static final int MAX_REQUEST_ATTEMPTS = 4;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public RequestCreationSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			if (this.myFSM.getRequestCount() < MAX_REQUEST_ATTEMPTS) {
				// DEBUG
				System.out.println("Seller: requesting auction creation !");

				Seller mySeller = (Seller) super.myAgent;

				ACLMessage mess = new ACLMessage(FishMarketProtocol.Performatives.TO_CREATE);

				// Receiver
				mess.addReceiver(mySeller.getMarketAgent());

				// Set topic
				mess.addReceiver(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);

				// Add starting price
				mess.setContent(mySeller.getFishSupplyName() + ":" + String.valueOf(mySeller.getCurrentPrice()));

				// Send
				mySeller.send(mess);

				this.myFSM.notifyNewRequest();

				// Select next transition
				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_RESPONSE;

				// DEBUG
				System.out.println("Seller: transition set to wait response !");
			} else {
				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_TERMINATE_FAILURE;

				// DEBUG
				System.out.println("Seller: transition set to terminate failure !");
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	
	
	@SuppressWarnings("serial")
	private class TerminateCancelSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public TerminateCancelSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// Send to_cancel
			Seller mySeller = (Seller) super.myAgent;

			ACLMessage cancelMess = new ACLMessage(FishMarketProtocol.Performatives.TO_CANCEL);

			// Set topic
			cancelMess.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);

			// Set conversation id
			cancelMess.setConversationId(this.myFSM.getResponse().getConversationId());

			// Receiver
			cancelMess.addReceiver(mySeller.getMarketAgent());

			// Add auction and send
			mySeller.send(cancelMess);

			mySeller.notifyAuctionCancelled();

			// Restarting
			mySeller.removeBehaviour(this.myFSM);
			mySeller.reset();
			mySeller.addBehaviour(new SellerFSMBehaviourAuctionCreation(mySeller));
		}
	}

	
	
	@SuppressWarnings("serial")
	private class TerminateFailureSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public TerminateFailureSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {

			((Seller) this.myAgent).notifyMessage("Auction creation failed :_(");

			this.myFSM.reset();
		}
	}

	
	
	@SuppressWarnings("serial")
	private class TerminateSuccessSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public TerminateSuccessSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// Add running auction FSM
			super.myAgent.addBehaviour(new SellerFSMBehaviourAuctionProgress((Seller) super.myAgent,
					this.myFSM.getResponse().getConversationId()));

			// Remove auction creation FSM
			super.myAgent.removeBehaviour(this.myFSM);
		}
	}

	
	
	@SuppressWarnings("serial")
	private class WaitCreateCommandSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/** The transition which will be selected. */
		private int transition;

		/** the duration of one blocking cycle in order to wait for suscribers. */
		private static final long WAIT_CREATE_COMMAND_CYCLE_DURATION = 500; // 1 sec

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public WaitCreateCommandSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			Seller mySeller = (Seller) super.myAgent;

			if (mySeller.isCreateCommandReceived()) {
				if (mySeller.lookupMarketAgent()) {
					// DEBUG
					System.out.println("Seller: setting transition to request creation !");

					this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_REQUEST_CREATION;
				} else {
					mySeller.notifyMarketNotFound();

					this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_CREATE_COMMAND;

					this.myFSM.block(WAIT_CREATE_COMMAND_CYCLE_DURATION);
				}
			} else {
				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_CREATE_COMMAND;

				this.myFSM.block(WAIT_CREATE_COMMAND_CYCLE_DURATION);
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	
	
	@SuppressWarnings("serial")
	private static class WaitResponseSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/** The transition which will be selected. */
		private int transition;

		/** Allows filtering incoming messages. */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				SellerFSMBehaviourAuctionCreation.MESSAGE_FILTER,
				MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ACCEPT),
						MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_REFUSE)));

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public WaitResponseSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			// DEBUG
			System.out.println("Seller: checking messages !");

			Seller mySeller = (Seller) super.myAgent;

			// Receive messages
			ACLMessage mess = myAgent.receive(WaitResponseSellerBehaviour.MESSAGE_FILTER);

			if (mess != null) {
				this.myFSM.setResponse(mess);

				// Reset blocking state
				this.restart();

				if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_ACCEPT) {
					this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_SUBSCRIBERS;

					mySeller.notifyAuctionCreated();

					// DEBUG
					System.out.println("Seller: setting transition to wait subscribers !");
				} else {
					this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_REQUEST_CREATION;

					// DEBUG
					System.out.println("Seller: setting transition to request creation !");
				}
			} else {
				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_RESPONSE;

				// DEBUG
				System.out.println("Seller: transition to wait response !");

				// DEBUG
				System.out.println("Seller: blocking FSM for wainting messages !");

				// Wait that myAgent receives message
				this.myFSM.block();
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	
	
	@SuppressWarnings("serial")
	public static class WaitSubscribersSellerBehaviour extends OneShotBehaviour {
		/** The FSM behaviour to which this behaviour is to be added. */
		private SellerFSMBehaviourAuctionCreation myFSM;

		/** The transition which will be selected. */
		private int transition;

		/** the duration of one blocking cycle in order to wait for suscribers. */
		private static final long WAITING_SUBSCRIBER_CYCLE_DURATION = 500; // 0.5
																			// sec

		/** Allows filtering incoming messages. */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				SellerFSMBehaviourAuctionCreation.MESSAGE_FILTER,
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_SUBSCRIBE));

		/**
		 * Creates a behaviour which represents a state of the FSM behaviour of a
		 * seller agent.
		 * 
		 * @param mySeller
		 *            the seller agent to which the FSM is to be added.
		 * @param myFSM
		 *            the FSM behaviour to which this behaviour is to be added.
		 */
		public WaitSubscribersSellerBehaviour(Seller mySeller, SellerFSMBehaviourAuctionCreation myFSM) {
			super(mySeller);

			this.myFSM = myFSM;
		}

		@Override
		public void action() {
			Seller mySeller = (Seller) super.myAgent;

			// Receive messages
			ACLMessage mess = myAgent.receive(WaitSubscribersSellerBehaviour.MESSAGE_FILTER);

			if (mess != null) {
				if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_SUBSCRIBE) {
					mySeller.notifyNewSubscriber();
				}

				// DEBUG
				System.out.println("Seller: notifying new subscriber !");
			}

			if (mySeller.isStartCommandReceived()) {

				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_TERMINATE_SUCCESS;

				// DEBUG
				System.out.println("Seller: setting transition to terminate success !");
			} else if (mySeller.isCancelCommandReceived()) {
				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_TERMINATE_CANCEL;

				// DEBUG
				System.out.println("Seller: setting transition to terminate cancel !");
			} else {
				// DEBUG
				// System.out.println("Seller: setting transition to wait
				// subscribers !");

				this.transition = SellerFSMBehaviourAuctionCreation.TRANSITION_TO_WAIT_SUBSCRIBERS;

				this.myFSM.block(WAITING_SUBSCRIBER_CYCLE_DURATION);
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

}

