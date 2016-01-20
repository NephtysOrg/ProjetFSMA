package agent.market.behaviours;

import java.util.HashSet;
import java.util.Set;

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
public class MarketFSMBehaviourAuctionProgress extends FSMBehaviour {
    private final AID mySeller;
    private final String myAuctionId;
    private final MessageTemplate messageFilter;
    private AID firstBidder = null;
    private Set<AID> bidders = new HashSet<AID>();
    private ACLMessage request;
    public static final AID MESSAGE_TOPIC = TopicUtility.createTopic(FishMarketProtocol.steps.STEP_AUCTION_IN_PROGRESS);
    public static final long BID_WAIT_DELAY = 10000; // 10 sec
    private static final String STATE_WAIT_TO_ANNOUNCE = "STATE_WAIT_TO_ANNOUNCE";
    private static final String STATE_RELAY_TO_ANNOUNCE = "STATE_RELAY_TO_ANNOUNCE";
    private static final String STATE_WAIT_TO_BID = "STATE_WAIT_TO_BID";
    private static final String STATE_RELAY_TO_BID = "STATE_RELAY_TO_BID";
    private static final String STATE_RELAY_AUCTION_CANCELLED = "STATE_RELAY_AUCTION_CANCELLED";
    private static final String STATE_RELAY_REP_BID_OK = "STATE_RELAY_REP_BID_OK";
    private static final String STATE_RELAY_REP_BID_NOK = "STATE_RELAY_REP_BID_NOK";
    private static final String STATE_WAIT_TO_ATTRIBUTE = "STATE_WAIT_TO_ATTRIBUTE";
    private static final String STATE_RELAY_TO_ATTRIBUTE = "STATE_RELAY_TO_ATTRIBUTE";
    private static final String STATE_WAIT_TO_GIVE = "STATE_WAIT_TO_GIVE";
    private static final String STATE_RELAY_TO_GIVE = "STATE_RELAY_TO_GIVE";
    private static final String STATE_WAIT_TO_PAY = "STATE_WAIT_TO_PAY";
    private static final String STATE_RELAY_TO_PAY = "STATE_RELAY_TO_PAY";
    private static final String STATE_WAIT_AUCTION_OVER = "STATE_WAIT_AUCTION_OVER";
    private static final String STATE_RELAY_AUCTION_OVER = "STATE_RELAY_AUCTION_OVER";
    private static final String STATE_TERMINATE_SUCCESS = "STATE_TERMINATE_SUCCESS";
    private static final String STATE_TERMINATE_CANCEL = "STATE_TERMINATE_CANCEL";
    public static final int TRANSITION_TO_WAIT_TO_ANNOUNCE = 0;
    public static final int TRANSITION_TO_RELAY_TO_ANNOUNCE = 1;
    public static final int TRANSITION_TO_WAIT_TO_BID = 2;
    public static final int TRANSITION_TO_RELAY_TO_BID = 3;
    public static final int TRANSITION_TO_CANCEL = 4;
    public static final int TRANSITION_TO_RELAY_REP_BID_OK = 5;
    public static final int TRANSITION_TO_RELAY_REP_BID_NOK = 6;
    public static final int TRANSITION_TO_WAIT_TO_ATTRIBUTE = 7;
    public static final int TRANSITION_TO_RELAY_TO_ATTRIBUTE = 8;
    public static final int TRANSITION_TO_WAIT_TO_GIVE = 9;
    public static final int TRANSITION_TO_RELAY_TO_GIVE = 10;
    public static final int TRANSITION_TO_WAIT_TO_PAY = 11;
    public static final int TRANSITION_TO_RELAY_TO_PAY = 12;
    public static final int TRANSITION_TO_WAIT_AUCTION_OVER = 13;
    public static final int TRANSITION_TO_RELAY_AUCTION_OVER = 14;

    public MarketFSMBehaviourAuctionProgress(Market myMarketAgent, AID mySeller, String myAuctionId) {
	super(myMarketAgent);
	this.mySeller = mySeller;
	this.myAuctionId = myAuctionId;
	this.messageFilter = this.createMessageFilter();
	// Register states
	this.registerFirstState(new WaitToAnnounceMarketBehaviour(myMarketAgent, this), STATE_WAIT_TO_ANNOUNCE);
	this.registerState(new WaitToBidMarketBehaviour(myMarketAgent, this), STATE_WAIT_TO_BID);
	this.registerState(new WaitToAttributeMarketBehaviour(myMarketAgent, this), STATE_WAIT_TO_ATTRIBUTE);
	this.registerState(new WaitToGiveMarketBehaviour(myMarketAgent, this), STATE_WAIT_TO_GIVE);
	this.registerState(new WaitToPayMarketBehaviour(myMarketAgent, this), STATE_WAIT_TO_PAY);
	this.registerState(new WaitAuctionOverMarketBehaviour(myMarketAgent, this), STATE_WAIT_AUCTION_OVER);
	this.registerState(new RelayToAnnounceMarketBehaviour(myMarketAgent, this), STATE_RELAY_TO_ANNOUNCE);
	this.registerState(new RelayToBidMarketBehaviour(myMarketAgent, this), STATE_RELAY_TO_BID);
	this.registerState(new RelayToCancelMarketBehaviour(myMarketAgent, this), STATE_RELAY_AUCTION_CANCELLED);
	this.registerState(new RelayRepBidOkMarketBehaviour(myMarketAgent, this), STATE_RELAY_REP_BID_OK);
	this.registerState(new RelayRepBidNokMarketBehaviour(myMarketAgent, this), STATE_RELAY_REP_BID_NOK);
	this.registerState(new RelayToAttributeMarketBehaviour(myMarketAgent, this), STATE_RELAY_TO_ATTRIBUTE);
	this.registerState(new RelayToGiveMarketBehaviour(myMarketAgent, this), STATE_RELAY_TO_GIVE);
	this.registerState(new RelayToPayMarketBehaviour(myMarketAgent, this), STATE_RELAY_TO_PAY);
	this.registerState(new RelayAuctionOverMarketBehaviour(myMarketAgent, this), STATE_RELAY_AUCTION_OVER);
	this.registerLastState(new TerminateSuccessMarketBehaviour(myMarketAgent, this), STATE_TERMINATE_SUCCESS);
	this.registerLastState(new TerminateCancelMarketBehaviour(myMarketAgent, this), STATE_TERMINATE_CANCEL);
	// Register transitions
	this.registerTransition(STATE_WAIT_TO_ANNOUNCE, STATE_WAIT_TO_ANNOUNCE, TRANSITION_TO_WAIT_TO_ANNOUNCE);
	this.registerTransition(STATE_WAIT_TO_ANNOUNCE, STATE_RELAY_TO_ANNOUNCE, TRANSITION_TO_RELAY_TO_ANNOUNCE);
	this.registerTransition(STATE_WAIT_TO_ANNOUNCE, STATE_TERMINATE_CANCEL, TRANSITION_TO_CANCEL);
	this.registerDefaultTransition(STATE_RELAY_TO_ANNOUNCE, STATE_WAIT_TO_BID);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_WAIT_TO_BID, TRANSITION_TO_WAIT_TO_BID);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_RELAY_TO_ANNOUNCE, TRANSITION_TO_RELAY_TO_ANNOUNCE);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_RELAY_TO_BID, TRANSITION_TO_RELAY_TO_BID);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_RELAY_REP_BID_OK, TRANSITION_TO_RELAY_REP_BID_OK);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_RELAY_REP_BID_NOK, TRANSITION_TO_RELAY_REP_BID_NOK);
	this.registerTransition(STATE_WAIT_TO_BID, STATE_RELAY_AUCTION_CANCELLED, TRANSITION_TO_CANCEL);
	this.registerDefaultTransition(STATE_RELAY_TO_BID, STATE_WAIT_TO_BID);
	this.registerDefaultTransition(STATE_RELAY_REP_BID_NOK, STATE_WAIT_TO_ANNOUNCE);
	this.registerDefaultTransition(STATE_RELAY_AUCTION_CANCELLED, STATE_TERMINATE_CANCEL);
	this.registerDefaultTransition(STATE_RELAY_REP_BID_OK, STATE_WAIT_TO_ATTRIBUTE);
	this.registerTransition(STATE_WAIT_TO_ATTRIBUTE, STATE_WAIT_TO_ATTRIBUTE, TRANSITION_TO_WAIT_TO_ATTRIBUTE);
	this.registerTransition(STATE_WAIT_TO_ATTRIBUTE, STATE_RELAY_TO_ATTRIBUTE, TRANSITION_TO_RELAY_TO_ATTRIBUTE);
	this.registerDefaultTransition(STATE_RELAY_TO_ATTRIBUTE, STATE_WAIT_TO_GIVE);
	this.registerTransition(STATE_WAIT_TO_GIVE, STATE_WAIT_TO_GIVE, TRANSITION_TO_WAIT_TO_GIVE);
	this.registerTransition(STATE_WAIT_TO_GIVE, STATE_RELAY_TO_GIVE, TRANSITION_TO_RELAY_TO_GIVE);
	this.registerDefaultTransition(STATE_RELAY_TO_GIVE, STATE_WAIT_TO_PAY);
	this.registerTransition(STATE_WAIT_TO_PAY, STATE_WAIT_TO_PAY, TRANSITION_TO_WAIT_TO_PAY);
	this.registerTransition(STATE_WAIT_TO_PAY, STATE_RELAY_TO_PAY, TRANSITION_TO_RELAY_TO_PAY);
	this.registerDefaultTransition(STATE_RELAY_TO_PAY, STATE_WAIT_AUCTION_OVER);
	this.registerTransition(STATE_WAIT_AUCTION_OVER, STATE_WAIT_AUCTION_OVER, TRANSITION_TO_WAIT_AUCTION_OVER);
	this.registerTransition(STATE_WAIT_AUCTION_OVER, STATE_RELAY_AUCTION_OVER, TRANSITION_TO_RELAY_AUCTION_OVER);
	this.registerDefaultTransition(STATE_RELAY_AUCTION_OVER, STATE_TERMINATE_SUCCESS);
    }

    public void setRequest(ACLMessage request) {
	this.request = request;
    }

    public ACLMessage getRequest() {
	return this.request;
    }

    public AID getSeller() {
	return mySeller;
    }

    public AID getSelectedBidder() {
	return this.firstBidder;
    }

    public void setFirstBidder(AID bidder) {
	this.firstBidder = bidder;
    }

    public AID getFirstBidder() {
	return firstBidder;
    }

    public Set<AID> getBidders() {
	return this.bidders;
    }

    public void addBidder(AID bidder) {
	if (this.bidders.isEmpty()) {
	    this.firstBidder = bidder;
	}
	this.bidders.add(bidder);
    }

    public void clearBidderList() {
	this.bidders.clear();
	this.firstBidder = null;
    }

    /**
     * 
     * @return the ID of the conversation associated with the auction that this
     *         behaviour manages.
     */
    public String getAuctionId() {
	return myAuctionId;
    }

    public static String createAuctionId(AID sellerAID) {
	return sellerAID.getName().toString();
    }

    public MessageTemplate getMessageFilter() {
	return messageFilter;
    }

    private MessageTemplate createMessageFilter() {
	return MessageTemplate.and(MessageTemplate.MatchTopic(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC),
		MessageTemplate.MatchConversationId(this.myAuctionId));
    }

    // Behaviours
    private class RelayAuctionOverMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayAuctionOverMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying auction over !");
	    ACLMessage request = this.myFSM.getRequest();
	    Market myMarketAgent = (Market) super.myAgent;
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    // Receivers
	    for (AID subscriber : myMarketAgent.getSubscribers(this.myFSM.getAuctionId())) {
		toRelay.addReceiver(subscriber);
	    }
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    myMarketAgent.send(toRelay);
	    // Delete request
	    this.myFSM.setRequest(null);
	    // Close auction
	    myMarketAgent.notifyAuctionOver(this.myFSM.getAuctionId(), this.myFSM.getSelectedBidder().getLocalName());
	    ;
	}
    }

    private class RelayRepBidNokMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayRepBidNokMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying rep bid nok !");
	    ACLMessage request = this.myFSM.getRequest();
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    // Receivers
	    for (AID bidder : this.myFSM.getBidders()) {
		toRelay.addReceiver(bidder);
	    }
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    // Content (false)
	    toRelay.setContent(request.getContent());
	    super.myAgent.send(toRelay);
	    this.myFSM.setRequest(null);
	    this.myFSM.clearBidderList();
	}
    }

    private class RelayRepBidOkMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayRepBidOkMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying rep bid ok !");
	    ACLMessage request = this.myFSM.getRequest();
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    // Receiver
	    toRelay.addReceiver(this.myFSM.getSelectedBidder());
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    // Content (true)
	    toRelay.setContent(request.getContent());
	    super.myAgent.send(toRelay);
	    this.myFSM.setRequest(null);
	}
    }

    private class RelayToAnnounceMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToAnnounceMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to announce !");
	    Market myMarketAgent = (Market) super.myAgent;
	    ACLMessage request = this.myFSM.getRequest();
	    // Update auction price
	    float price = Float.parseFloat((String) request.getContent());
	    myMarketAgent.setAuctionPrice(this.myFSM.getAuctionId(), price);
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    // Add all subscribers as receivers
	    for (AID subscriber : myMarketAgent.getSubscribers(this.myFSM.getAuctionId())) {
		toRelay.addReceiver(subscriber);
	    }
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    // Content (price)
	    toRelay.setContent(request.getContent());
	    // Send
	    myMarketAgent.send(toRelay);
	    // delete request
	    this.myFSM.setRequest(null);
	    // Update GUI
	    myMarketAgent.refreshView();
	}
    }

    private class RelayToAttributeMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToAttributeMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to attribute !");
	    ACLMessage request = this.myFSM.getRequest();
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    toRelay.addReceiver(this.myFSM.getSelectedBidder());
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    super.myAgent.send(toRelay);
	    // Delete request
	    this.myFSM.setRequest(null);
	}
    }

    private class RelayToBidMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToBidMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to bid !");
	    ACLMessage request = this.myFSM.getRequest();
	    this.myFSM.addBidder(request.getSender());
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    // set seller agent as receiver
	    toRelay.addReceiver(this.myFSM.getSeller());
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    // Content (price)
	    toRelay.setContent(request.getContent());
	    // Send
	    super.myAgent.send(toRelay);
	    // DEBUG
	    System.out.println("Market: setting transition to wwait rep bid !");
	    // Delete request
	    this.myFSM.setRequest(null);
	}
    }

    private class RelayToCancelMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToCancelMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to cancel !");
	    ACLMessage request = this.myFSM.getRequest();
	    Market myMarketAgent = (Market) super.myAgent;
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    for (AID subscriber : myMarketAgent.getSubscribers(this.myFSM.getAuctionId())) {
		toRelay.addReceiver(subscriber);
	    }
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    myMarketAgent.send(toRelay);
	    // Delete request
	    this.myFSM.setRequest(null);
	    // Close auction
	    myMarketAgent.notifyAuctionCancelled(this.myFSM.getAuctionId());
	}
    }

    private class RelayToGiveMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToGiveMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to give !");
	    ACLMessage request = this.myFSM.getRequest();
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    toRelay.addReceiver(this.myFSM.getSelectedBidder());
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    super.myAgent.send(toRelay);
	    // Delete request
	    this.myFSM.setRequest(null);
	}
    }

    private class RelayToPayMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public RelayToPayMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    // DEBUG
	    System.out.println("Market: relaying to pay !");
	    ACLMessage request = this.myFSM.getRequest();
	    // RELAY
	    ACLMessage toRelay = new ACLMessage(request.getPerformative());
	    toRelay.addReceiver(this.myFSM.getSeller());
	    // Message topic
	    toRelay.addReceiver(MarketFSMBehaviourAuctionProgress.MESSAGE_TOPIC);
	    // Conversation ID
	    toRelay.setConversationId(request.getConversationId());
	    super.myAgent.send(toRelay);
	    // Delete request
	    this.myFSM.setRequest(null);
	}
    }

    private class TerminateCancelMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public TerminateCancelMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    Market myMarketAgent = (Market) super.myAgent;
	    // myMarketAgent.deleteAuction(this.myFSM.getAuctionId());
	    // Update GUI
	    myMarketAgent.refreshView();
	    myMarketAgent.clearSubscribers(this.myFSM.getAuctionId());
	    myMarketAgent.removeBehaviour(this.myFSM);
	    // Say bye !
	}
    }

    private class TerminateSuccessMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;

	public TerminateSuccessMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	}

	@Override
	public void action() {
	    Market myMarketAgent = (Market) super.myAgent;
	    // myMarketAgent.deleteAuction(this.myFSM.getAuctionId());
	    // Update GUI
	    myMarketAgent.refreshView();
	    myMarketAgent.clearSubscribers(this.myFSM.getAuctionId());
	    myMarketAgent.removeBehaviour(this.myFSM);
	    // Say bye !
	}
    }

    private class WaitAuctionOverMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitAuctionOverMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for auction over");
	    // Receive messages
	    ACLMessage mess = myAgent.receive(this.messageFilter);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_AUCTION_OVER;
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_AUCTION_OVER;
		// DEBUG
		System.out.println("Market: setting transition to wait wait auction over");
		this.myFSM.block();
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(),
		    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.AUCTION_OVER));
	}
    }

    private class WaitToAnnounceMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitToAnnounceMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for to announce");
	    // Receive messages
	    ACLMessage mess = super.myAgent.receive(this.messageFilter);
	    if (mess != null) {
		if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_ANNOUNCE) {
		    this.myFSM.setRequest(mess);
		    // DEBUG
		    System.out.println("Market: setting transition to relay to announce");
		    this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_ANNOUNCE;
		} else {
		    // DEBUG
		    System.out.println("Market: setting transition to cancel");
		    this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_CANCEL;
		    ((Market) super.myAgent).setAuctionStatus(this.myFSM.getAuctionId(), Auction.STATUS_CANCELLED);
		}
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_ANNOUNCE;
		// DEBUG
		System.out.println("Market: setting transition to wait to announce");
		this.myFSM.block();
	    }
	}

	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(),
		    MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ANNOUNCE),
			    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CANCEL)));
	}
    }

    private class WaitToAttributeMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitToAttributeMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for to attribute");
	    // Receive messages
	    ACLMessage mess = myAgent.receive(this.messageFilter);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		// DEBUG
		System.out.println("Market: setting transition to relay to attribute");
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_ATTRIBUTE;
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_ATTRIBUTE;
		// DEBUG
		System.out.println("Market: setting transition to wait wait to attribute");
		this.myFSM.block();
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(),
		    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ATTRIBUTE));
	}
    }

    private class WaitToBidMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitToBidMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for to bid");
	    // Receive messages
	    ACLMessage mess = myAgent.receive(this.messageFilter);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_BID) {
		    // DEBUG
		    System.out.println("Market: setting transition to relay bid");
		    this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_BID;
		} else if (mess.getPerformative() == FishMarketProtocol.Performatives.TO_ANNOUNCE) {
		    // DEBUG
		    System.out.println("Market: setting transition to relay to announce");
		    this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_ANNOUNCE;
		} else if (mess.getPerformative() == FishMarketProtocol.Performatives.REP_BID) {
		    boolean repBidOk = Boolean.parseBoolean(mess.getContent());
		    if (repBidOk) {
			// DEBUG
			System.out.println("Market: setting transition to relay rep bid ok");
			this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_REP_BID_OK;
		    } else {
			// DEBUG
			System.out.println("Market: setting transition to relay rep bid nok");
			this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_REP_BID_NOK;
		    }
		} else {
		    // DEBUG
		    System.out.println("Market: setting transition to cancel");
		    this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_CANCEL;
		}
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_BID;
		// DEBUG
		System.out.println("Market: setting transition to wait to bid");
		this.myFSM.block();
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(), MessageTemplate.or(
		    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_BID),
		    MessageTemplate.or(MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_ANNOUNCE),
			    MessageTemplate.or(
				    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.REP_BID),
				    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_CANCEL)))));
	}
    }

    private class WaitToGiveMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitToGiveMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for to give");
	    // Receive messages
	    ACLMessage mess = myAgent.receive(this.messageFilter);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		// DEBUG
		System.out.println("Market: setting transition to relay to give");
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_GIVE;
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_GIVE;
		// DEBUG
		System.out.println("Market: setting transition to wait to give");
		this.myFSM.block();
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(),
		    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_GIVE));
	}
    }

    private class WaitToPayMarketBehaviour extends OneShotBehaviour {
	private MarketFSMBehaviourAuctionProgress myFSM;
	private int transition;
	private final MessageTemplate messageFilter;

	public WaitToPayMarketBehaviour(Market myMarketAgent, MarketFSMBehaviourAuctionProgress myFSM) {
	    super(myMarketAgent);
	    this.myFSM = myFSM;
	    this.messageFilter = this.createMessageFilter();
	}

	@Override
	public void action() {
	    // Delete any previous request
	    this.myFSM.setRequest(null);
	    // DEBUG
	    System.out.println("Market: checking messages for to pay");
	    // Receive messages
	    ACLMessage mess = myAgent.receive(this.messageFilter);
	    if (mess != null) {
		this.myFSM.setRequest(mess);
		// DEBUG
		System.out.println("Market: setting transition to relay to pay");
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_RELAY_TO_PAY;
	    } else {
		// Continue to wait
		this.transition = MarketFSMBehaviourAuctionProgress.TRANSITION_TO_WAIT_TO_PAY;
		// DEBUG
		System.out.println("Market: setting transition to wait wait to pay");
		this.myFSM.block();
	    }
	}

	@Override
	public int onEnd() {
	    return this.transition;
	}

	private MessageTemplate createMessageFilter() {
	    return MessageTemplate.and(this.myFSM.getMessageFilter(),
		    MessageTemplate.MatchPerformative(FishMarketProtocol.Performatives.TO_PAY));
	}
    }
}
