package agent.market.behaviours;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class MarketFSMBehaviourHandshake extends FSMBehaviour {
    private ACLMessage request;
    public static final AID MESSAGE_TOPIC = TopicUtility
	    .createTopic(FishMarketProtocol.steps.STEP_BIDDERS_SUBSCRIPTION);
    public static final MessageTemplate MESSAGE_FILTER = MessageTemplate
	    .MatchTopic(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);
    private static final String STATE_WAIT_BIDDER_REQUEST = "STATE_WAIT_BIDDER_REQUEST";
    private static final String STATE_PROVIDE_AUCTION_LIST = "STATE_PRIVIDE_AUCTION_LIST";
    private static final String STATE_EVALUATE_SUBSCRIPTION_REQUEST = "STATE_EVALUATE_SUBSCRIPTION_REQUEST";
    private static final String STATE_NOTIFY_SELLER = "STATE_NOTIFY_SELLER";
    private static final String STATE_TERMINATE_BIDDER_MANAGEMENT = "STATE_TERMINATE_BIDDER_MANAGEMENT";
    public static final int TRANSITION_TO_PROVIDE_AUCTION_LIST = 0;
    public static final int TRANSITION_TO_VALUATE_REQUEST = 1;
    public static final int TRANSITION_TO_TERMINATE = 2;
    public static final int TRANSITION_TO_WAIT_REQUEST = 3;
    public static final int TRANSITION_TO_NOTIFY_SELLER = 4;
    public static final int STATUS_REFUSE_AUCTION_OVER = 0;
    public static final int STATUS_REFUSE_AUCTION_CANCELLED = 1;
    public static final int STATUS_REFUSE_AUCTION_NOT_FOUND = 2;
    public static final int STATUS_REFUSE_ALREADY_REGISTERED = 3;
    public static final int STATUS_REFUSE_SELLER_AID_NOT_UNDERSTOOD = 4;

    public MarketFSMBehaviourHandshake(Market myMarketAgent) {
	super(myMarketAgent);
	// Register states
	// The last state should call myMarketAgent.setIsDone(true);
	this.registerFirstState(new WaitBidderRequestMarketBehaviour(myMarketAgent, this), STATE_WAIT_BIDDER_REQUEST);
	this.registerState(new ProvideAuctionListMarketBehaviour(myMarketAgent, this), STATE_PROVIDE_AUCTION_LIST);
	this.registerState(new EvaluateSubscriptionRequestMarketBehaviour(myMarketAgent, this),
		STATE_EVALUATE_SUBSCRIPTION_REQUEST);
	this.registerState(new NotifySellerMarketBehaviour(myMarketAgent, this), STATE_NOTIFY_SELLER);
	this.registerLastState(new TerminateSubscriptionsMarketBehaviour(myMarketAgent),
		STATE_TERMINATE_BIDDER_MANAGEMENT);
	// Register transitions
	this.registerTransition(STATE_WAIT_BIDDER_REQUEST, STATE_PROVIDE_AUCTION_LIST,
		TRANSITION_TO_PROVIDE_AUCTION_LIST);
	this.registerTransition(STATE_WAIT_BIDDER_REQUEST, STATE_EVALUATE_SUBSCRIPTION_REQUEST,
		TRANSITION_TO_VALUATE_REQUEST);
	this.registerTransition(STATE_WAIT_BIDDER_REQUEST, STATE_WAIT_BIDDER_REQUEST, TRANSITION_TO_WAIT_REQUEST);
	this.registerTransition(STATE_WAIT_BIDDER_REQUEST, STATE_TERMINATE_BIDDER_MANAGEMENT, TRANSITION_TO_TERMINATE);
	this.registerDefaultTransition(STATE_PROVIDE_AUCTION_LIST, STATE_WAIT_BIDDER_REQUEST);
	this.registerTransition(STATE_EVALUATE_SUBSCRIPTION_REQUEST, STATE_WAIT_BIDDER_REQUEST,
		TRANSITION_TO_WAIT_REQUEST);
	this.registerTransition(STATE_EVALUATE_SUBSCRIPTION_REQUEST, STATE_NOTIFY_SELLER, TRANSITION_TO_NOTIFY_SELLER);
	this.registerDefaultTransition(STATE_NOTIFY_SELLER, STATE_WAIT_BIDDER_REQUEST);
    }

    public ACLMessage getRequest() {
	return this.request;
    }

    public void setRequest(ACLMessage request) {
	this.request = request;
    }

    // Behaviours
    private class EvaluateSubscriptionRequestMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourHandshake myFSM;
	private int status;
	private boolean subscriptionAccepted;

	public EvaluateSubscriptionRequestMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourHandshake myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    this.subscriptionAccepted = false;
	    ACLMessage request = this.myFSM.getRequest();
	    Market myMarketAgent = (Market) super.myAgent;
	    String auctionId = (String) request.getContent();
	    if (auctionId != null) {
		Auction requestedAuction = myMarketAgent.findAuction(auctionId);
		if (requestedAuction != null) {
		    if (requestedAuction.getStatus() != Auction.STATUS_OVER
			    && requestedAuction.getStatus() != Auction.STATUS_CANCELLED
			    && !myMarketAgent.isSuscriber(auctionId, request.getSender())) {
			this.subscriptionAccepted = true; //// Accepted
							  //// !!!!!!!!!!
			ACLMessage reply = request.createReply();
			AID bidderAID = request.getSender();
			// Register subscription
			myMarketAgent.addSuscriber(auctionId, bidderAID);
			// Reply accept
			reply.setPerformative(FishMarketProtocol.Performatives.TO_ACCEPT);
			// Set topic
			reply.addReceiver(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);
			// Inform conversation ID
			reply.setConversationId(auctionId);
			reply.setContent(auctionId);
			// Send
			myMarketAgent.send(reply);
		    } else if (requestedAuction.getStatus() == Auction.STATUS_OVER) {
			this.status = MarketFSMBehaviourHandshake.STATUS_REFUSE_AUCTION_OVER;
		    } else if (requestedAuction.getStatus() == Auction.STATUS_CANCELLED) {
			this.status = MarketFSMBehaviourHandshake.STATUS_REFUSE_AUCTION_CANCELLED;
		    } else {
			this.status = MarketFSMBehaviourHandshake.STATUS_REFUSE_ALREADY_REGISTERED;
		    }
		} else {
		    this.status = MarketFSMBehaviourHandshake.STATUS_REFUSE_AUCTION_NOT_FOUND;
		}
	    } else {
		this.status = MarketFSMBehaviourHandshake.STATUS_REFUSE_SELLER_AID_NOT_UNDERSTOOD;
	    }
	}

	@Override
	public int onEnd() {
	    return (this.subscriptionAccepted) ? MarketFSMBehaviourHandshake.TRANSITION_TO_NOTIFY_SELLER
		    : this.replyRefuse();
	}

	private int replyRefuse() {
	    // Reply
	    ACLMessage reply = this.myFSM.getRequest().createReply();
	    reply.setPerformative(FishMarketProtocol.Performatives.TO_REFUSE);
	    // Set refuse status code
	    reply.setContent(String.valueOf(this.status));
	    // Set topic
	    reply.addReceiver(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);
	    super.myAgent.send(reply);
	    return MarketFSMBehaviourHandshake.TRANSITION_TO_WAIT_REQUEST;
	}
    }

    private class NotifySellerMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourHandshake myFSM;

	public NotifySellerMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourHandshake myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // Notify the seller (so he can start to announce).
	    Market myMarketAgent = (Market) super.myAgent;
	    ACLMessage request = this.myFSM.getRequest();
	    String auctionID = (String) request.getContent();
	    ACLMessage notify = new ACLMessage(FishMarketProtocol.Performatives.TO_SUBSCRIBE);
	    // Set topic
	    notify.addReceiver(MarketFSMBehaviourAuctionCreation.MESSAGE_TOPIC);
	    // Inform conversation ID
	    notify.setConversationId(auctionID);
	    notify.setContent(auctionID);
	    notify.addReceiver(myMarketAgent.getSeller(auctionID));
	    myMarketAgent.send(notify);
	    // Update GUI
	    myMarketAgent.refreshView();
	}
    }

    private static class ProvideAuctionListMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourHandshake myFSM;
	private static final Logger LOGGER = Logger.getLogger(ProvideAuctionListMarketBehaviour.class.getName());

	public ProvideAuctionListMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourHandshake myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // Filter auctions
	    HashSet<Auction> runninAuctions = new HashSet<Auction>();
	    for (Auction auction : ((Market) myAgent).getRegisteredAuctions()) {
		if (auction.getStatus() != Auction.STATUS_OVER && auction.getStatus() != Auction.STATUS_CANCELLED) {
		    runninAuctions.add(auction);
		}
	    }
	    // Provide auction list
	    ACLMessage reply = this.myFSM.getRequest().createReply();
	    reply.setPerformative(FishMarketProtocol.Performatives.TO_PROVIDE);
	    // Add topic
	    reply.addReceiver(MarketFSMBehaviourHandshake.MESSAGE_TOPIC);
	    // Set content and send
	    try {
		reply.setContentObject(runninAuctions);
		myAgent.send(reply);
	    } catch (IOException e) {
		ProvideAuctionListMarketBehaviour.LOGGER.log(Level.SEVERE, null, e);
	    }
	}
    }

    private class TerminateSubscriptionsMarketBehaviour extends OneShotBehaviour {
	public TerminateSubscriptionsMarketBehaviour(Market myMarketAgent) {
	    super(myMarketAgent);
	}

	@Override
	public void action() {
	    ((Market) super.myAgent).setIsDone(true);
	    // Say bye !
	}
    }

    private static class WaitBidderRequestMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourHandshake myFSM;
	private int transition;
	private static final MessageTemplate MESSAGE_FILTER = MessageTemplate
		.and(MarketFSMBehaviourHandshake.MESSAGE_FILTER,
			MessageTemplate.or(
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_REQUEST),
				MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_SUBSCRIBE)));

	public WaitBidderRequestMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourHandshake myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // Wait that myAgent receives message
	    this.block();
	    // Receive messages
	    ACLMessage mess = myAgent.receive(WaitBidderRequestMarketBehaviour.MESSAGE_FILTER);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		this.restart();
		if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_REQUEST) {
		    this.transition = MarketFSMBehaviourHandshake.TRANSITION_TO_PROVIDE_AUCTION_LIST;
		} else {
		    this.transition = MarketFSMBehaviourHandshake.TRANSITION_TO_VALUATE_REQUEST;
		}
	    } else {
		this.transition = MarketFSMBehaviourHandshake.TRANSITION_TO_WAIT_REQUEST;
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}
    }
}
