package agent.bidder.behaviours;

import java.util.Random;
import agent.bidder.Bidder;
import agent.market.behaviours.MarketFSMBehaviourAuctionProgress;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.FishMarketProtocol;

/**
 * 
 * @author cfollet & rbary
 *
 */
@SuppressWarnings("serial")
public class BidderFSMBehaviourAuctionProgress extends FSMBehaviour {
	
	/** states **/
	private static final String STATE_INITIAL_OR_AFTER_FAILED_BID = "STATE_INITIAL_OR_AFTER_FAILED_BID";
	private static final String STATE_ABOUT_TO_BID = "STATE_ABOUT_TO_BID";
	private static final String STATE_WAIT_BID_RESULT = "STATE_WAIT_BID_RESULT";
	private static final String STATE_WAIT_ATTRIBUTION = "STATE_WAIT_ATTRIBUTION";
	private static final String STATE_WAIT_FISH = "STATE_WAIT_FISH";
	private static final String STATE_PAYMENT = "STATE_PAYMENT";
	private static final String STATE_AUCTION_OVER_SUCCESSFULLY = "STATE_AUCTION_OVER_SUCCESSFULLY";
	private static final String STATE_AUCTION_OVER_UNSUCESSFULLY = "STATE_AUCTION_OVER_UNSUCESSFULLY";
	private static final String STATE_OTHER_BIDDER_WON = "STATE_OTHER_BIDDER_WON";
	
	/** transitions **/
	public static final int TRANSITION_RECEIVED_FIRST_ANNOUNCE = 0;
	public static final int TRANSITION_RECEIVED_SUBSEQUENT_ANNOUNCE = 1;
	public static final int TRANSITION_BID = 2;
	public static final int TRANSITION_RECEIVED_REP_BID_OK = 3;
	public static final int TRANSITION_RECEIVED_REP_BID_NOK = 4;
	public static final int TRANSITION_GO_GET_FISH = 5;
	public static final int TRANSITION_TO_PAYMENT = 6;
	public static final int TRANSITION_RECEIVED_AUCTION_CANCELLED = 7;
	public static final int TRANSITION_RECEIVED_AUCTION_OVER = 8;
	public static final int TRANSITION_WAIT_AUCTION_OVER = 9;
	public static final int TRANSITION_WAIT_FIRST_ANNOUNCE = 10;
	public static final int TRANSITION_WAIT_BID_RESULT = 11;
	public static final int TRANSITION_WAIT_ATTRIBUTION = 12;
	public static final int TRANSITION_WAIT_FISH = 13;
	public static final int TRANSITION_WAIT_USER_CHOICE = 14;


	/** Message holder */
	private ACLMessage request;

	public BidderFSMBehaviourAuctionProgress(Agent a) {
		super(a);
		// States registration
		this.registerFirstState(new InitialOrAfterFailedBidBidderBehaviour(a, this), STATE_INITIAL_OR_AFTER_FAILED_BID);

		this.registerState(new AboutToBidBidderBehaviour(a, this), STATE_ABOUT_TO_BID);

		this.registerState(new WaitBidResultBidderBehaviour(a, this), STATE_WAIT_BID_RESULT);

		this.registerState(new WaitAttributionBidderBehaviour(a, this), STATE_WAIT_ATTRIBUTION);

		this.registerState(new WaitFishBidderBehaviour(a, this), STATE_WAIT_FISH);

		this.registerState(new PaymentBidderBehaviour(a, this), STATE_PAYMENT);

		this.registerLastState(new AuctionOverSuccessfullyBidderBehaviour(a), STATE_AUCTION_OVER_SUCCESSFULLY);

		this.registerLastState(new AuctionOverUnsuccessfullyBidderBehaviour(a), STATE_AUCTION_OVER_UNSUCESSFULLY);

		this.registerLastState(new OtherBidderWonBidderBehaviour(a), STATE_OTHER_BIDDER_WON);

		// Transitions registration 
		this.registerTransition(STATE_INITIAL_OR_AFTER_FAILED_BID, STATE_ABOUT_TO_BID,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_FIRST_ANNOUNCE);

		this.registerTransition(STATE_INITIAL_OR_AFTER_FAILED_BID, STATE_INITIAL_OR_AFTER_FAILED_BID,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_FIRST_ANNOUNCE);

		this.registerTransition(STATE_ABOUT_TO_BID, STATE_WAIT_BID_RESULT,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_BID_RESULT);

		this.registerTransition(STATE_ABOUT_TO_BID, STATE_ABOUT_TO_BID,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_SUBSEQUENT_ANNOUNCE);

		this.registerTransition(STATE_ABOUT_TO_BID, STATE_ABOUT_TO_BID,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_USER_CHOICE);

		this.registerTransition(STATE_WAIT_BID_RESULT, STATE_WAIT_ATTRIBUTION,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_REP_BID_OK);

		this.registerTransition(STATE_WAIT_BID_RESULT, STATE_WAIT_BID_RESULT,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_BID_RESULT);

		this.registerTransition(STATE_WAIT_BID_RESULT, STATE_INITIAL_OR_AFTER_FAILED_BID,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_REP_BID_NOK);

		this.registerTransition(STATE_WAIT_ATTRIBUTION, STATE_WAIT_FISH,
				BidderFSMBehaviourAuctionProgress.TRANSITION_GO_GET_FISH);

		this.registerTransition(STATE_WAIT_ATTRIBUTION, STATE_WAIT_ATTRIBUTION,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_ATTRIBUTION);

		this.registerTransition(STATE_WAIT_FISH, STATE_PAYMENT, BidderFSMBehaviourAuctionProgress.TRANSITION_TO_PAYMENT);

		this.registerTransition(STATE_WAIT_FISH, STATE_WAIT_FISH,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_FISH);

		this.registerTransition(STATE_PAYMENT, STATE_PAYMENT,
				BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_AUCTION_OVER);

		this.registerTransition(STATE_PAYMENT, STATE_AUCTION_OVER_SUCCESSFULLY,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER);

		// Transitions to unsuccessful ends
		this.registerTransition(STATE_INITIAL_OR_AFTER_FAILED_BID, STATE_AUCTION_OVER_UNSUCESSFULLY,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED);

		this.registerTransition(STATE_INITIAL_OR_AFTER_FAILED_BID, STATE_OTHER_BIDDER_WON,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER);

		this.registerTransition(STATE_ABOUT_TO_BID, STATE_AUCTION_OVER_UNSUCESSFULLY,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED);

		this.registerTransition(STATE_ABOUT_TO_BID, STATE_OTHER_BIDDER_WON,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER);

		this.registerTransition(STATE_WAIT_BID_RESULT, STATE_AUCTION_OVER_UNSUCESSFULLY,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED);

		this.registerTransition(STATE_WAIT_BID_RESULT, STATE_OTHER_BIDDER_WON,
				BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER);

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

	@Override
	protected void handleStateEntered(Behaviour state) {
		((Bidder) myAgent).updateAuctionStatus(getBehaviourName());
	}

	@Override
	public int onEnd() {
		return super.onEnd();
	}

	// OneShot Behaviours

	private static class AboutToBidBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourAuctionProgress myFSM;

		private static String AUCTION_BID_SENT = "Bid sent.";
		private static String AUCTION_PRICE_TOO_HIGH = "Skipping : price = %.2f ; limit = %.2f.";

		private boolean isWaitingNewAnnounce = false;

		private ACLMessage lastMessage = null;

		private int transition;

		/**
		 * Max and min time before bid is sent when auto bid mode is on.
		 * (millisecs)
		 */
		private static long AUTO_BID_RANDOM_MAX_TIME_LIMIT = 2000l;
		private static long AUTO_BID_RANDOM_MIN_TIME_LIMIT = 500l;

		/** Message filtering */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate
				.and(MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
						MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ANNOUNCE),
								MessageTemplate.or(
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CANCEL),
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.AUCTION_OVER))));

		public AboutToBidBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			// read price from last announce
			ACLMessage mess = this.myFSM.getRequest();
			float price;

			if (mess != null) {
				this.lastMessage = mess;
				price = Float.parseFloat(mess.getContent());
				// Display price to user and store it
				Bidder.handleAnnounce(price);
				// remove last announce
				this.myFSM.setRequest(null);

				this.isWaitingNewAnnounce = false;

				// needs user interaction
				Bidder.setAnswerBid(false);
				Bidder.setWithinBiddingTimeFrame(true);

				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_USER_CHOICE;
			}

			ACLMessage newMessage = myAgent.receive(MESSAGE_FILTER);

			// Did we receive a new message ?
			if (newMessage != null) {
				// new price, cancel, or auction end
				if (newMessage.getPerformative() == FishMarketProtocol.Performatives.TO_ANNOUNCE) {
					this.myFSM.setRequest(newMessage);

					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_SUBSEQUENT_ANNOUNCE;

					Bidder.setWithinBiddingTimeFrame(true);
					Bidder.setAnswerBid(false);
				} else if (newMessage.getPerformative() == FishMarketProtocol.Performatives.TO_CANCEL) {
					// to final state
					this.myFSM.setRequest(null);
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED;

					Bidder.setWithinBiddingTimeFrame(false);
					Bidder.setAnswerBid(false);
				} else {
					// to final state : other bidder won
					this.myFSM.setRequest(null);
					// mess.getPerformative() ==
					// FishMarketProtocol.Performatives.AUCTION_OVER
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER;

					Bidder.setWithinBiddingTimeFrame(false);
					Bidder.setAnswerBid(false);
				}
			}

			// is bidding allowed ?
			if (Bidder.isWithinBiddingTimeFrame() && !this.isWaitingNewAnnounce) {
				// is bidding handled automatically ?
				if (Bidder.bidsAutomatically() && !Bidder.answerBid()) {
					// is price over our limit ?
					if (Bidder.getBiddingPrice() <= Bidder.getMaxPrice()) {
						// no
						Bidder.setAnswerBid(true);
						// wàit random time so first auto bid agent doesn't
						// always
						// win
						Random rand = new Random();
						long range = (AUTO_BID_RANDOM_MAX_TIME_LIMIT - AUTO_BID_RANDOM_MIN_TIME_LIMIT);
						long waitTime = (long) (rand.nextDouble() * range) + AUTO_BID_RANDOM_MIN_TIME_LIMIT;
						Bidder.displayBidInformation("(Waiting random time)");

						block(waitTime);
					} else {
						String priceTooHigh = String.format(AUCTION_PRICE_TOO_HIGH, Bidder.getBiddingPrice(),
								Bidder.getMaxPrice());
						Bidder.displayBidInformation(priceTooHigh);

						this.isWaitingNewAnnounce = true;

						this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_USER_CHOICE;

						this.myFSM.block();
					}
				} else if (Bidder.answerBid()) {
					// Send bid
					ACLMessage bid = this.lastMessage.createReply();
					bid.setPerformative(FishMarketProtocol.Performatives.TO_BID);
					bid.setContent(String.valueOf(Bidder.getBiddingPrice()));
					bid.clearAllReceiver();
					bid.addReceiver(Bidder.getMarketAgentAID());
					bid.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
					bid.setConversationId(this.lastMessage.getConversationId());
					bid.setSender(Bidder.getAID());
					Bidder.send(bid);

					// user interacted
					Bidder.setWithinBiddingTimeFrame(false);
					Bidder.setAnswerBid(false);

					Bidder.displayBidInformation(AUCTION_BID_SENT);

					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_BID_RESULT;
				} else {
					// wait some more
					System.out.println("Waiting for user bid");
					this.myFSM.block(500);
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_USER_CHOICE;
				}
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private static class AuctionOverSuccessfullyBidderBehaviour extends OneShotBehaviour {
		private static String AUCTION_SUCCESS = "You won the auction.";

		public AuctionOverSuccessfullyBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			System.out.println("Thanks for the fish");
			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			Bidder.displayBidInformation(AUCTION_SUCCESS);
			Bidder.auctionOver();
		}
	}

	private static class AuctionOverUnsuccessfullyBidderBehaviour extends OneShotBehaviour {
		private static String AUCTION_FAILURE = "Auction canceled.";

		public AuctionOverUnsuccessfullyBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			System.out.println("Auction cancelled.");

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			Bidder.displayBidInformation(AUCTION_FAILURE);
			Bidder.auctionOver();
		}
	}

	private static class InitialOrAfterFailedBidBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourAuctionProgress myFSM;

		private boolean firstAnnounce = true;

		private static String AUCTION_START = "Auction start";

		private static String ENTERED_AUCTION = "Entered auction";

		/** Allows filtering incoming messages. */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate
				.and(MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
						MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ANNOUNCE),
								MessageTemplate.or(
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CANCEL),
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.AUCTION_OVER))));

		private int transition;

		public InitialOrAfterFailedBidBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;

			// Display enter message
			((Bidder) a).displayBidInformation(ENTERED_AUCTION);

		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			ACLMessage mess = Bidder.receive(MESSAGE_FILTER);

			if (mess != null) {
				this.myFSM.setRequest(mess);

				if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_ANNOUNCE) {
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_FIRST_ANNOUNCE;
					if (this.firstAnnounce) {
						this.firstAnnounce = false;
						Bidder.displayBidInformation(AUCTION_START);
					}
				} else if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_CANCEL) {
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED;
				} else {
					// mess.getPerformative() ==
					// FishMarketProtocol.Performatives.AUCTION_OVER
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER;
				}
			} else {
				this.myFSM.block();
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_FIRST_ANNOUNCE;
			}

			// transition to next step

		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private static class OtherBidderWonBidderBehaviour extends OneShotBehaviour {

		private static String OTHER_BIDDER_WON = "Another bidder won the auction.";

		public OtherBidderWonBidderBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			System.out.println("Other bidder won.");

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			Bidder.displayBidInformation(OTHER_BIDDER_WON);
			Bidder.auctionOver();
		}
	}

	private static class PaymentBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourAuctionProgress myFSM;

		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.AUCTION_OVER));

		private int transition;

		public PaymentBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			ACLMessage mess = this.myFSM.getRequest();

			if (mess != null) {
				this.myFSM.setRequest(null);
				ACLMessage payment = mess.createReply();

				payment.setConversationId(mess.getConversationId());
				payment.clearAllReceiver();
				payment.addReceiver(Bidder.getMarketAgentAID());
				payment.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
				payment.setPerformative(FishMarketProtocol.Performatives.TO_PAY);
				payment.setContent(String.valueOf(Bidder.getBiddingPrice()));

				Bidder.send(payment);

				// wait for payment acknowledgement (auction_over)
				this.myFSM.block();
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_AUCTION_OVER;
			} else {
				mess = Bidder.receive(MESSAGE_FILTER);

				if (mess != null) {
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER;
				} else {
					// wait some more
					this.myFSM.block();
				}
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private static class WaitAttributionBidderBehaviour extends OneShotBehaviour {
		private BidderFSMBehaviourAuctionProgress myFSM;

		private int transition;

		/** Message filtering */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ATTRIBUTE));

		public WaitAttributionBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			ACLMessage message = myAgent.receive(MESSAGE_FILTER);

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			if (message != null) {
				System.out.println("Received attribution");
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_GO_GET_FISH;
			} else {
				this.myFSM.block();
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_ATTRIBUTION;
			}
		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private static class WaitBidResultBidderBehaviour extends OneShotBehaviour {

		private BidderFSMBehaviourAuctionProgress myFSM;

		private int transition;

		/** Message filtering */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate
				.and(MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
						MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.REP_BID),
								MessageTemplate.or(
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CANCEL),
										MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.AUCTION_OVER))));

		public WaitBidResultBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			// waiting for bid results.

			ACLMessage mess = myAgent.receive(MESSAGE_FILTER);

			if (mess != null) {
				this.myFSM.setRequest(mess);

				if (mess.getPerformative() == FishMarketProtocol.Performatives.REP_BID) {
					// Bid results
					boolean bidResult = Boolean.valueOf(mess.getContent());
					if (bidResult == true) {
						this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_REP_BID_OK;
						System.out.println("I won !!!");
					} else {
						// bid not won
						this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_REP_BID_NOK;
						System.out.println("Several bidders");
					}
				} else if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_CANCEL) {
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_CANCELLED;
				} else {
					// mess.getPerformative() ==
					// FishMarketProtocol.Performatives.AUCTION_OVER
					this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_RECEIVED_AUCTION_OVER;
				}
			} else {
				this.myFSM.block();
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_BID_RESULT;
			}

		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

	private static class WaitFishBidderBehaviour extends OneShotBehaviour {

		private BidderFSMBehaviourAuctionProgress myFSM;

		/** Message filtering */
		private static final MessageTemplate MESSAGE_FILTER = MessageTemplate.and(
				MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_GIVE));

		private int transition;

		public WaitFishBidderBehaviour(Agent a, BidderFSMBehaviourAuctionProgress fsm) {
			super(a);
			this.myFSM = fsm;
		}

		@Override
		public void action() {
			System.out.println("action => " + getBehaviourName());

			Bidder Bidder = (Bidder) super.myAgent;

			Bidder.updateAuctionStatus(getBehaviourName());

			ACLMessage message = myAgent.receive(MESSAGE_FILTER);

			if (message != null) {
				this.myFSM.setRequest(message);
				System.out.println("Received Fish");
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_TO_PAYMENT;
			} else {
				System.out.println("Waiting for fish");
				this.myFSM.block();
				this.transition = BidderFSMBehaviourAuctionProgress.TRANSITION_WAIT_FISH;
			}

		}

		@Override
		public int onEnd() {
			return this.transition;
		}
	}

}

